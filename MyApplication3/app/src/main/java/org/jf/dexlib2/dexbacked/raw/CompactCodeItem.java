/*
 * Copyright 2013, Google Inc.
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

package org.jf.dexlib2.dexbacked.raw;

import org.jf.dexlib2.dexbacked.BaseDexReader;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import javax.annotation.Nonnull;

public class CompactCodeItem {
    public static int INSTRUCTION_START_OFFSET = 4;

    public static int REGISTERS_SIZE_SHIFT = 12;
    public static int INS_SIZE_SHIFT = 8;
    public static int OUTS_SIZE_SHIFT = 4;
    public static int TRIES_SIZE_SHIFT = 0;
    public static int INSNS_SIZE_SHIFT = 5;

    public static int FLAG_PREHEADER_REGISTER_SIZE = 0x1 << 0;
    public static int FLAG_PREHEADER_INS_SIZE = 0x1 << 1;
    public static int FLAG_PREHEADER_OUTS_SIZE = 0x1 << 2;
    public static int FLAG_PREHEADER_TRIES_SIZE = 0x1 << 3;
    public static int FLAG_PREHEADER_INSNS_SIZE = 0x1 << 4;

    public static int FLAG_PREHEADER_COMBINED = FLAG_PREHEADER_REGISTER_SIZE
            | FLAG_PREHEADER_INS_SIZE | FLAG_PREHEADER_OUTS_SIZE
            | FLAG_PREHEADER_TRIES_SIZE | FLAG_PREHEADER_INSNS_SIZE;

    public int fields;
    public int insnsCountAndFlags;
    public int insnsCount;
    public int registersSize;
    public int insSize;
    public int outsSize;
    public int triesSize;
    public int codeOffset;
    BaseDexReader reader;
    DexBackedDexFile dexFile;

    public CompactCodeItem(@Nonnull DexBackedDexFile dexFile, int codeOffset) {
        this.codeOffset = codeOffset;
        this.dexFile = dexFile;
        this.reader = dexFile.readerAt(codeOffset);
        this.fields = reader.readUshort();
        this.insnsCountAndFlags = reader.readUshort();
    }

    public boolean hasPreHeader(int flag) {
        return (insnsCountAndFlags & flag) != 0;
    }
    public boolean hasAnyPreHeader(int insns_count_and_flags) {
        return (insns_count_and_flags & FLAG_PREHEADER_COMBINED) != 0;
    }

    public void decodeFields(boolean decodeOnlyInstructionCount) {
        insnsCount = insnsCountAndFlags >> INSNS_SIZE_SHIFT;

        if (!decodeOnlyInstructionCount) {
            int fields = this.fields;
            registersSize = (fields >> REGISTERS_SIZE_SHIFT) & 0xF;
            insSize = (fields >> INS_SIZE_SHIFT) & 0xF;
            outsSize = (fields >> OUTS_SIZE_SHIFT) & 0xF;
            triesSize = (fields >> TRIES_SIZE_SHIFT) & 0xF;
        }
        if (hasAnyPreHeader(insnsCountAndFlags)) {
            BaseDexReader preheaderReader = dexFile.readerAt(0);
            int preheaderOffset = codeOffset;
            if (hasPreHeader(FLAG_PREHEADER_INSNS_SIZE)) {
                preheaderOffset -= 2;
                insnsCount += preheaderReader.readUshort(preheaderOffset);
                preheaderOffset -= 2;
                insnsCount += preheaderReader.readUshort(preheaderOffset) << 16;
            }
            if (!decodeOnlyInstructionCount) {
                if (hasPreHeader(FLAG_PREHEADER_REGISTER_SIZE)) {
                    preheaderOffset -= 2;
                    registersSize += preheaderReader.readUshort(preheaderOffset);
                }
                if (hasPreHeader(FLAG_PREHEADER_INS_SIZE)) {
                    preheaderOffset -= 2;
                    insSize += preheaderReader.readUshort(preheaderOffset);
                }
                if (hasPreHeader(FLAG_PREHEADER_OUTS_SIZE)) {
                    preheaderOffset -= 2;
                    outsSize += preheaderReader.readUshort(preheaderOffset);
                }
                if (hasPreHeader(FLAG_PREHEADER_TRIES_SIZE)) {
                    preheaderOffset -= 2;
                    triesSize += preheaderReader.readUshort(preheaderOffset);
                }
            }
        }
        if (!decodeOnlyInstructionCount) {
            registersSize += insSize;
        }
    }
}
