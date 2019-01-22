import com.feertech.qfiler.DirEntry;
import com.feertech.qfiler.FileInfo;
import com.feertech.qfiler.QLFile;
import com.feertech.qfiler.zip.ZipFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.feertech.qfiler.DataUtils.doHex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestZip {

    @Test
    public void shouldOpenZip() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("hdutils.zip");
        File file = new File(url.getPath());

        ZipFile zf = new ZipFile(file);
        List<DirEntry> dirs = zf.getDir();
        for( DirEntry entry: dirs ) {
            System.out.println(entry.getFilename()+"  "+entry.getLength());
        }

        assertEquals(2, dirs.size());

        List<QLFile> files = zf.getFiles("drvchk*");
        assertEquals(1, files.size());
        QLFile qlfile = files.get(0);

        System.out.println(qlfile.getHeader());
    }
}
