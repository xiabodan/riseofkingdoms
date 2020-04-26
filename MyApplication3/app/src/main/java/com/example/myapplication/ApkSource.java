package com.example.myapplication;

import java.io.InputStream;

public interface ApkSource {

    boolean nextApk() throws Exception;

    InputStream openApkInputStream() throws Exception;

    long getApkLength() throws Exception;

    String getApkName() throws Exception;
}