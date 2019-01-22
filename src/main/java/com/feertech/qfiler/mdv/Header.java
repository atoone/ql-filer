package com.feertech.qfiler.mdv;

import java.util.Arrays;

import static com.feertech.qfiler.DataUtils.*;

public class Header {
    private byte[] preamble = new byte[12];   // 10 x 0x00, 2 x 0x0ff
    private byte   fflag;
    private byte   sectorNum;
    private byte[] name = new byte[10];
    private short  random;
    private short  checksum;

    public static final int HEADER_LENGTH = 28;

    public Header(String driveName, int sector, short random) {
        Arrays.fill(preamble, (byte)0);
        preamble[10] = (byte)0xff;
        preamble[11] = (byte)0xff;

        fflag = (byte)0xff;
        sectorNum = (byte)sector;

        for(int i=0; i<name.length; i++) {
            name[i] = (byte) (i < driveName.length() ? driveName.charAt(i) : ' ');
        }

        this.random = random;
        checksum = calcChecksum();
    }

    public Header(byte[] bytes, int offset) {
        copyBytes(bytes, offset, preamble, 0, 12);
        fflag = bytes[offset+12];
        sectorNum = bytes[offset+13];
        copyBytes(bytes, offset+14, name, 0, 10);
        random = shortValue(bytes, 24);
        checksum = shortValue(bytes, 26);
        // System.out.println(" Got "+byteHex(bytes[24])+" "+byteHex(bytes[25])+" gives "+shortHex(random));
        // System.out.println("Header "+getName()+" Sector "+(sectorNum & 0x0FF));
        // System.out.println("Checksum is "+shortHex(checksum)+" expected "+shortHex(calcChecksum()));
    }

    public byte[] toBytes() {
        byte[] header = new byte[HEADER_LENGTH];
        copyBytes(preamble, header, 0);
        header[12] = fflag;
        header[13] = sectorNum;
        copyBytes(name, header, 14);
        setShort(random, header, 24);
        setShort(checksum, header, 26);

        return header;
    }

    private short calcChecksum() {
        return checksum(fflag).add(sectorNum).add(name).add(random).checksum();
    }

    public boolean checkChecksum() {
        return checksum == calcChecksum();
    }

    public boolean checkPreamble() {
        return checkBytes(preamble,0, 10,  (byte)0) &
               checkBytes(preamble, 10, 2, (byte)0xff) ;
    }

    public short getRandom() {
        return random;
    }

    public int getSectorNum() {
        return sectorNum & 0x0ff;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<name.length; i++) {
            sb.append((char)name[i]);
        }
        return sb.toString().trim();
    }

    public boolean isUnused() {
        return fflag != (byte)0xff;
    }
}
