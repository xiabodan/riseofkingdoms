package com.example.myapplication;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

    public static void copyStream(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[1024 * 1024];
        int len;
        while ((len = from.read(buf)) > 0) {
            to.write(buf, 0, len);
        }
    }

//    public static void copyFile(File original, File destination) throws IOException {
//        try (FileInputStream inputStream = new FileInputStream(original); FileOutputStream outputStream = new FileOutputStream(destination)) {
//            copyStream(inputStream, outputStream);
//        }
//    }
//
//    public static void copyFileFromAssets(Context context, String assetFileName, File destination) throws IOException {
//        try (InputStream inputStream = context.getAssets().open(assetFileName); FileOutputStream outputStream = new FileOutputStream(destination)) {
//            copyStream(inputStream, outputStream);
//        }
//    }

    public static void deleteRecursively(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles())
                deleteRecursively(child);
        }
        f.delete();
    }

}
