package com.feertech.qfiler;

import com.feertech.qfiler.mdv.Cartridge;
import com.feertech.qfiler.zip.ZipFile;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.*;
import java.util.regex.Pattern;

import static com.feertech.qfiler.DataUtils.doHex;

public class FileManager {

    private static final String SPECIAL_REGEX_CHARS = "<([{\\^-=$!|]})+.>";

    public Media getContainer(String filename) throws IOException {
        if( filename.toLowerCase().endsWith(Cartridge.MDV_EXTENSION) ) {
            File file = new File(filename);
            try( FileInputStream fis = new FileInputStream(file)) {
                Cartridge mdv = new Cartridge(filename);
                mdv.read(fis);
                return mdv;
            }
        }
        else if( filename.toLowerCase().endsWith(ZipFile.ZIP_EXTENSION) ) {
            File file = new File(filename);
            ZipFile zip = new ZipFile(file);
            return zip;
        }
        else {
            throw new IOException("Unknown file type for "+filename);
        }
    }

    public void exportFile(File target, QLFile file) throws IOException {
        String name = target.getName();
        File infoFile = new File(target.getParent(), name+".qlinfo");
        if( target.exists() ) {
            throw new IOException("Target file exists: "+target.getAbsolutePath() );
        }
        if( infoFile.exists() ) {
            throw new IOException("Info file exists: "+infoFile.getAbsolutePath() );
        }

        // Write header out..
        String header = doHex(file.getHeader().toBytes(), 0, FileInfo.INFO_LENGTH);
        try(PrintWriter out = new PrintWriter(infoFile) ) {
            out.print(header);
        }

        // Then data
        try(FileOutputStream fos = new FileOutputStream(target)) {
            fos.write(file.getData());
        }
    }

    public static String wildcardToRegex(String pattern) {
        for(int i=0; i<SPECIAL_REGEX_CHARS.length(); i++) {
            pattern = pattern.replace(SPECIAL_REGEX_CHARS.substring(i, i+1), "\\"+SPECIAL_REGEX_CHARS.charAt(i));
        }
        return "^" + pattern.toLowerCase().replace("*", ".*").
                             replace("?", ".") + "$";
    }
}
