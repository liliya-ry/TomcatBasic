package tomcat.utility;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GzipCompressor {
    public static void gzipFile(String source, String destination) {
        byte[] buffer = new byte[1024];

        try (var fileOut = new FileOutputStream(destination);
             var gzipOut = new GZIPOutputStream(fileOut)) {
            try (var fileIn = new FileInputStream(source)) {
                for (int curRedByte = fileIn.read(buffer); curRedByte > 0; curRedByte = fileIn.read(buffer)) {
                    gzipOut.write(buffer, 0, curRedByte);
                }
            }
            gzipOut.finish();
        } catch (IOException ex) {
            System.out.println("Error while archiving file");
        }
    }
}