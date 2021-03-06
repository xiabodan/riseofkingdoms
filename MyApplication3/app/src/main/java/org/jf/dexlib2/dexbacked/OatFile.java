/*
 * Copyright 2014, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib2.dexbacked;

import com.example.myapplication.OatFileParser;
import com.example.myapplication.Xlog;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.io.ByteStreams;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.OatFile.OatDexFile;
import org.jf.dexlib2.dexbacked.OatFile.SymbolTable.Symbol;
import org.jf.dexlib2.dexbacked.raw.HeaderItem;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.util.AbstractForwardSequentialList;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OatFile extends BaseDexBuffer implements MultiDexContainer<OatDexFile> {
    private static final String TAG = OatFileParser.TAG;
    private static final byte[] ELF_MAGIC = new byte[] { 0x7f, 'E', 'L', 'F' };
    private static final byte[] OAT_MAGIC = new byte[] { 'o', 'a', 't', '\n' };
    private static final int MIN_ELF_HEADER_SIZE = 52;

    // These are the "known working" versions that I have manually inspected the source for.
    // Later version may or may not work, depending on what changed.
    private static final int MIN_OAT_VERSION = 39;
    private static final int MAX_OAT_VERSION = 131;

    public static final int UNSUPPORTED = 0;
    public static final int SUPPORTED = 1;
    public static final int UNKNOWN = 2;

    private final boolean is64bit;
    @Nonnull private final OatHeader oatHeader;
    @Nonnull private final Opcodes opcodes;
    @Nullable private final VdexProvider vdexProvider;

    public OatFile(@Nonnull byte[] buf) {
        this(buf, null);
    }

    public OatFile(@Nonnull byte[] buf, @Nullable VdexProvider vdexProvider) {
        super(buf);

        if (buf.length < MIN_ELF_HEADER_SIZE) {
            throw new NotAnOatFileException();
        }

        verifyMagic(buf);

        if (buf[4] == 1) {
            is64bit = false;
        } else if (buf[4] == 2) {
            is64bit = true;
        } else {
            throw new InvalidOatFileException(String.format("Invalid word-size value: %x", buf[5]));
        }

        OatHeader oatHeader = null;
        SymbolTable symbolTable = getSymbolTable();
        for (Symbol symbol: symbolTable.getSymbols()) {
            if (symbol.getName().equals("oatdata")) {
                oatHeader = new OatHeader(symbol.getFileOffset());
                break;
            }
        }

        if (oatHeader == null) {
            throw new InvalidOatFileException("Oat file has no oatdata symbol");
        }
        this.oatHeader = oatHeader;

        if (!oatHeader.isValid()) {
            throw new InvalidOatFileException("Invalid oat magic value");
        }

        System.out.println("Oat Version: " + oatHeader.getVersion());
        System.out.println("Dex Count: " + oatHeader.getDexFileCount());
        System.out.println("Dex Offset: " + oatHeader.getDexListStart());

        this.opcodes = Opcodes.forArtVersion(oatHeader.getVersion());
        this.vdexProvider = vdexProvider;
    }

    private static void verifyMagic(byte[] buf) {
        for (int i = 0; i < ELF_MAGIC.length; i++) {
            if (buf[i] != ELF_MAGIC[i]) {
                throw new NotAnOatFileException();
            }
        }
    }

    public static OatFile fromInputStream(@Nonnull InputStream is) throws IOException {
        return fromInputStream(is, null);
    }

    public static OatFile fromInputStream(@Nonnull InputStream is, @Nullable VdexProvider vdexProvider)
            throws IOException {
        if (!is.markSupported()) {
            throw new IllegalArgumentException("InputStream must support mark");
        }
        is.mark(4);
        byte[] partialHeader = new byte[4];
        try {
            ByteStreams.readFully(is, partialHeader);
        } catch (EOFException ex) {
            throw new NotAnOatFileException();
        } finally {
            is.reset();
        }

        verifyMagic(partialHeader);

        is.reset();

        byte[] buf = ByteStreams.toByteArray(is);
        return new OatFile(buf, vdexProvider);
    }

    public int getOatVersion() {
        return oatHeader.getVersion();
    }

    public int isSupportedVersion() {
        int version = getOatVersion();
        if (version < MIN_OAT_VERSION) {
            return UNSUPPORTED;
        }
        if (version <= MAX_OAT_VERSION) {
            return SUPPORTED;
        }
        return UNKNOWN;
    }

    @Nonnull
    public List<String> getBootClassPath() {
        if (getOatVersion() < 75) {
            return ImmutableList.of();
        }
        String bcp = oatHeader.getKeyValue("bootclasspath");
        if (bcp == null) {
            return ImmutableList.of();
        }
        return Arrays.asList(bcp.split(":"));
    }

    @Nonnull @Override public Opcodes getOpcodes() {
        return opcodes;
    }

    @Nonnull
    public List<DexFile> getDexFiles() {
        return new AbstractForwardSequentialList<DexFile>() {
            @Override public int size() {
                return oatHeader.getDexFileCount();
            }

            @Nonnull @Override public Iterator<DexFile> iterator() {
                return Iterators.transform(new DexEntryIterator(), new Function<DexEntry, DexFile>() {
                    @Nullable @Override public DexFile apply(DexEntry dexEntry) {
                        return dexEntry.getDexFile();
                    }
                });
            }
        };
    }

    @Nonnull @Override public List<String> getDexEntryNames() throws IOException {
        return new AbstractForwardSequentialList<String>() {
            @Override public int size() {
                return oatHeader.getDexFileCount();
            }

            @Nonnull @Override public Iterator<String> iterator() {
                return Iterators.transform(new DexEntryIterator(), new Function<DexEntry, String>() {
                    @Nullable @Override public String apply(DexEntry dexEntry) {
                        return dexEntry.entryName;
                    }
                });
            }
        };
    }

    @Nullable @Override public OatDexFile getEntry(@Nonnull String entryName) throws IOException {
        DexEntryIterator iterator = new DexEntryIterator();
        while (iterator.hasNext()) {
            DexEntry entry = iterator.next();
            if (entry.entryName.equals(entryName)) {
                if (entry.isCompactDex) {
                    throw new RuntimeException("oatFile.getEntry fatal! entry is cdex");
                }
                return (OatDexFile) entry.getDexFile();
            }
        }
        return null;
    }

    public class OatCDexFile extends CDexBackedDexFile implements MultiDexContainer.MultiDexFile {
        @Nonnull public final String filename;

        public OatCDexFile(byte[] buf, int offset, @Nonnull String filename) {
            super(opcodes, buf, offset);
            this.filename = filename;
        }

        @Nonnull @Override public String getEntryName() {
            return filename;
        }

        @Nonnull @Override public OatFile getContainer() {
            return OatFile.this;
        }

        @Override public boolean hasOdexOpcodes() {
            return true;
        }

        @Nonnull
        @Override
        public byte[] getBuf() {
            return super.getBuf();
        }

        public int getOffset() {
            return super.getBaseOffset();
        }
    }

    public class OatDexFile extends DexBackedDexFile implements MultiDexContainer.MultiDexFile {
        @Nonnull public final String filename;

        public OatDexFile(byte[] buf, int offset, @Nonnull String filename) {
            super(opcodes, buf, offset);
            this.filename = filename;
        }

        @Nonnull @Override public String getEntryName() {
            return filename;
        }

        @Nonnull @Override public OatFile getContainer() {
            return OatFile.this;
        }

        @Override public boolean hasOdexOpcodes() {
            return true;
        }

        @Nonnull
        @Override
        public byte[] getBuf() {
            return super.getBuf();
        }

        public int getOffset() {
            return super.getBaseOffset();
        }
    }

    private class OatHeader {
        private final int headerOffset;

        public OatHeader(int offset) {
            this.headerOffset = offset;
        }

        public boolean isValid() {
            for (int i=0; i<OAT_MAGIC.length; i++) {
                if (buf[headerOffset + i] != OAT_MAGIC[i]) {
                    return false;
                }
            }

            for (int i=4; i<7; i++) {
                if (buf[headerOffset + i] < '0' || buf[headerOffset + i] > '9') {
                    return false;
                }
            }

            return buf[headerOffset + 7] == 0;
        }

        public int getVersion() {
            return Integer.valueOf(new String(buf, headerOffset + 4, 3));
        }

        public int getDexFileCount() {
            return readSmallUint(headerOffset + 20);
        }

        public int getOatDexFilesOffset() {
            final int version = getVersion();
            if (version < 127) {
                throw new IllegalStateException("No oat_dex_files_offset defined in this version: " + version);
            }
            return readSmallUint(headerOffset + 24);
        }

        public int getKeyValueStoreSize() {
            final int version = getVersion();
            if (version < MIN_OAT_VERSION) {
                throw new IllegalStateException("Unsupported oat version");
            }
            int uint32Cnt = 17;
            if (version < 52) {
                uint32Cnt += 3; // portable offsets
            }
            if (version >= 127) {
                uint32Cnt += 1; // oat_dex_files_offset_
            }
            int fieldOffset = uint32Cnt * 4;
            return readSmallUint(headerOffset + fieldOffset);
        }

        public int getHeaderSize() {
            final int version = getVersion();
            if (version < MIN_OAT_VERSION) {
                throw new IllegalStateException("Unsupported oat version");
            }
            int uint32Cnt = 18;
            if (version < 52) {
                uint32Cnt += 3; // portable offsets
            }
            if (version >= 127) {
                uint32Cnt += 1; // oat_dex_files_offset_
            }
            return uint32Cnt*4 + getKeyValueStoreSize();
        }

        @Nullable
        public String getKeyValue(@Nonnull String key) {
            int size = getKeyValueStoreSize();

            final int version = getVersion();
            int uint32Cnt = 18;
            if (version < 52) {
                uint32Cnt += 3; // portable offsets
            }
            if (version >= 127) {
                uint32Cnt += 1; // oat_dex_files_offset_
            }

            int offset = headerOffset + uint32Cnt * 4;
            int endOffset = offset + size;

            while (offset < endOffset) {
                int keyStartOffset = offset;
                while (offset < endOffset && buf[offset] != '\0') {
                    offset++;
                }
                if (offset >= endOffset) {
                    throw new InvalidOatFileException("Oat file contains truncated key value store");
                }
                int keyEndOffset = offset;

                String k = new String(buf, keyStartOffset, keyEndOffset - keyStartOffset);
                if (k.equals(key)) {
                    int valueStartOffset = ++offset;
                    while (offset < endOffset && buf[offset] != '\0') {
                        offset++;
                    }
                    if (offset >= endOffset) {
                        throw new InvalidOatFileException("Oat file contains truncated key value store");
                    }
                    int valueEndOffset = offset;
                    return new String(buf, valueStartOffset, valueEndOffset - valueStartOffset);
                }
                offset++;
            }
            return null;
        }

        public int getDexListStart() {
            if (getVersion() >= 127) {
                int r =  headerOffset + getOatDexFilesOffset();
                Xlog.i(TAG, "0 getDexListStart headerOffset " + headerOffset + " r " + r);
                return r;
            } else {
                int r = headerOffset + getHeaderSize();
                Xlog.i(TAG, "1 getDexListStart headerOffset " + headerOffset + " r " + r);
                return r;
            }
        }
    }

    @Nonnull
    private List<SectionHeader> getSections() {
        final int offset;
        final int entrySize;
        final int entryCount;
        if (is64bit) {
            offset = readLongAsSmallUint(40);
            entrySize = readUshort(58);
            entryCount = readUshort(60);
        } else {
            offset = readSmallUint(32);
            entrySize = readUshort(46);
            entryCount = readUshort(48);
        }

        if (offset + (entrySize * entryCount) > buf.length) {
            throw new InvalidOatFileException("The ELF section headers extend past the end of the file");
        }

        return new AbstractList<SectionHeader>() {
            @Override public SectionHeader get(int index) {
                if (index < 0 || index >= entryCount) {
                    throw new IndexOutOfBoundsException();
                }
                if (is64bit) {
                    return new SectionHeader64Bit(offset + (index * entrySize));
                } else {
                    return new SectionHeader32Bit(offset + (index * entrySize));
                }
            }

            @Override public int size() {
                return entryCount;
            }
        };
    }

    @Nonnull
    private SymbolTable getSymbolTable() {
        for (SectionHeader header: getSections()) {
            if (header.getType() == SectionHeader.TYPE_DYNAMIC_SYMBOL_TABLE) {
                return new SymbolTable(header);
            }
        }
        throw new InvalidOatFileException("Oat file has no symbol table");
    }

    @Nonnull
    private StringTable getSectionNameStringTable() {
        int index = readUshort(50);
        if (index == 0) {
            throw new InvalidOatFileException("There is no section name string table");
        }

        try {
            return new StringTable(getSections().get(index));
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidOatFileException("The section index for the section name string table is invalid");
        }
    }

    private abstract class SectionHeader {
        protected final int offset;
        public static final int TYPE_DYNAMIC_SYMBOL_TABLE = 11;
        public SectionHeader(int offset) { this.offset = offset; }
        @Nonnull public String getName() { return getSectionNameStringTable().getString(readSmallUint(offset)); }
        public int getType() { return readInt(offset + 4); }
        public abstract long getAddress();
        public abstract int getOffset();
        public abstract int getSize();
        public abstract int getLink();
        public abstract int getEntrySize();
    }

    private class SectionHeader32Bit extends SectionHeader {
        public SectionHeader32Bit(int offset) { super(offset); }
        @Override public long getAddress() { return readInt(offset + 12) & 0xFFFFFFFFL; }
        @Override public int getOffset() { return readSmallUint(offset + 16); }
        @Override public int getSize() { return readSmallUint(offset + 20); }
        @Override public int getLink() { return readSmallUint(offset + 24); }
        @Override public int getEntrySize() { return readSmallUint(offset + 36); }
    }

    private class SectionHeader64Bit extends SectionHeader {
        public SectionHeader64Bit(int offset) { super(offset); }
        @Override public long getAddress() { return readLong(offset + 16); }
        @Override public int getOffset() { return readLongAsSmallUint(offset + 24); }
        @Override public int getSize() { return readLongAsSmallUint(offset + 32); }
        @Override public int getLink() { return readSmallUint(offset + 40); }
        @Override public int getEntrySize() { return readLongAsSmallUint(offset + 56); }
    }

    class SymbolTable {
        @Nonnull private final StringTable stringTable;
        private final int offset;
        private final int entryCount;
        private final int entrySize;

        public SymbolTable(@Nonnull SectionHeader header) {
            try {
                this.stringTable = new StringTable(getSections().get(header.getLink()));
            } catch (IndexOutOfBoundsException ex) {
                throw new InvalidOatFileException("String table section index is invalid");
            }
            this.offset = header.getOffset();
            this.entrySize = header.getEntrySize();
            this.entryCount = header.getSize() / entrySize;

            if (offset + entryCount * entrySize > buf.length) {
                throw new InvalidOatFileException("Symbol table extends past end of file");
            }
        }

        @Nonnull
        public List<Symbol> getSymbols() {
            return new AbstractList<Symbol>() {
                @Override public Symbol get(int index) {
                    if (index < 0 || index >= entryCount) {
                        throw new IndexOutOfBoundsException();
                    }
                    if (is64bit) {
                        return new Symbol64(offset + index * entrySize);
                    } else {
                        return new Symbol32(offset + index * entrySize);
                    }
                }

                @Override public int size() {
                    return entryCount;
                }
            };
        }

        public abstract class Symbol {
            protected final int offset;
            public Symbol(int offset) { this.offset = offset; }
            @Nonnull public abstract String getName();
            public abstract long getValue();
            public abstract int getSize();
            public abstract int getSectionIndex();

            public int getFileOffset() {
                SectionHeader sectionHeader;
                try {
                    sectionHeader = getSections().get(getSectionIndex());
                } catch (IndexOutOfBoundsException ex) {
                    throw new InvalidOatFileException("Section index for symbol is out of bounds");
                }

                long sectionAddress = sectionHeader.getAddress();
                int sectionOffset = sectionHeader.getOffset();
                int sectionSize = sectionHeader.getSize();

                long symbolAddress = getValue();

                if (symbolAddress < sectionAddress || symbolAddress >= sectionAddress + sectionSize) {
                    throw new InvalidOatFileException("symbol address lies outside it's associated section");
                }

                long fileOffset = (sectionOffset + (getValue() - sectionAddress));
                assert fileOffset <= Integer.MAX_VALUE;
                return (int)fileOffset;
            }
        }

        public class Symbol32 extends Symbol {
            public Symbol32(int offset) { super(offset); }

            @Nonnull
            public String getName() { return stringTable.getString(readSmallUint(offset)); }
            public long getValue() { return readSmallUint(offset + 4); }
            public int getSize() { return readSmallUint(offset + 8); }
            public int getSectionIndex() { return readUshort(offset + 14); }
        }

        public class Symbol64 extends Symbol {
            public Symbol64(int offset) { super(offset); }

            @Nonnull
            public String getName() { return stringTable.getString(readSmallUint(offset)); }
            public long getValue() { return readLong(offset + 8); }
            public int getSize() { return readLongAsSmallUint(offset + 16); }
            public int getSectionIndex() { return readUshort(offset + 6); }
        }
    }

    private class StringTable {
        private final int offset;
        private final int size;

        public StringTable(@Nonnull SectionHeader header) {
            this.offset = header.getOffset();
            this.size = header.getSize();

            if (offset + size > buf.length) {
                throw new InvalidOatFileException("String table extends past end of file");
            }
        }

        @Nonnull
        public String getString(int index) {
            if (index >= size) {
                throw new InvalidOatFileException("String index is out of bounds");
            }

            int start = offset + index;
            int end = start;
            while (buf[end] != 0) {
                end++;
                if (end >= offset + size) {
                    throw new InvalidOatFileException("String extends past end of string table");
                }
            }

            return new String(buf, start, end-start, Charset.forName("US-ASCII"));
        }
    }

    private class DexEntry {
        public final String entryName;
        public final byte[] buf;
        public final int dexOffset;

        boolean isCompactDex = false;


        public DexEntry(String entryName, byte[] buf, int dexOffset) {
            this.entryName = entryName;
            this.buf = buf;
            this.dexOffset = dexOffset;
        }


        public DexEntry(String entryName, byte[] buf, int dexOffset, boolean isCdex) {
            this.entryName = entryName;
            this.buf = buf;
            this.dexOffset = dexOffset;
            isCompactDex = isCdex;
        }

        public DexFile getDexFile() {
            if (isCompactDex) return new OatCDexFile(buf, dexOffset, entryName);
            return new OatDexFile(buf, dexOffset, entryName);
        }
    }

    private class DexEntryIterator implements Iterator<DexEntry> {
        int index = 0;
        int offset = oatHeader.getDexListStart();

        @Override public boolean hasNext() {
            return index < oatHeader.getDexFileCount();
        }

        @Override public DexEntry next() {
            Xlog.i(TAG, "next offset " + offset);
            int filenameLength = readSmallUint(offset);
            offset += 4;   // dex_file_location_size_

            // TODO: what is the correct character encoding?
            String filename = new String(buf, offset, filenameLength, Charset.forName("US-ASCII"));
            offset += filenameLength;  // dex_file_location_data_

            offset += 4; // dex_file_location_checksum_

            Xlog.i(TAG, "next read dexOffset from " + offset);
            int dexOffset = readSmallUint(offset);
            offset += 4;  // dex_file_offset_

            Xlog.i(TAG, "getOatVersion " + getOatVersion());
            byte[] buf;
            if (getOatVersion() >= 87 && vdexProvider != null && vdexProvider.getVdex() != null) {
                buf = vdexProvider.getVdex();
            } else {
                buf = OatFile.this.buf;
                dexOffset += oatHeader.headerOffset;
            }

            if (getOatVersion() >= 75) {
                offset += 4; // class_offsets_offset_
            }
            if (getOatVersion() >= 73) {
                offset += 4; // lookup_table_offset_
            }
            if (getOatVersion() >= 127) {
                offset += 4; // method_bss_mapping_offset_
            }
            if (getOatVersion() >= 131) {
                offset += 4; // dex_sections_layout_offset_
                offset += 4; // method_bss_mapping_offset_
                offset += 4; // dex_sections_layout_offset_
            }
            if (getOatVersion() < 75) {
                // prior to 75, the class offsets are included here directly
                int classCount = readSmallUint(dexOffset + HeaderItem.CLASS_COUNT_OFFSET);
                offset += 4 * classCount;
            }

            index++;

            if (getOatVersion() >= 138) {
                Xlog.i(TAG, "filename " + filename + ", dexOffset " + dexOffset);
                // Xlog.i(TAG, "filename", new Throwable("dexOffset"));
                return new DexEntry(filename, buf, dexOffset, true);
            }

            return new DexEntry(filename, buf, dexOffset);
        }

        @Override public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static class InvalidOatFileException extends RuntimeException {
        public InvalidOatFileException(String message) {
            super(message);
        }
    }

    public static class NotAnOatFileException extends RuntimeException {
        public NotAnOatFileException() {}
    }

    public interface VdexProvider {
        @Nullable
        byte[] getVdex();
    }
}