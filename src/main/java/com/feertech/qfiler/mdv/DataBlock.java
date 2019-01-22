package com.feertech.qfiler.mdv;

import java.util.Arrays;

import static com.feertech.qfiler.DataUtils.*;

public class DataBlock {
    private static byte FREE_FILE = (byte)253;
    public static int DATA_LENGTH = 512;

    // File block header = 16 bytes
    private byte[] preamble = new byte[12];   // 10 x 0x00, 2 x 0xff
    private byte fileNum;
    private byte blockNum;
    private short headerChecksum;

    // Data = DATA_LENGTH + 10 bytes
    private byte[] dataPreamble = new byte[8]; // 6 x 0x00, 2 x 0xff
    private byte[] data = new byte[DATA_LENGTH];
    private short dataChecksum;

    public static final int DATABLOCK_LENGTH = 16 + DATA_LENGTH + 10;

    public DataBlock() {
        Arrays.fill(preamble, (byte)0);
        preamble[10] = (byte)0xff;
        preamble[11] = (byte)0xff;
        fileNum = FREE_FILE;
        blockNum = 0;
        headerChecksum = checksum(fileNum).add(blockNum).checksum();

        Arrays.fill(dataPreamble, (byte)0);
        dataPreamble[6] = (byte)0x0ff;
        dataPreamble[7] = (byte)0x0ff;
        Arrays.fill(data, (byte)0x00);
        dataChecksum = checksum(data, 0, DATA_LENGTH).checksum();
    }

    public DataBlock(byte[] block, int offset) {
        copyBytes(block, offset, preamble, 0, 12);
        fileNum = block[offset+12];
        blockNum = block[offset+13];
        headerChecksum = shortValue(block, offset+14);

        copyBytes(block, offset+16, dataPreamble, 0 , 8);
        copyBytes(block, offset+24, data, 0, DATA_LENGTH);
        dataChecksum = shortValue(block, offset+24+DATA_LENGTH);
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[DATABLOCK_LENGTH];
        copyBytes(preamble, 0, bytes, 0, 12);
        bytes[12] = fileNum;
        bytes[13] = blockNum;
        setShort(headerChecksum, bytes, 14);

        copyBytes(dataPreamble, 0, bytes, 16, 8);
        copyBytes(data, 0, bytes, 24, DATA_LENGTH);

        setShort(dataChecksum, bytes, 24+DATA_LENGTH);
        return bytes;
    }

    public boolean isFree() {
        return fileNum == FREE_FILE;
    }

    public boolean checkHeaderChecksum() {
        return headerChecksum == checksum(fileNum).add(blockNum).checksum();
    }

    public boolean checkDataChecksum() {
        return dataChecksum == checksum(data, 0, DATA_LENGTH).checksum();
    }

    public int getFileNum() {
        return fileNum & 0x0FF;
    }

    public int getBlockNum() {
        return blockNum & 0x0FF;
    }

    public byte[] getArray() {
        return data;
    }
}
