package com.feertech.qfiler.mdv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Sector {
    Header header;
    DataBlock data;
    SyncBlock sync;

    public static int SECTOR_LENGTH = Header.HEADER_LENGTH + DataBlock.DATABLOCK_LENGTH + SyncBlock.BLOCK_LENGTH;

    public Sector(String driveName, int sector, short random) {
        header = new Header(driveName, sector, random);
        data = new DataBlock();
        sync = new SyncBlock();
    }

    public Sector(InputStream input) throws IOException {
        byte[] bytes = readBlock(input, Header.HEADER_LENGTH);
        header = new Header(bytes, 0);

        bytes = readBlock(input, DataBlock.DATABLOCK_LENGTH);
        data = new DataBlock(bytes, 0);

        bytes = readBlock(input, SyncBlock.BLOCK_LENGTH);
        sync = new SyncBlock(bytes, 0);
    }

    public void write(OutputStream out) throws IOException {
        byte[] bytes = header.toBytes();
        out.write(bytes);

        bytes = data.toBytes();
        out.write(bytes);

        bytes = sync.toBytes();
        out.write(bytes);
    }

    private byte[] readBlock(InputStream input, int length) throws IOException {
        byte[] data = new byte[length];
        if( input.read(data, 0, length) != length ) {
            throw new IOException("End of file whilst reading data");
        }
        return data;
    }

    public Header getHeader() {
        return header;
    }

    public DataBlock getData() {
        return data;
    }
}
