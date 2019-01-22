package com.feertech.qfiler.zip;

import com.feertech.qfiler.FileInfo;
import com.feertech.qfiler.QLFile;

public class ZipFileData implements QLFile {

    private final FileInfo header;
    private final byte[] data;
    private final String name;

    public ZipFileData(String name, FileInfo header, byte[] data) {
        this.header = header;
        this.data = data;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public FileInfo getHeader() {
        return header;
    }
}
