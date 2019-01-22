package com.feertech.qfiler.mdv;

import com.feertech.qfiler.Media;
import com.feertech.qfiler.FileInfo;
import com.feertech.qfiler.QLFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Pattern;

import static com.feertech.qfiler.FileManager.wildcardToRegex;

public class Cartridge implements Media {

    public static final String MDV_EXTENSION = ".mdv";
    private static final int MAX_SECTORS = 255;

    private List<Sector> sectors;

    private Map<Integer, Sector> sectorMap;
    private Map<Integer, ChunkedFile> fileMap;

    private String name;
    private short random;
    private int errors;
    int free = 0;

    public Cartridge(String name) {
        if(name.length() > 10) throw new IllegalArgumentException("Name is too long, must be 10 characters of fewer");
        this.name = name;
        this.random = (short) (Math.random() * 65536);
        errors = 0;
        sectors = new ArrayList<>();
        sectorMap = new HashMap<>();
        fileMap = new HashMap<>();
    }

    public String toString() {
        return "Cartridge: "+name+" "+free+"/"+sectors.size();
    }

    public void format() {
        this.random = (short) (Math.random() * 65536);
        for(int i=0; i<MAX_SECTORS; i++) {
            sectors.add(new Sector(name, i, random));
        }
        errors = 0;
        free = MAX_SECTORS;
    }

    public int read(InputStream input) throws IOException {
        sectors.clear();
        sectorMap.clear();
        fileMap.clear();

        while (input.available() > 0) {
            sectors.add(new Sector(input));
        }

        if (sectors.isEmpty()) {
            throw new IOException("No data");
        }

        Sector first = sectors.get(0);
        name = first.getHeader().getName();
        random = first.getHeader().getRandom();

        errors = checkSectors();
        for( ChunkedFile file: fileMap.values() ) {
            errors += file.validate();
        }

        if( !hasDir() ) {
            log("Cartridge has no directory file");
            errors++;
        }
        else {
            errors += validateDirectory();
        }
        return errors;
    }

    public void write(OutputStream out) throws IOException {
        for(Sector sector: sectors) {
            sector.write(out);
        }
    }

    public int checkSectors() {
        int errors = 0;

        for(Sector sector: sectors) {
            Header header = sector.getHeader();
            if( header.isUnused() ) {
                continue;
            }

            if( sectorMap.containsKey( header.getSectorNum() )) {
                errors++;
                log("Duplicate sector found: "+header.getSectorNum());
            }
            else {
                sectorMap.put(header.getSectorNum(), sector);
            }

            if( !header.checkChecksum() ) {
                errors++;
                log( "Header checksum error for sector "+header.getSectorNum());
            }
            if( !header.checkPreamble() ) {
                errors++;
                log( "Header preamble is invalid for sector "+header.getSectorNum());
            }
            if( !name.equals(header.getName()) ) {
                errors++;
                log("Name mismatch, expected "+name+", got "+header.getName());
            }

            DataBlock data = sector.getData();

            if( data.isFree() ) {
                free++;
            }
            else {
                if( !data.checkHeaderChecksum() ) {
                    errors++;
                    log("Data header checksum error for sector "+header.getSectorNum());
                }
                if( !data.checkDataChecksum() ) {
                    errors++;
                    log("Data block checksum error for sector "+header.getSectorNum());
                }

                if( !fileMap.containsKey(data.getFileNum()) ) {
                    fileMap.put(data.getFileNum(), new ChunkedFile());
                }

                fileMap.get(data.getFileNum()).addSector(sector);
            }
        }
        return errors;
    }

    public int getErrors() {
        return errors;
    }

    /**
     * True if the cartridge has a directory file
     * @return
     */
    public boolean hasDir() {
        ChunkedFile file = fileMap.get(0);
        return file != null && file.getLength() > 0;
    }

    private int validateDirectory() {
        int errors = 0;
        ChunkedFile directory = fileMap.get(0);
        int total = directory.getLength() / FileInfo.INFO_LENGTH;

        byte[] data = directory.getData();
        for( int i=0; i<total; i++ ) {
            FileInfo entry = new FileInfo(data, i*FileInfo.INFO_LENGTH);
            if( entry.getLength() == 0 ) {
                log( "Skipping zero length file at index "+i);
                continue;
            }

            ChunkedFile file = fileMap.get(i+1);
            if( file == null ) {
                log("Missing file for index "+i);
                errors++;
            }
            else {
                file.setName(entry.getFilename());
            }
        }

        return errors;
    }

    /**
     * Return the directory for this cartridge, either from the directory file, or by parsing the file contents.
     * @return The list of normal files on this cartridge
     */
    public List<FileInfo> getDir() {
        if( !hasDir() ) return getDirFromFiles();

        ChunkedFile file = fileMap.get(0);
        int total = file.getLength() / FileInfo.INFO_LENGTH;
        List<FileInfo> files = new ArrayList<>();

        byte[] data = file.getData();
        for( int i=0; i<total; i++ ) {
            files.add(new FileInfo(data, i*FileInfo.INFO_LENGTH));
        }
        return files;
    }

    private List<FileInfo> getDirFromFiles() {
        List<FileInfo> files = new ArrayList<>();
        for( Map.Entry<Integer,ChunkedFile> entry: fileMap.entrySet() ) {
            int fileNum = entry.getKey();
            if( fileNum > 0 && fileNum < 127 ) {
                files.add(entry.getValue().getHeader());
            }
        }
        return files;
    }

    public List<QLFile> getFiles(String match) {
        Pattern pattern = Pattern.compile(wildcardToRegex(match));
        List<QLFile> files = new ArrayList<>();

        ChunkedFile file = fileMap.get(0);
        int total = file.getLength() / FileInfo.INFO_LENGTH;

        byte[] data = file.getData();
        int index = 0;

        for( int i=0; i<total; i++ ) {
            index++;
            FileInfo info = new FileInfo(data, i*FileInfo.INFO_LENGTH);
            if( pattern.matcher(info.getFilename().toLowerCase()).matches() ) {
                files.add(getFile(index));
            }
        }
        return files;
    }

    public int getFree() {
        return free;
    }

    public int getSectors() {
        return sectors.size();
    }

    public int getFiles() {
        return fileMap.size();
    }

    public Set<Integer> getFileIndices() {
        return fileMap.keySet();
    }

    public ChunkedFile getFile(Integer fileNum) {
        return fileMap.get(fileNum);
    }

    public String describe() {
        return "Cartridge '"+name+"' "+getFree()+"/"+getSectors()+". Files: "+getFiles()+" Errors:  "+errors;
    }

    private void log(String message) {
        System.out.println(message);
    }
}
