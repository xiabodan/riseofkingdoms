package com.example.myapplication;

import java.io.InputStream;

public interface FileDescriptor {

    String name() throws Exception;

    long length() throws Exception;

    InputStream open() throws Exception;

}
