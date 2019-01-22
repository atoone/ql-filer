package com.feertech.qfiler.mdv;

import com.feertech.qfiler.FileInfo;
import com.feertech.qfiler.QLFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.feertech.qfiler.DataUtils.copyBytes;

public class ChunkedFile implements QLFile {

    private List<Sector> sectors;
    private FileInfo info;
    private String name;

    public ChunkedFile() {
        sectors = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addSector(Sector sector) {
        sectors.add(sector);
    }

    public int validate() {
        int errors = 0;

        Collections.sort(sectors, Comparator.comparingInt(s -> s.getData().getBlockNum()));

        int index = 0;
        for( Sector sector: sectors ) {
            int blockNum = sector.getData().getBlockNum();
            if( blockNum == index ) {
                index++;
            }
            else if( blockNum > index ) {
                errors++;
                log("Missing block "+index+" in file ");
                index = blockNum+1;
            }
            else {
                errors++;
                log("Duplicate block "+index+" in file");
            }
        }

        if( index > 0) {
            parseHeader(sectors.get(0));
        }
        return errors;
    }

    private void parseHeader(Sector sector) {
        DataBlock data = sector.getData();
        info = new FileInfo(data.getArray(), 0);
    }

    private void log(String message) {

    }

    public FileInfo getHeader() {
        return info;
    }

    public int getSectorCount() {
        return sectors.size();
    }

    public int getLength() {
        return info.getLength() - FileInfo.INFO_LENGTH;
    }

    public byte[] getData() {
        int offset = 0;
        int remaining = info.getLength() - FileInfo.INFO_LENGTH;

        byte[] bytes = new byte[remaining];

        for(int i=0; i<sectors.size(); i++ ) {
            int start = i==0 ? FileInfo.INFO_LENGTH: 0;
            int length = DataBlock.DATA_LENGTH - start;
            if( length > remaining ) length = remaining;

            copyBytes(sectors.get(i).getData().getArray(), start,bytes, offset, length );
            offset += length;
            remaining -= length;
        }
        return bytes;
    }
}
