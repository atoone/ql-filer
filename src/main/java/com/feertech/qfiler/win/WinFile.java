package com.feertech.qfiler.win;

import com.feertech.qfiler.FileInfo;
import com.feertech.qfiler.QLFile;

public class WinFile implements QLFile {

    private FileInfo header;
    private byte[] data;

    public WinFile(FileInfo header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    @Override
    public String getName() {
        return header.getFilename();
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
