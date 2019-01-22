package com.feertech.qfiler.mdv;

import java.util.Arrays;

import static com.feertech.qfiler.DataUtils.copyBytes;

public class SyncBlock {
    public static final int BLOCK_LENGTH = 120;

    private byte[] block = new byte[BLOCK_LENGTH];

    public SyncBlock() {
        Arrays.fill(block, (byte)0x5a);
    }

    public SyncBlock(byte[] data, int offset) {
        copyBytes(data, offset, block, 0, 120);
    }

    public byte[] toBytes() {
        return block;
    }

    public boolean checkBlock() {
        for(int i=0; i<BLOCK_LENGTH; i++) {
            if(block[i] != (byte)0x5a) {
                return false;
            }
        }
        return true;
    }
}
