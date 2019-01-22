package com.feertech.qfiler.zip;

import com.feertech.qfiler.DirEntry;
import com.feertech.qfiler.FileInfo;
import com.feertech.qfiler.Media;
import com.feertech.qfiler.QLFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.feertech.qfiler.DataUtils.*;
import static com.feertech.qfiler.DisplayUtils.dataLength;
import static com.feertech.qfiler.FileManager.wildcardToRegex;

public class ZipFile implements Media {

    public static final String ZIP_EXTENSION = ".zip";
    private final File source;

    public ZipFile(File source) {
        this.source = source;
    }

    public String toString() {
        return "Zip File: "+source.getName()+"  "+dataLength(source.length(), true);
    }

    @Override
    public List<DirEntry> getDir() throws IOException {
        List<DirEntry> dir = new ArrayList<>();
        try(ZipInputStream in = new ZipInputStream(new FileInputStream(source))) {
            ZipEntry entry;
            while((entry = in.getNextEntry()) != null) {
                FileInfo info =  parseExtra(entry.getExtra(), entry.getName(), (int) entry.getSize());
                dir.add(info);
            }
        }
        return dir;
    }

    @Override
    public List<QLFile> getFiles(String match) throws IOException {
        List<QLFile> files = new ArrayList<>();
        Pattern pattern = Pattern.compile(wildcardToRegex(match));

        try(ZipInputStream in = new ZipInputStream(new FileInputStream(source))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if( pattern.matcher(entry.getName().toLowerCase()).matches() ) {
                    QLFile file = getFile(entry, in);
                    files.add(file);
                }
            }
        }
        return files;
    }

    private QLFile getFile(ZipEntry entry, InputStream stream) throws IOException {
        FileInfo info =  parseExtra(entry.getExtra(), entry.getName(), (int) entry.getSize());
        byte[] bytes = new byte[info.getLength()];

        int offset = 0;
        int length = 0;
        while( offset < bytes.length && (length = stream.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += length;
        }
        return new ZipFileData(entry.getName(), info, bytes);
    }

    private FileInfo parseExtra(byte[] extra, String name, int length) {
        int index = 0;
        FileInfo info = null;

        while( extra != null && index+1 < extra.length ) {
            int tag = shortValue(extra, index);

            index += 2;

            if( tag == 0x5554 ) {
                if( index+1 < extra.length ) {
                    int size = shortLE(extra, index);
                    if( size != 5 ) {
                        System.out.println("Unexpected tag size for 5554 tag");
                        break;
                    }
                    index += 2+size;
                }
                else {
                    System.out.println("Missing header data for tag");
                    break;
                }
            }
            else if( tag == 0x4afb ) {
                if( index+1 < extra.length ) {
                    int size = shortLE(extra, index);
                    if( size != 72 ) {
                        System.out.println("Unexpected tag size for 4afb tag");
                        break;
                    }
                    info = new FileInfo(extra, index+10);
                    index += 2+size;
                }
                else {
                    System.out.println("Missing header data for tag");
                    break;
                }
            }
            else {
                System.out.println("Unrecognised tag in header "+Integer.toString(tag, 16));
                break;
            }
        }

        if( info == null ) {
            info = new FileInfo(name, length);
        }
        return info;
    }
}
