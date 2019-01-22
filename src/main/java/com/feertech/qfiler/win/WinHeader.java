package com.feertech.qfiler.win;

import static com.feertech.qfiler.DataUtils.*;

public class WinHeader {

    public static final int HEADER_LENGTH = 64;

    private static final char[] DISC_IDENTIFIER = {'Q','L','W','A'};

    private static final int SECTOR_LENGTH = 512;

    private char[] id = new char[4];
    private short nameLength;
    private byte[] name = new byte[20];
    private int updateCheck;
    private short interleave;
    private short tracksPerCylinder;
    private short sectorsPerGroup;
    private short sectorsPerTrack;
    private short cylindersPerDrive;

    private short sectorsPerMap;
    private short mapCount;
    private int groupCount;
    private int freeGroupCount;

    private int firstFreeGroup;
    private int rootDirNum;
    private int rootDirLen;

    private short firstCylinderNum;
    private int firstSectorInPartition;

    public WinHeader(byte[] bytes, int offset) {
        for( int i=0; i<4; i++ ) {
            id[i] = (char)bytes[offset+i];
        }
        nameLength = shortValue(bytes, offset+4);
        copyBytes(bytes, offset+6, name, 0, 20);
        updateCheck = intValue(bytes, offset+28);
        interleave = shortValue(bytes,offset+32);

        sectorsPerGroup = shortValue(bytes, offset+34);
        sectorsPerTrack = shortValue(bytes, offset+36);
        tracksPerCylinder = shortValue(bytes, offset+38);
        cylindersPerDrive = shortValue(bytes, offset+40);

        groupCount = shortValue(bytes, offset+42) & 0x0FFFF;
        freeGroupCount = shortValue(bytes, offset+44) & 0x0FFFF;
        sectorsPerMap = shortValue(bytes, offset+46);
        mapCount = shortValue(bytes, offset+48);

        firstFreeGroup = shortValue(bytes, offset+50) & 0x0FFFF;
        rootDirNum = shortValue(bytes, offset+52) & 0x0FFFF;
        rootDirLen = intValue(bytes, offset+54);

        firstCylinderNum = shortValue(bytes, offset+58);
        firstSectorInPartition = intValue(bytes, offset+60);
    }

    public String debug() {
        StringBuilder sb = new StringBuilder("Win Drive header");
        sb.append("\n  Name : ").append(getName())
          .append("\n  Update check : ").append(updateCheck)
          .append("\n  Interleave   : ").append(interleave)
          .append("\n  Sectors/group: ").append(sectorsPerGroup)
          .append("\n  Sectors/track: ").append(sectorsPerTrack)
          .append("\n  Tracks/cylinder: ").append(tracksPerCylinder)
          .append("\n  Cylinders/drive: ").append(cylindersPerDrive)
          .append("\n  Group count  : ").append(groupCount)
          .append("\n  Free groups  : ").append(freeGroupCount)
          .append("\n  Sectors/map  : ").append(sectorsPerMap)
          .append("\n  Map count    : ").append(mapCount)
          .append("\n  First free group: ").append(firstFreeGroup)
          .append("\n  Root dir     : ").append(rootDirNum)
          .append("\n  Root dir len : ").append(rootDirLen)
          .append("\n  First cyl num: ").append(firstCylinderNum)
          .append("\n  First sector in partition: ").append(firstSectorInPartition);
        return sb.toString();
    }
    public int getTotalSize() {
        return groupCount * sectorsPerGroup * SECTOR_LENGTH;
    }

    public int getFreeSize() {
        return freeGroupCount * sectorsPerGroup * SECTOR_LENGTH;
    }

    public int getGroupCount() {
        return groupCount;
    }

    public int getMapLength() {
        return sectorsPerMap * SECTOR_LENGTH;
    }

    public int getSectorsPerMap() {
        return sectorsPerMap;
    }

    public int getRootDirNum() {
        return rootDirNum;
    }

    public int getRootDirLen() {
        return rootDirLen;
    }

    public int getGroupLength() {
        return sectorsPerGroup * SECTOR_LENGTH;
    }

    public int getSectorsPerGroup() {
        return sectorsPerGroup;
    }

    public String getName() {
        return new String(name, 0, nameLength);
    }
}
