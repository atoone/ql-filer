import com.feertech.qfiler.mdv.Cartridge;
import com.feertech.qfiler.mdv.ChunkedFile;
import com.feertech.qfiler.FileInfo;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCartridge {

    @Test
    public void shouldReadCartridge() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("rockfall.mdv");
        File file = new File(url.getPath());

        Cartridge cartridge = new Cartridge("Test");
        cartridge.read(new FileInputStream(file));

        System.out.println(cartridge.describe());
        Set<Integer> fileIndices = cartridge.getFileIndices();
        for(Integer index: fileIndices) {
            ChunkedFile cf = cartridge.getFile(index);
            System.out.println("  "+index+" -> "+cf.getHeader().toString()+" sectors "+cf.getSectorCount());
        }

        System.out.println("Directory: ");
        List<FileInfo> dir = cartridge.getDir();
        for( FileInfo info: dir ) {
            System.out.println(info);
        }

        assertEquals(3, dir.size());
    }

    @Test
    public void shouldDuplicateCartridge() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("rockfall.mdv");
        File file = new File(url.getPath());

        Cartridge cartridge = new Cartridge("Test");
        cartridge.read(new FileInputStream(file));

        File copy = File.createTempFile("test_", ".mdv");
        try(OutputStream out = new FileOutputStream(copy)) {
            cartridge.write(out);
        }

        assertTrue( isIdentical(file, copy) );
    }

    public boolean isIdentical(File f1, File f2) throws IOException {
        boolean identical = true;

        try (BufferedInputStream fis1 = new BufferedInputStream(new FileInputStream(f1));
            BufferedInputStream fis2 = new BufferedInputStream(new FileInputStream(f2))) {
            int b1 = 0, b2 = 0, pos = 1;
            while (b1 != -1 && b2 != -1) {
                if (b1 != b2) {
                    System.out.println("Files differ at position " + pos);
                    identical = false;
                }
                pos++;
                b1 = fis1.read();
                b2 = fis2.read();
            }
            if (b1 != b2) {
                System.out.println("Files have different length");
                identical = false;
            } else {
                System.out.println("Files are identical, you can delete one of them.");
            }
        }
        return identical;
    }
}
