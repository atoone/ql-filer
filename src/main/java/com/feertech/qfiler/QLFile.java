package com.feertech.qfiler;

public interface QLFile {

    String getName();

    byte[] getData();

    FileInfo getHeader();
}
