package com.feertech.qfiler;

import java.io.IOException;
import java.util.List;

public interface Media {

    List<? extends DirEntry> getDir() throws IOException;

    List<QLFile> getFiles(String match) throws IOException;
}
