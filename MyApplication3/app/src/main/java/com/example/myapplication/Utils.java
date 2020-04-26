package com.example.myapplication;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static class DexEntry {
        final String name;
        final byte[] buf;
        final int off;
        final int len;

        public DexEntry(String name, byte[] buf, int off, int len) {
            this.name = name;
            this.buf = buf;
            this.off = off;
            this.len = len;
        }
    }

    public static void buildIntactApk(String outputZip, List<String> inputZips, List<DexEntry> appends) throws Exception {
        final ZipOutputStream outputZipFile = new ZipOutputStream(new FileOutputStream(outputZip));

        // first, copy contents from existing inputZipFile
        if (inputZips != null && inputZips.size() > 0) {
            final byte[] buffer = new byte[1024 * 512];
            for (final String inputZip : inputZips) {
                final ZipInputStream zis = new ZipInputStream(new FileInputStream(inputZip));
                for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                    Log.i(TAG + "-buildIntactApk", "copy:" + inputZip +  ":" + entry.getName());
                    ZipEntry newEntry;
                    if (entry.getMethod() == ZipEntry.STORED) {
                        newEntry = entry;
                    } else {
                        newEntry = new ZipEntry(entry);
                        newEntry.setCompressedSize(-1);
                    }
                    outputZipFile.putNextEntry(newEntry);
                    for (int bytesRead = zis.read(buffer); bytesRead > -1; bytesRead = zis.read(buffer)) {
                        outputZipFile.write(buffer, 0, bytesRead);
                    }
                    outputZipFile.closeEntry();
                }
                zis.close();
            }
        }

        if (appends != null && appends.size() > 0) {
            for (final DexEntry dexEntry : appends) {
                final ZipEntry zipEntry = new ZipEntry(dexEntry.name);
                Log.i(TAG + "-buildIntactApk", "append: " + zipEntry.getName());
                outputZipFile.putNextEntry(zipEntry);
                outputZipFile.write(dexEntry.buf, dexEntry.off, dexEntry.len);
                outputZipFile.closeEntry();
            }
        }

        // close
        outputZipFile.close();
    }
}
