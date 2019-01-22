package com.feertech.qfiler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static com.feertech.qfiler.DataUtils.*;
import static com.feertech.qfiler.DisplayUtils.padTo;

public class FileInfo extends DirEntry {

    public static final int EPOCH_ADJUST = 284000400;
    private byte access;
    private byte type;    // 0: regular  1:exec
    private int[] info = new int[2];
    private short nameLength;
    private byte[] name = new byte[MAX_NAME_LENGTH];
    private int lastUpdate;
    private int version;
    private int fileId;
    private int lastBackup;

    public static final int INFO_LENGTH = 64;
    public static final int MAX_NAME_LENGTH = 36;

    private static final DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm");

    public FileInfo(byte[] bytes, int offset) {
        setLength( intValue(bytes, offset) );
        access = bytes[offset+4];
        type = bytes[offset+5];
        info[0] = intValue(bytes, offset+6);
        info[1] = intValue(bytes, offset+10);
        nameLength = shortValue(bytes, offset+14);
        copyBytes(bytes, offset+16, name, 0, MAX_NAME_LENGTH);
        lastUpdate = intValue(bytes, offset+52);
        version = shortValue(bytes, offset+56) & 0x0FFFF;
        fileId = shortValue(bytes, offset+58) & 0x0FFFF;
        lastBackup = intValue(bytes, offset+60);

        setFilename(parseFilename());
    }

    public FileInfo(String name, int length) {
        setLength(length);
        access = 0;
        type = 0;
        info[0] = 0;
        info[1] = 0;
        setFilename(name);
        lastUpdate = 0;
        version = 1;
        fileId = 0;
        lastBackup = 0;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[INFO_LENGTH];
        setInt(getLength(), bytes, 0);
        bytes[4] = access;
        bytes[5] = type;
        setInt(info[0], bytes, 6);
        setInt(info[1], bytes, 10);
        setShort(nameLength, bytes,14);
        copyBytes(name, 0, bytes, 16, MAX_NAME_LENGTH);
        setInt(lastUpdate, bytes, 52);
        setShort((short)version, bytes, 56);
        setShort((short)fileId, bytes, 58);
        setInt(lastBackup, bytes, 60);

        return bytes;
    }

    public void setFilename(String filename) {
        if( filename.length() > MAX_NAME_LENGTH ) throw new IllegalArgumentException("Name is too long: "+filename);

        Arrays.fill(name, (byte)0);
        nameLength = (short)filename.length();
        for( int i=0; i<nameLength; i++ ) {
            name[i] = (byte)filename.charAt(i);
        }
        super.setFilename(filename);
    }

    public String parseFilename() {
        int len = nameLength > 36 ? 2: nameLength;

        StringBuilder sb = new StringBuilder();
        for(int index=0; index<len; index++) {
            sb.append((char)name[index]);
        }
        return sb.toString();
    }

    public String toString() {
        return "File: "+padTo(getFilename(),20)+
               " Type: "+type+
               " Length: "+padTo(Integer.toString(getLength()-FileInfo.INFO_LENGTH), 10)+
               " Dataspace: "+padTo(Integer.toString(info[0]), 8)+
                (lastUpdate != 0 ? (" Updated "+
                        dateFormat.format(new Date((lastUpdate-EPOCH_ADJUST)*1000l))):"")+
               " Acc/Ver/FID: "+access+"/"+version+"/"+fileId;     }

    public int getFileId() {
        return fileId;
    }
}
