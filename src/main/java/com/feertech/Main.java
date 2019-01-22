package com.feertech;

import com.feertech.qfiler.DirEntry;
import com.feertech.qfiler.FileInfo;
import com.feertech.qfiler.Media;
import com.feertech.qfiler.QLFile;
import com.feertech.qfiler.mdv.Cartridge;
import com.feertech.qfiler.win.Disk;
import com.feertech.qfiler.zip.ZipFile;

import java.io.*;
import java.util.List;

import static com.feertech.qfiler.DataUtils.doHex;
import static com.feertech.qfiler.DisplayUtils.*;

public class Main {

    private static final String INFO_SUFFIX = ".qlinfo";

    private final String[] args;

    private File currentFile;
    private Media media;

    public Main(String[] args) {
        this.args = args;
    }

    public static void main(String[] args) {
        if( args.length == 0 ) {
            printAbout();
            return;
        }
        Main main = new Main(args);
        main.processCommands();
    }

    private void processCommands() {
        int index = 0;
        while( index < args.length ) {
            try {
                index = doCommand(args[index++].toLowerCase(), index);
            }
            catch( IllegalStateException|IllegalArgumentException ia) {
                // Nothing to do here
            }
            catch( Exception ex ) {
                ex.printStackTrace();
                System.err.println("Unexpected error : "+ex.getMessage());
            }
        }
    }

    private int doCommand(String command, int index) {
        switch( command ) {
            case "open" : return openMedia(index);
            case "dir"  : return dir(index, false);
            case "info" : return dir(index, true);
            case "export": return export(index);
            case "dump" : return dump(index);
            case "help" : return help(index);
            default:
                stop("Unexpected command "+command);
        }
        return index;
    }

    private int help(int index) {
        if( (index >= args.length) || (index < 0) ) {
            out("help <command> - Give extended help for the given command");
        }
        else {
            String command = args[index++];
            doCommand(command, -1);
        }
        return index;
    }

    private int dump(int index) {
        if( index < 0 ) {
            out("dump <filename> - Produce a hex dump of file(s) on the media\n"+
                    "   Filenames are case insensitive. Wildcards are supported.\n"+
                    "   ? matches any single character, * matches multiple characters");
        }
        else {
            String match = checkOption(index++, "Extract: Missing filename");

            if (media == null) {
                stop("Dump: No media open, use open command first");
            }

            try {
                for (QLFile file : media.getFiles(match)) {
                    String filename = file.getName();
                    out("============== " + filename+ " ==============");
                    byte[] data = file.getData();
                    for(int pos=0; pos < data.length; pos+=16) {
                        out(paddedHex(pos, 8)+" "+doHex(data, pos, 16));
                    }
                    out("");
                }
            }
            catch(IOException ioe) {
                stop("Dump: Could not read media "+ioe.getMessage());
            }
        }

        return index;
    }

    private int export(int index) {
        if( index < 0 ) {
            out("extract <filepattern> - Write file(s) from media to disk\n"+
                "   Filenames are case insensitive. Wildcards are supported.\n"+
                "   ? matches any single character, * matches multiple characters");
        }
        else {
            String match = checkOption(index++, "Extract: Missing filename");

            if( media == null ) {
                stop("Export: No media open, use open command first");
            }

            try {
                int exported = 0;

                for(QLFile file: media.getFiles(match)) {
                    exported++;
                    String filename = file.getName();
                    out("Writing file "+filename);

                    File outFile = new File(filename);
                    File infoFile = new File(filename+INFO_SUFFIX);
                    if( outFile.exists() ) {
                        stop("Cannot write file "+outFile.getName()+" - already exists");
                    }
                    if( infoFile.exists() ) {
                        stop("Cannot write info file "+infoFile.getName()+" - already exists");
                    }
                    try( OutputStream os = new FileOutputStream(infoFile) ) {
                        os.write(doHex(file.getHeader().toBytes(), 0, FileInfo.INFO_LENGTH).getBytes());
                        os.write("\n----------\n".getBytes());
                        os.write(file.getHeader().toString().getBytes());
                        os.write("\n".getBytes());
                    }
                    try( OutputStream os = new FileOutputStream(outFile)) {
                        os.write(file.getData());
                    }
                }
                out("Exported "+exported+(exported == 1? " file.": " files"));
            }
            catch(IOException ioe) {
                stop("Extract: Could not read media "+ioe.getMessage());
            }
        }
        return index;
    }

    private int dir(int index, boolean detail) {
        if( index < 0 ) {
            if( detail ) {
                out("info - Gives more detailed information of the files on the media");
            }
            else {
                out("dir - Lists the file contents of the media");
            }
        }
        else {
            if( media == null ) {
                stop("Dir: No media open, use open command first");
            }

            out( media.toString() );
            try {
                for (DirEntry dir : media.getDir()) {
                    if( detail ) {
                        out(dir.toString());
                    }
                    else {
                        out(padTo(dir.getFilename(), 20) + " " + dataLength(dir.getLength()-FileInfo.INFO_LENGTH, true));
                    }
                }
            }
            catch(IOException ioe) {
                stop("Dir: Could not read media - "+ioe.getMessage());
            }
        }
        return index;
    }

    private int openMedia(int index) {
        if( index < 0 ) {
            out("open <filename> - Opens a QL media file for processing by subsequent commands");
            out(" e.g.   open myCartridge.mdv");
        }
        else {
            String filename = checkOption(index++, "Open: Missing filename");
            if( currentFile != null ) stop("Open: Media is already open");
            currentFile = new File(filename);
            if( !currentFile.canRead() ) stop("Open: Cannot read file "+filename);

            String name = currentFile.getName().toLowerCase();
            if( name.endsWith(Cartridge.MDV_EXTENSION) ) {
                media = new Cartridge("qlfiler");
                try(FileInputStream fis = new FileInputStream(currentFile) ) {
                    int errors = ((Cartridge) media).read(fis);
                    if( errors > 0 ) stop("Errors detected in .mdv file, stopping");
                }
                catch(IOException ioe) {
                    stop("Open: Could not read .mdv file "+ioe.getMessage());
                }
            }
            else if( name.endsWith(ZipFile.ZIP_EXTENSION) ) {
                media = new ZipFile(currentFile);
            }
            else if( name.endsWith(Disk.WIN_EXTENSION) ) {
                System.out.println("Reading win file "+name);
                try {
                    media = new Disk(currentFile);
                } catch (IOException|IllegalStateException ioe) {
                    stop("Open: Could not read .win file "+ioe.getMessage());
                }
            }
        }
        return index;
    }

    private String checkOption(int index, String message) {
        if( index >= args.length ) {
            System.err.println(message);
            throw new IllegalArgumentException();
        }
        return args[index];
    }

    private static void stop(String message) {
        System.err.println(message);
        throw new IllegalStateException();
    }

    private static void out(String message) {
        System.out.println(message);
    }

    private static void printAbout() {
        System.out.println("QL Filer version 1.0 by Andy Toone");
        System.out.println("Read and extract data from QL .mdv and .zip media");
        System.out.println("To run, provide a list of commands at the prompt:");
        System.out.println("  e.g. java -jar qlfiler.jar  <command list>\n");
        System.out.println("Commands: ");
        System.out.println("      help <command>   - Get help on a specific command");
        System.out.println("      open <filename>  - Open QL media (.mdv or .zip)");
        System.out.println("      dir              - Show the directory listing for the media");
        System.out.println("      info             - Show more detailed information for the media");
        System.out.println("      export <filename>- Write file(s) from the media to disk");
        System.out.println("      dump <filename>  - Hex dump of file(s) on the media");
    }
}
