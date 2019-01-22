package com.feertech.qfiler.win;

import com.feertech.qfiler.DirEntry;
import com.feertech.qfiler.FileInfo;
import com.feertech.qfiler.Media;
import com.feertech.qfiler.QLFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.feertech.qfiler.DataUtils.shortValue;
import static com.feertech.qfiler.FileManager.wildcardToRegex;

public class Disk implements Media {

    public static final String WIN_EXTENSION = ".win";

    private File file;

    private WinHeader header;

    private int[] groupMap;

    private List<FileInfo> directory;

    public Disk(File file) throws IOException {
        directory = new ArrayList<>();
        read(file);
    }

    public String toString() {
        return "Win Disk "+header.getName();
    }
    public void setFile(File file) {
        this.file = file;
    }

    public void read(File file) throws IOException {
        setFile(file);
        try(InputStream is = new FileInputStream(file)) {
            byte[] bytes = new byte[WinHeader.HEADER_LENGTH];

            readFully(is, bytes, 0, WinHeader.HEADER_LENGTH);
            header = new WinHeader(bytes, 0);

            groupMap = new int[header.getGroupCount()];

            int mapLength = header.getMapLength();
            byte[] groupData = new byte[mapLength];
            readFully(is, groupData, 0, mapLength);

            for (int i = 0; i < header.getGroupCount(); i++) {
                groupMap[i] = shortValue(groupData, i * 2) & 0x0FFF;
            }
        }

        // Check group map is first sectors of drive in sequence
        int spm = header.getSectorsPerMap();
        int spg = header.getSectorsPerGroup();

        int groupsInMap = spm / spg + ((spm % spg == 0) ? 0 : 1);

        for( int i=0; i<groupsInMap-1; i++ ) {
            if( groupMap[i] != i+1) {
                throw new IllegalStateException("Group map is not in sequence, cannot read drive");
            }
        }
        if( groupMap[groupsInMap-1] != 0) {
            throw new IllegalStateException("Group map continues past final sector, cannot read drive");
        }

        byte[] directoryFile = readFile(header.getRootDirNum(), header.getRootDirLen());
        parseDirectory(directoryFile);
    }

    private void parseDirectory(byte[] directoryFile) {
        directory.clear();
        int count = directoryFile.length / FileInfo.INFO_LENGTH;
        if( directoryFile.length % FileInfo.INFO_LENGTH != 0 ) {
            log("Warning: Directory file is not exact multiple of directory entry length: "+directoryFile.length);
        }

        for( int i=1; i<count; i++) {
            FileInfo entry = new FileInfo(directoryFile, i*FileInfo.INFO_LENGTH);
            if( entry.getLength() != 0) {
                directory.add(entry);
            }
        }
    }

    private byte[] readFile(int fileNum, int fileLength) throws IOException {
        byte[] bytes = new byte[fileLength-FileInfo.INFO_LENGTH];
        int offset = 0;

        boolean first = true;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            while( fileNum != 0 ) {
                byte[] group = readGroup(raf, fileNum);
                int index = first ? FileInfo.INFO_LENGTH : 0;
                while( index < group.length && offset < fileLength-FileInfo.INFO_LENGTH ) {
                    bytes[offset++] = group[index++];
                }

                fileNum = groupMap[fileNum];
                first = false;
            }
        }

        return bytes;
    }

    private byte[] readGroup(RandomAccessFile raf, int fileNum) throws IOException {
        raf.seek(header.getGroupLength() * fileNum);
        byte[] bytes = new byte[header.getGroupLength()];

        int offset = 0;
        int remaining = bytes.length;

        while( remaining > 0 ) {
            int read = raf.read(bytes, offset, remaining);
            if( read < 0 ) {
                throw new IOException("Unexpected file error reading group");
            }
            offset += read;
            remaining -= read;
        }
        return bytes;
    }

    private void readFully(InputStream is, byte[] bytes, int offset, int remaining) throws IOException {
        while( remaining > 0 ) {
            int read = is.read(bytes, offset, remaining);
            if( read < 0 ) {
                throw new IOException("Unexpected end of file reading header");
            }
            offset += read;
            remaining -= read;
        }
    }

    private void log(String message) {
        System.out.println(message);
    }

    @Override
    public List<? extends DirEntry> getDir() throws IOException {
        return directory;
    }

    @Override
    public List<QLFile> getFiles(String match) throws IOException {
        Pattern pattern = Pattern.compile(wildcardToRegex(match));
        List<QLFile> files = new ArrayList<>();

        for( FileInfo info: directory ) {
            if( pattern.matcher(info.getFilename().toLowerCase()).matches() ) {
                byte[] data = readFile(info.getFileId(), info.getLength());
                files.add(new WinFile(info, data));
            }
        }
        return files;
    }
}
