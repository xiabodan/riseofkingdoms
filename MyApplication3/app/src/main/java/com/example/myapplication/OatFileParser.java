package com.example.myapplication;


import android.annotation.TargetApi;
import android.os.Build;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.BaseDexBuffer;
import org.jf.dexlib2.dexbacked.BaseDexReader;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.dexbacked.DexBackedMethodImplementation;
import org.jf.dexlib2.dexbacked.OatFile;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction10x;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction22cs;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction35ms;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction3rms;
import org.jf.dexlib2.dexbacked.raw.HeaderItem;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.util.DexUtil;
import org.jf.dexlib2.writer.io.MemoryDataStore;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Adler32;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class OatFileParser {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_VV = true;
    public static final String TAG = OatFileParser.class.getSimpleName();

    private static final byte[] DEX_MAGIC = new byte[] { 'd', 'e', 'x', '\n' };

    static class DexHeader {
        static final int SIGNATURE_OFFSET = 12;
        static final int SIGNATURE_SIZE = 20;
        static final int SIGNATURE_DATA_START_OFFSET = 32;

        static final int CHECKSUM_DATA_START_OFFSET = 12;

        static final int FILE_SIZE_OFFSET = 32;
    }

    private static boolean updateDexSignatureSha1(byte[] buf, int off, int len) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (digest == null) {
            return false;
        }

        digest.update(buf, off + DexHeader.SIGNATURE_DATA_START_OFFSET, len - DexHeader.SIGNATURE_DATA_START_OFFSET);

        try {
            if (digest.digest(buf, off + DexHeader.SIGNATURE_OFFSET, DexHeader.SIGNATURE_SIZE) == DexHeader.SIGNATURE_SIZE) {
                return true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static Utils.DexEntry convertToDexEntry(String entryName, OatFile.OatCDexFile dexFile) throws Exception {
        //initialCapacity simlilar to CompactDexFile size. Only init here not accurate size.
        int offset = dexFile.getOffset();
        byte[] buffer = dexFile.getBuf();
        Xlog.i(TAG, "entryName " + entryName + " buffer:" + buffer[offset] + buffer[offset+1] + buffer[offset+2] + buffer[offset+3]);
        MemoryDataStore memoryStore = new MemoryDataStore(dexFile.getBuf().length - dexFile.getOffset());
        Xlog.i(TAG, "entryName " + entryName + " dexFile.getOffset " + dexFile.getOffset());
        DexFileFactory.writeDexFile(memoryStore, dexFile);
        byte[] buf = memoryStore.getData();
        final BaseDexBufferLite dexBuffer = new BaseDexBufferLite(buf);
        final int fileSizeOff = DexHeader.FILE_SIZE_OFFSET;
        final int fileSize = dexBuffer.readSmallUint(fileSizeOff);
        if (!updateDexSignatureSha1(buf, 0, fileSize)) {
            throw new RuntimeException("error update sha-1");
        }

        Xlog.i(TAG, "entryName " + entryName + " buf:" + buf[0] + buf[1] + buf[2] + buf[3]);   // dex

        Adler32 adler32 = new Adler32();
        adler32.update(buf, DexHeader.CHECKSUM_DATA_START_OFFSET, fileSize - DexHeader.CHECKSUM_DATA_START_OFFSET);
        final int checkSum = (int)adler32.getValue();
        buf[8] = (byte)checkSum;
        buf[9] = (byte)(checkSum >> 8);
        buf[10] = (byte)(checkSum >> 16);
        buf[11] = (byte)(checkSum >> 24);

        return new Utils.DexEntry(entryName, buf, 0, fileSize);
    }

    private static Utils.DexEntry convertToDexEntry(String entryName, OatFile.OatDexFile dexFile) throws Exception {
        if (DEBUG) Xlog.d(TAG, "getDexEntry: entryName %s:%s, off %s",
                dexFile.getEntryName(), entryName, dexFile.getOffset());

        final int off = dexFile.getOffset();
        final byte[] buf = dexFile.getBuf();

        for (int i = 0; i < DEX_MAGIC.length; i++) {
            if (buf[off + i] != DEX_MAGIC[i]) {
                final String msg = String.format("invalid dex magic: magic[%d] = %s, expect %s",
                        i, buf[off + i], DEX_MAGIC[i]);
                throw new RuntimeException(msg);
            }
        }

        final BaseDexBufferLite dexBuffer = new BaseDexBufferLite(buf);
        final int fileSizeOff = off + DexHeader.FILE_SIZE_OFFSET;
        final int fileSize = dexBuffer.readSmallUint(fileSizeOff);
        if (fileSize < 0) {
            throw new RuntimeException("invalid dexfile length");
        }

        if (DEBUG) Xlog.d(TAG, "getDexEntry: %s:%s size %s", dexFile.getEntryName(), entryName, fileSize);

        if (!updateDexSignatureSha1(buf, off, fileSize)) {
            throw new RuntimeException("error update sha-1");
        }

        Adler32 adler32 = new Adler32();
        adler32.update(buf, off + DexHeader.CHECKSUM_DATA_START_OFFSET, fileSize - DexHeader.CHECKSUM_DATA_START_OFFSET);
        final int checkSum = (int)adler32.getValue();
        buf[off + 8] = (byte)checkSum;
        buf[off + 9] = (byte)(checkSum >> 8);
        buf[off + 10] = (byte)(checkSum >> 16);
        buf[off + 11] = (byte)(checkSum >> 24);

        return new Utils.DexEntry(entryName, buf, off, fileSize);
    }

    private static void recoveryOatDexFile(DexFile dexFile, BaseDexReader quickenedInfoReader) {
        if (quickenedInfoReader == null) {
            return;
        }
        OatFile.OatDexFile oatDexFile = dexFile instanceof OatFile.OatDexFile ? (OatFile.OatDexFile)dexFile : null;
        for (DexBackedClassDef classDef : oatDexFile.getClasses()) {
            for (DexBackedMethod dirMethod : classDef.getDirectMethods()) {
                final DexBackedMethodImplementation implementation = dirMethod.getImplementation();
                if (implementation == null) {
                    continue;
                }
                final int quickenedInfoLen = quickenedInfoReader.readInt();
                if (quickenedInfoLen == 0) {
                    continue;
                }
                final int nextMethodInfoOff = quickenedInfoReader.getOffset() + quickenedInfoLen;
                final DexDecompiler dexDecompiler = new DexDecompiler(implementation, quickenedInfoReader, nextMethodInfoOff);
                dexDecompiler.decompile();
                quickenedInfoReader.setOffset(nextMethodInfoOff);
            }
            for (DexBackedMethod virMethod : classDef.getVirtualMethods()) {
                final DexBackedMethodImplementation implementation = virMethod.getImplementation();
                if (implementation == null) {
                    continue;
                }
                final int quickeningLen = quickenedInfoReader.readInt();
                if (quickeningLen == 0) {
                    continue;
                }
                final int nextMethodInfoOff = quickenedInfoReader.getOffset() + quickeningLen;
                final DexDecompiler dexDecompiler = new DexDecompiler(implementation, quickenedInfoReader, nextMethodInfoOff);
                dexDecompiler.decompile();
                quickenedInfoReader.setOffset(nextMethodInfoOff);
            }
        }
    }

    private static void recoveryOatDexFileV8(DexFile dexFile, VdexFile vdexFile, int dexIdx) {
        if (vdexFile == null) {
            return;
        }

        OatFile.OatDexFile oatDexFile = dexFile instanceof OatFile.OatDexFile ? (OatFile.OatDexFile)dexFile : null;
        final QuickeningInfoIterator iterator = new QuickeningInfoIterator(dexIdx, vdexFile);
        for (DexBackedClassDef classDef : oatDexFile.getClasses()) {
            for (DexBackedMethod dirMethod : classDef.getDirectMethods()) {
                final DexBackedMethodImplementation implementation = dirMethod.getImplementation();
                if (implementation == null) {
                    continue;
                }
                if (DEBUG_VV && false) {
                    Xlog.v(TAG, "method: %s, off: %s vs. %s", dirMethod,
                            implementation.codeOffset, iterator.GetCurrentCodeItemOffset());
                }
                if (!iterator.Done() && implementation.codeOffset == iterator.GetCurrentCodeItemOffset()) {
                    final BaseDexReader quickenedInfoReader = iterator.GetCurrentQuickeningInfo();
                    final int length = quickenedInfoReader.readInt();
                    final DexDecompilerV7 dexDecompiler = new DexDecompilerV7(implementation,
                            quickenedInfoReader, quickenedInfoReader.getOffset() + length);
                    dexDecompiler.decompile();
                    iterator.Advance();
                }
            }
            for (DexBackedMethod virMethod : classDef.getVirtualMethods()) {
                final DexBackedMethodImplementation implementation = virMethod.getImplementation();
                if (implementation == null) {
                    continue;
                }
                if (DEBUG_VV && false) {
                    Xlog.v(TAG, "method: %s, off: %s vs. %s", virMethod,
                            implementation.codeOffset, iterator.GetCurrentCodeItemOffset());
                }
                if (!iterator.Done() && implementation.codeOffset == iterator.GetCurrentCodeItemOffset()) {
                    final BaseDexReader quickenedInfoReader = iterator.GetCurrentQuickeningInfo();
                    final int length = quickenedInfoReader.readInt();
                    final DexDecompilerV7 dexDecompiler = new DexDecompilerV7(implementation,
                            quickenedInfoReader, quickenedInfoReader.getOffset() + length);
                    dexDecompiler.decompile();
                    iterator.Advance();
                }
            }
        }
    }

    private static void recoveryOatDexFileV19(DexFile dexFile, VdexFileP vdexFileP, int dexIdx) {
        if (vdexFileP == null) {
            return;
        }
        int dexBegin = vdexFileP.getNextDexFileData(0);
        for (int i = 0; i < dexIdx; i++) {
            dexBegin = vdexFileP.getNextDexFileData(dexBegin);
        }
        Xlog.i(TAG, "recoveryOatDexFileV19 dexBegin " + dexBegin);
        int quickenInfoOffsetTable = vdexFileP.getQuickenInfoOffsetTable(dexBegin);
        Xlog.i(TAG, "recoveryOatDexFileV19 quickenInfoOffsetTable " + quickenInfoOffsetTable);
        DexUtil.CompactAccessor accessor = vdexFileP.getCompactAccessor(quickenInfoOffsetTable);

        if (dexFile instanceof OatFile.OatCDexFile) {
            OatFile.OatCDexFile cdexFile = (OatFile.OatCDexFile)dexFile;
            Xlog.i(TAG, "cdexFile class size " + cdexFile.getClasses().size());
            for (DexBackedClassDef classDef : cdexFile.getClasses()) {
                for (DexBackedMethod dirMethod : classDef.getDirectMethods()) {
                    final DexBackedMethodImplementation implementation = dirMethod.getImplementation();
                    if (implementation == null) {
                        continue;
                    }
                    int offset = accessor.getOffset(dirMethod.getMethodIndex());
                    if (offset != 0) {
                        final BaseDexReader quickenedInfoReader = vdexFileP.getQuickenInfoAt(offset);
                        final int length = quickenedInfoReader.readBigUleb128();
                        final DexDecompilerV7 dexDecompiler = new DexDecompilerV7(implementation,
                                quickenedInfoReader, quickenedInfoReader.getOffset() + length*2);
                        dexDecompiler.decompile();
                    }
                }

                for (DexBackedMethod virMethod : classDef.getVirtualMethods()) {
                    final DexBackedMethodImplementation implementation = virMethod.getImplementation();
                    if (implementation == null) {
                        continue;
                    }
                    int offset = accessor.getOffset(virMethod.getMethodIndex());
                    if (offset != 0) {
                        final BaseDexReader quickenedInfoReader = vdexFileP.getQuickenInfoAt(offset);
                        final int length = quickenedInfoReader.readBigUleb128();
                        final DexDecompilerV7 dexDecompiler = new DexDecompilerV7(implementation,
                                quickenedInfoReader, quickenedInfoReader.getOffset() + length*2);
                        dexDecompiler.decompile();
                    }
                }
            }
        }
    }

    public static List<Utils.DexEntry> getDexEntries(final File odexFile64) {
        try {
            final ArrayList<Utils.DexEntry> entries = new ArrayList<Utils.DexEntry>();
            final DexFile dexFile = DexFileFactory.loadDexFile(odexFile64,
                    Opcodes.forApi(Build.VERSION.SDK_INT));

            OatFile oatFile = null;
            if (dexFile instanceof OatFile.OatDexFile) {
                oatFile = ((OatFile.OatDexFile) dexFile).getContainer();
            } else if (dexFile instanceof OatFile.OatCDexFile) {
                oatFile = ((OatFile.OatCDexFile) dexFile).getContainer();
            }

            BaseDexReader quickenedInfoReader = null;
            VdexFile vdexFile = null;
            VdexFileP vdexFileP = null;
            try {
                vdexFile = new VdexFile(((OatFile.OatDexFile)dexFile).getBuf());
                if (vdexFile.getVersion() < 8) {
                    quickenedInfoReader = vdexFile.getQuickeningInfo();
                }
            } catch (ClassCastException e) {
                try {
                    vdexFileP = new VdexFileP(((OatFile.OatCDexFile)dexFile).getBuf());
                } catch (Exception err) {
                    if (DEBUG) err.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Xlog.w(TAG, "getDexEntries: getQuickeningInfo", e);
                } else {
                    e.printStackTrace();
                }
            }

            final List<DexFile> oatDexFiles = oatFile.getDexFiles();
            for (int i = 0; i < oatDexFiles.size(); i++) {
                Xlog.i(TAG, i + "+++++++++++++++++ ");
                final DexFile oatDexFile = oatDexFiles.get(i);
                final String entryName = "classes" + (i == 0 ? "" : (i + 1)) + ".dex";
                Xlog.i(TAG, "entryName " + entryName);
                if (vdexFile != null) {
                    try {
                        if (quickenedInfoReader != null) {
                            recoveryOatDexFile(oatDexFile, quickenedInfoReader);
                        } else if (vdexFile.getVersion() >= 8) {
                            recoveryOatDexFileV8(oatDexFile, vdexFile, i);
                        }
                    } catch (Exception e) {
                        if (DEBUG) {
                            Xlog.e(TAG, "getDexEntries: recoveryOatDexFile", e);
                        } else {
                            e.printStackTrace();
                        }
                    }
                } else if (vdexFileP != null) {
                    try {
                        recoveryOatDexFileV19(oatDexFile, vdexFileP, i);
                    } catch (Exception e) {
                        if (DEBUG) {
                            Xlog.e(TAG, "getDexEntries: recoveryOatDexFileV19", e);
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
                if (vdexFileP != null) {
                    entries.add(convertToDexEntry(entryName, (OatFile.OatCDexFile)oatDexFile));
                } else {
                    entries.add(convertToDexEntry(entryName, (OatFile.OatDexFile)oatDexFile));
                }
            }

            return entries;
        } catch (Exception e) {
            if (DEBUG) {
                Xlog.e(TAG, "getDexEntries: ", e);
            } else {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static class VdexFile {
        private static final byte[] VDEX_MAGIC = new byte[] { 'v', 'd', 'e', 'x' };
        private static final int MIN_VDEX_VERSION  = 1;

        final byte[] mBuf;
        final BaseDexReader mDexReader;
        final int mVdexVersion;
        final int mNumberOfDexfiles;
        final int mDexSize;
        final int mVerifierDepsSize;
        final int mQuickeningInfoSize;

        VdexFile(byte[] buf) {
            mBuf = buf;
            mDexReader = new BaseDexReader(new BaseDexBuffer(buf, 0), 0);
            for (int i = 0; i < VDEX_MAGIC.length; i++) {
                if (VDEX_MAGIC[i] != mDexReader.readByte()) {
                    throw new RuntimeException("Invalid vdex file!");
                }
            }
            mVdexVersion = Integer.parseInt(new String(buf, VDEX_MAGIC.length, 3));
            if (mVdexVersion < MIN_VDEX_VERSION) {
                throw new RuntimeException("Invalid vdex version " + mVdexVersion);
            }
            mDexReader.moveRelative(4); // skip version.
            mNumberOfDexfiles = mDexReader.readInt();
            mDexSize = mDexReader.readInt();
            mVerifierDepsSize = mDexReader.readInt();
            mQuickeningInfoSize = mDexReader.readInt();
            if (DEBUG) {
                Xlog.i(TAG, "VdexFile: version %s, %s:%s:%s:%s",
                        mVdexVersion, mNumberOfDexfiles, mDexSize, mVerifierDepsSize, mQuickeningInfoSize);
            }
        }

        BaseDexReader getQuickeningInfo() {
            // headerSize + ChecksumSize + dexSize + depsSize
            final int offset = mDexReader.getOffset()
                    + 4 * mNumberOfDexfiles
                    + mDexSize
                    + mVerifierDepsSize;
            if (offset >= mBuf.length) {
                throw new RuntimeException("Invalid quickeningInfo offset: " + offset);
            }
            return new BaseDexReader(mDexReader.dexBuf, offset);
        }

        int getVersion() {
            return mVdexVersion;
        }

        int getDexCount() {
            return mNumberOfDexfiles;
        }

        int getQuickeningInfoSize() {
            return mQuickeningInfoSize;
        }

        byte[] getBuf() {
            return mBuf;
        }
    }

    static class QuickeningInfoIterator {
        final VdexFile mVdexFile;
        final BaseDexReader mQuickeningInfoReader;
        final int mCurrentCodeItemEnd;
        int mCurrentCodeItemOff;

        QuickeningInfoIterator(int dexFileIndex, VdexFile vdexFile) {
            mVdexFile = vdexFile;
            mQuickeningInfoReader = vdexFile.getQuickeningInfo();

            final int baseOffset = mQuickeningInfoReader.getOffset();
            int dexFileIndicesOff = vdexFile.getQuickeningInfoSize() - vdexFile.getDexCount() * 4;
            if ((baseOffset + dexFileIndicesOff) > vdexFile.getBuf().length) {
                throw new RuntimeException("Invalid quickeningInfo size: " + vdexFile.getQuickeningInfoSize());
            }
            if (DEBUG) Xlog.v(TAG, "dexFileIndicesOff: %s", Integer.toHexString(dexFileIndicesOff));
            mCurrentCodeItemEnd = dexFileIndex == (vdexFile.getDexCount() - 1) ?
                    dexFileIndicesOff : mQuickeningInfoReader.readInt(baseOffset + dexFileIndicesOff + (dexFileIndex + 1) * 4);
            mCurrentCodeItemOff = mQuickeningInfoReader.readInt(baseOffset + dexFileIndicesOff + dexFileIndex * 4);
            if (DEBUG) {
                Xlog.d(TAG, "QuickeningInfoIterator: dexIdx %s, codeItemOff %s, codeItemEnd %s",
                        dexFileIndex,
                        Integer.toHexString(mCurrentCodeItemOff),
                        Integer.toHexString(mCurrentCodeItemEnd));
            }
        }

        boolean Done() {
            return mCurrentCodeItemOff >= mCurrentCodeItemEnd;
        }

        void Advance() {
            mCurrentCodeItemOff += 8;
        }

        int GetCurrentCodeItemOffset() {
            return mQuickeningInfoReader.readInt(mQuickeningInfoReader.getOffset() + mCurrentCodeItemOff);
        }

        BaseDexReader GetCurrentQuickeningInfo() {
            final int offset = mQuickeningInfoReader.readInt(mQuickeningInfoReader.getOffset() + mCurrentCodeItemOff + 4);
            if (mQuickeningInfoReader.getOffset() + offset >= mVdexFile.getBuf().length) {
                throw new RuntimeException("Invalid quickening info offset: " + offset);
            }
            return new BaseDexReader(mQuickeningInfoReader.dexBuf, mQuickeningInfoReader.getOffset() + offset);
        }
    };

    private static class VdexFileP {
        private static final byte[] VERIFIER_DEPS_MAGIC = new byte[] { 'v', 'd', 'e', 'x' };
        private static final int Q_VDEX_VERSION = 21;
        byte[] mBuf;
        BaseDexReader mDexReader;
        int quickeningInfoBegin;
        VerifierDepsHeaderP mVerifierDepsHeaderP;
        DexSectionHeaderP mDexSectionHeaderP;
        int dexBegin;

        public class VerifierDepsHeaderP {
            int verifierDepsVersion;
            int dexSectionVersion;
            int numberOfDexFiles;
            int verifierDepsSize;
            //android Q
            int bootclasspathChecksumsSize;
            int classLoaderContextSize;

            public VerifierDepsHeaderP() {
                for (int i = 0; i < VERIFIER_DEPS_MAGIC.length; i++) {
                    if (VERIFIER_DEPS_MAGIC[i] != mDexReader.readByte()) {
                        throw new RuntimeException("Invalid vdex file!");
                    }
                }
                verifierDepsVersion = Integer.parseInt(new String(mBuf, 4, 3));
                dexSectionVersion = Integer.parseInt(new String(mBuf, 8, 3));
                mDexReader.moveRelative(8);
                numberOfDexFiles = mDexReader.readInt();
                verifierDepsSize = mDexReader.readInt();
                if (verifierDepsVersion >= Q_VDEX_VERSION) {
                    bootclasspathChecksumsSize = mDexReader.readInt();
                    classLoaderContextSize = mDexReader.readInt();
                }
                mDexReader.moveRelative(4*numberOfDexFiles);
            }
            boolean hasDexSection() {
                return dexSectionVersion > 0;
            }
        }

        public class DexSectionHeaderP {
            int dexSize;
            int dexSharedDataSize;
            int quickeningInfoSize;

            public DexSectionHeaderP() {
                dexSize = mDexReader.readInt();
                dexSharedDataSize = mDexReader.readInt();
                quickeningInfoSize = mDexReader.readInt();
            }
        }

        public VdexFileP(byte[] buf) {
            mBuf = buf;
            mDexReader = new BaseDexReader(new BaseDexBuffer(buf, 0), 0);
            mVerifierDepsHeaderP = new VerifierDepsHeaderP();
            if (mVerifierDepsHeaderP.hasDexSection()) {
                mDexSectionHeaderP = new DexSectionHeaderP();
                dexBegin = mDexReader.getOffset();
                Xlog.i(TAG, "VdexFileP dexBegin " + dexBegin);
                quickeningInfoBegin = dexBegin + mDexSectionHeaderP.dexSize +
                        mDexSectionHeaderP.dexSharedDataSize + mVerifierDepsHeaderP.verifierDepsSize;
                Xlog.i(TAG, "VdexFileP quickeningInfoBegin " + quickeningInfoBegin);
            }
        }

        int getVersion() {
            return mVerifierDepsHeaderP.verifierDepsVersion;
        }

        byte[] getBuf() {
            return mBuf;
        }

        BaseDexReader getQuickenInfoAt(int offset) {
            return new BaseDexReader(mDexReader.dexBuf,quickeningInfoBegin + (offset - 1));
        }

        int getQuickenInfoOffsetTable(int sourceDexBegin) {
            int quickeningInfoOffset = 0;
            quickeningInfoOffset = mDexReader.readInt(sourceDexBegin - 4);
            /*
            if (sourceDexBegin == 60) {   // 0x3c
                quickeningInfoOffset = mDexReader.readInt(sourceDexBegin - 4);  // 0x38
            } else if (sourceDexBegin == 1688660){  //  0x19c454
                quickeningInfoOffset = mDexReader.readInt(sourceDexBegin);              // 0x19c454
            } else if (sourceDexBegin == 2983556) {  //  0x2d8684
                quickeningInfoOffset = mDexReader.readInt(sourceDexBegin + 4);  //  0x2d8688
            } else {
                quickeningInfoOffset = mDexReader.readInt(sourceDexBegin + 8);  //  0x484784
            }*/
            Xlog.i(TAG, "sourceDexBegin " + sourceDexBegin + " quickeningInfoBegin " + quickeningInfoBegin + " quickeningInfoOffset " + quickeningInfoOffset);
            return quickeningInfoBegin + quickeningInfoOffset;
        }

        DexUtil.CompactAccessor getCompactAccessor(int dataBegin) {
            return new DexUtil.CompactAccessor(mDexReader, dataBegin);
        }

        int getNextDexFileData(int off) {
            if (off < 0 || off > mBuf.length) {
                throw new RuntimeException("Invalid dexfile offset: " + off);
            }
            if (off == 0) {
                Xlog.i(TAG, "getNextDexFileData dexBegin " + dexBegin);
                return mVerifierDepsHeaderP.hasDexSection() ? dexBegin + 4 : 0;
            } else {
                //align(4)
                Xlog.i(TAG, "getNextDexFileData off " + off);
                int dexFileSize = 0;
                /*
                if (off == 60) {  // 0x3c
                    dexFileSize = mDexReader.readInt(off + HeaderItem.FILE_SIZE_OFFSET);      // 0x3c
                } else if (off == 1688660) {  //  0x19c454
                    dexFileSize = mDexReader.readInt(off + HeaderItem.FILE_SIZE_OFFSET + 4);  // 0x19c458
                } else if (off == 2983556) {  //  0x2d8684
                    dexFileSize = mDexReader.readInt(off + HeaderItem.FILE_SIZE_OFFSET + 8);  // 0x2d868c
                } else {
                }*/
                dexFileSize = mDexReader.readInt(off + HeaderItem.FILE_SIZE_OFFSET);
                int offset = off + dexFileSize;
                Xlog.i(TAG, "getNextDexFileData dexFileSize " + dexFileSize);

                if (offset%4 == 1) {
                    offset += 3;
                } else if (offset%4 == 2) {
                    offset += 2;
                } else if (offset%4 == 3) {
                    offset += 1;
                }
                Xlog.i(TAG, "getNextDexFileData offset " + offset);
                return offset == dexBegin + mDexSectionHeaderP.dexSize ? 0 : offset + 4;
            }
        }
    }

    private static class DexDecompiler {
        private final DexBackedMethodImplementation mMethodImplementation;
        private final BaseDexReader mQuickenedInfoReader;
        private final int mInfoStartOff;
        private final int mInfoEndOff;

        DexDecompiler(DexBackedMethodImplementation methodImplementation, BaseDexReader quickenedInfoReader, int infoEndOff) {
            mMethodImplementation = methodImplementation;
            mQuickenedInfoReader = quickenedInfoReader;
            mInfoStartOff = quickenedInfoReader.getOffset();
            mInfoEndOff = infoEndOff;
        }

        void decompileNop(Instruction inst, int dexPc, Iterator<? extends Instruction> instsIterator) {
            final int curOff = mQuickenedInfoReader.getOffset();
            if (curOff >= mInfoEndOff) {
                return;
            }
            final int quickenedPc = mQuickenedInfoReader.readLargeUleb128();
            mQuickenedInfoReader.setOffset(curOff);
            if (quickenedPc != dexPc) {
                return;
            }
            final int referenceIndex = getIndexAt(dexPc, "Nop0");
            final int typeIndex = getIndexAt(dexPc, "Nop1");
            if (DEBUG_VV && false) {
                Xlog.v(TAG, "%s: refIdx %s, typeIdx %s", inst.getOpcode(), referenceIndex, typeIndex);
            }
            final Instruction nextInst = instsIterator.next();
            if (nextInst == null || nextInst.getOpcode() != Opcode.NOP) {
                throw new RuntimeException(String.format("NOP's next inst (%s) isn't NOP! ", nextInst.getOpcode()));
            }
            if (inst instanceof DexBackedInstruction10x) {
                ((DexBackedInstruction10x)inst).setOpcode(Opcode.CHECK_CAST);
                ((DexBackedInstruction10x)inst).setRefIdx(referenceIndex);
                ((DexBackedInstruction10x)inst).setTypeIdx(typeIndex);
            }
        }

        void decompileInstanceFieldAccess(Instruction inst, int dexPc, Opcode newOp) {
            final int index = getIndexAt(dexPc, "FieldAccess");
            if (inst instanceof DexBackedInstruction22cs) {
                ((DexBackedInstruction22cs)inst).setOpcode(newOp);
                ((DexBackedInstruction22cs)inst).setFieldOffset(index);
            }
        }

        void decompileInvokeVirtual(Instruction inst, int dexPc, Opcode newOp) {
            final int index = getIndexAt(dexPc, "InvokeVirtual");
            if (inst instanceof DexBackedInstruction35ms) {
                ((DexBackedInstruction35ms)inst).setOpcode(newOp);
                ((DexBackedInstruction35ms)inst).setVtableIndex(index);
            } else if (inst instanceof DexBackedInstruction3rms) {
                ((DexBackedInstruction3rms)inst).setOpcode(newOp);
                ((DexBackedInstruction3rms)inst).setVtableIndex(index);
            }
        }

        int getIndexAt(int dexPc, String tag) {
            final int quickenedPc = mQuickenedInfoReader.readLargeUleb128();
            final int index = mQuickenedInfoReader.readLargeUleb128();
            if (quickenedPc != dexPc) {
                throw new RuntimeException(String.format("PC mismatch @ %s: %s vs. %s", tag, quickenedPc, dexPc));
            }
            return index;
        }

        boolean decompile() {
            final Iterator<DexBackedInstruction> instsIterator =
                    (Iterator<DexBackedInstruction>)mMethodImplementation.getInstructions().iterator();
            int insStart = -1;
            while (instsIterator.hasNext()) {
                final DexBackedInstruction instruction = instsIterator.next();
                if (insStart < 0) {
                    insStart = instruction.instructionStart;
                }
                switch (instruction.getOpcode()) {
                    case NOP: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileNop(instruction, dexPc, instsIterator);
                    } break;
                    case IGET_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IGET);
                    } break;
                    case IGET_WIDE_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IGET_WIDE);
                    } break;
                    case IGET_OBJECT_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IGET_OBJECT);
                    } break;
                    case IGET_BOOLEAN_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IGET_BOOLEAN);
                    } break;
                    case IGET_BYTE_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IGET_BYTE);
                    } break;
                    case IGET_CHAR_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IGET_CHAR);
                    } break;
                    case IGET_SHORT_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IGET_SHORT);
                    } break;
                    case IPUT_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IPUT);
                    } break;
                    case IPUT_WIDE_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IPUT_WIDE);
                    } break;
                    case IPUT_OBJECT_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IPUT_OBJECT);
                    } break;
                    case IPUT_BOOLEAN_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IPUT_BOOLEAN);
                    } break;
                    case IPUT_BYTE_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IPUT_BYTE);
                    } break;
                    case IPUT_CHAR_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IPUT_CHAR);
                    } break;
                    case IPUT_SHORT_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInstanceFieldAccess(instruction, dexPc, Opcode.IPUT_SHORT);
                    } break;
                    case INVOKE_VIRTUAL_QUICK: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInvokeVirtual(instruction, dexPc, Opcode.INVOKE_VIRTUAL);
                    } break;
                    case INVOKE_VIRTUAL_QUICK_RANGE: {
                        final int dexPc = (instruction.instructionStart - insStart) >> 1;
                        decompileInvokeVirtual(instruction, dexPc, Opcode.INVOKE_VIRTUAL_RANGE);
                    } break;
                }
            }

            if (mQuickenedInfoReader.getOffset() != mInfoEndOff) {
                if (mQuickenedInfoReader.getOffset() == mInfoStartOff) {
                    if (DEBUG) Xlog.w(TAG, "Failed to use any value in quickening info, potentially due to duplicate methods.");
                } else {
                    if (DEBUG) Xlog.e(TAG, "Failed to use all values in quickening info. Actual: %s, Expected %s",
                            mQuickenedInfoReader.getOffset(), mInfoEndOff);
                    return false;
                }
            }

            return true;
        }

    }

    private static class DexDecompilerV7 {
        private final DexBackedMethodImplementation mMethodImplementation;
        private final BaseDexReader mQuickenedInfoReader;
        private final int mInfoStartOff;
        private final int mInfoEndOff;

        DexDecompilerV7(DexBackedMethodImplementation methodImplementation, BaseDexReader quickenedInfoReader, int infoEndOff) {
            mMethodImplementation = methodImplementation;
            mQuickenedInfoReader = quickenedInfoReader;
            mInfoStartOff = quickenedInfoReader.getOffset();
            mInfoEndOff = infoEndOff;
            if (DEBUG_VV && false) {
                Xlog.v(TAG, "mInfoStartOff: %s, mInfoEndOff %s, size %s",
                        mInfoStartOff, mInfoEndOff, (mInfoEndOff - mInfoStartOff));
            }
        }

        void decompileNop(Instruction inst, Iterator<? extends Instruction> instsIterator) {
            final int referenceIndex = nextIndex();
            if (DEBUG_VV && false)
                Xlog.v(TAG, "NOP: ref %s", Integer.toHexString(referenceIndex));
            if (referenceIndex == 0xFFFF) {
                // This means it was a normal nop and not a check-cast.
                return;
            }
            final int typeIndex = nextIndex();
            if (DEBUG_VV && false)
                Xlog.v(TAG, "NOP: type %s", Integer.toHexString(typeIndex));
            final Instruction nextInst = instsIterator.next();
            if (nextInst == null || nextInst.getOpcode() != Opcode.NOP) {
                throw new RuntimeException(String.format("NOP's next inst (%s) isn't NOP! ", nextInst.getOpcode()));
            }
            if (inst instanceof DexBackedInstruction10x) {
                ((DexBackedInstruction10x)inst).setOpcode(Opcode.CHECK_CAST);
                ((DexBackedInstruction10x)inst).setRefIdx(referenceIndex);
                ((DexBackedInstruction10x)inst).setTypeIdx(typeIndex);
            }
        }

        void decompileInstanceFieldAccess(Instruction inst, Opcode newOp) {
            final int index = nextIndex();
            if (DEBUG_VV && false)
                Xlog.v(TAG, "FIELD: %s", Integer.toHexString(index));
            if (inst instanceof DexBackedInstruction22cs) {
                ((DexBackedInstruction22cs)inst).setOpcode(newOp);
                ((DexBackedInstruction22cs)inst).setFieldOffset(index);
            }
        }

        void decompileInvokeVirtual(Instruction inst, Opcode newOp) {
            final int index = nextIndex();
            if (DEBUG_VV && false)
                Xlog.v(TAG, "VIRTUALMETHOD: %s", Integer.toHexString(index));
            if (inst instanceof DexBackedInstruction35ms) {
                ((DexBackedInstruction35ms)inst).setOpcode(newOp);
                ((DexBackedInstruction35ms)inst).setVtableIndex(index);
            } else if (inst instanceof DexBackedInstruction3rms) {
                ((DexBackedInstruction3rms)inst).setOpcode(newOp);
                ((DexBackedInstruction3rms)inst).setVtableIndex(index);
            }
        }

        int nextIndex() {
            return mQuickenedInfoReader.readUshort();
        }

        boolean decompile() {
            final Iterator<DexBackedInstruction> instsIterator =
                    (Iterator<DexBackedInstruction>)mMethodImplementation.getInstructions().iterator();
            while (instsIterator.hasNext()) {
                final DexBackedInstruction instruction = instsIterator.next();
                switch (instruction.getOpcode()) {
                    case SPARSE_SWITCH_PAYLOAD:
                    case PACKED_SWITCH_PAYLOAD:
                    case ARRAY_PAYLOAD:
                    case NOP: {
                        decompileNop(instruction, instsIterator);
                    } break;
                    case IGET_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IGET);
                    } break;
                    case IGET_WIDE_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IGET_WIDE);
                    } break;
                    case IGET_OBJECT_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IGET_OBJECT);
                    } break;
                    case IGET_BOOLEAN_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IGET_BOOLEAN);
                    } break;
                    case IGET_BYTE_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IGET_BYTE);
                    } break;
                    case IGET_CHAR_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IGET_CHAR);
                    } break;
                    case IGET_SHORT_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IGET_SHORT);
                    } break;
                    case IPUT_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IPUT);
                    } break;
                    case IPUT_WIDE_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IPUT_WIDE);
                    } break;
                    case IPUT_OBJECT_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IPUT_OBJECT);
                    } break;
                    case IPUT_BOOLEAN_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IPUT_BOOLEAN);
                    } break;
                    case IPUT_BYTE_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IPUT_BYTE);
                    } break;
                    case IPUT_CHAR_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IPUT_CHAR);
                    } break;
                    case IPUT_SHORT_QUICK: {
                        decompileInstanceFieldAccess(instruction, Opcode.IPUT_SHORT);
                    } break;
                    case INVOKE_VIRTUAL_QUICK: {
                        decompileInvokeVirtual(instruction, Opcode.INVOKE_VIRTUAL);
                    } break;
                    case INVOKE_VIRTUAL_QUICK_RANGE: {
                        decompileInvokeVirtual(instruction, Opcode.INVOKE_VIRTUAL_RANGE);
                    } break;
                }
            }

            if (mQuickenedInfoReader.getOffset() != mInfoEndOff) {
                if (mQuickenedInfoReader.getOffset() == mInfoStartOff) {
                    if (DEBUG) Xlog.w(TAG, "Failed to use any value in quickening info, potentially due to duplicate methods.");
                } else {
                    if (DEBUG) Xlog.e(TAG, "Failed to use all values in quickening info. Actual: %s, Expected %s",
                            mQuickenedInfoReader.getOffset(), mInfoEndOff);
                    return false;
                }
            }
            return true;
        }
    }
}