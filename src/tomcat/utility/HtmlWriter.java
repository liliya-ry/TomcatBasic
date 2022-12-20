package tomcat.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Stream;

public class HtmlWriter {
    private static final Format FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

    public static void createListDirFile(Path dirPath, String dirFileName, String webRoot, int port) {
        try (FileOutputStream out = new FileOutputStream(dirFileName)) {
            out.write("<html>\n<header>\n".getBytes());
            String dirPathStr = dirPath.toString();
            if (dirPath.equals(Path.of(webRoot))) {
                dirPathStr = "/";
            }
            String title = "Index of " + dirPathStr;
            String titleLine = String.format("<title>%s</title>%n", title);
            out.write(titleLine.getBytes());
            out.write("</header>\n<body>\n".getBytes());
            titleLine = String.format("<h1>%s</h1>%n", title);
            out.write(titleLine.getBytes());

            int webrootLen = webRoot.length();
            try (Stream<Path> pathStream = Files.list(dirPath)) {
                pathStream.forEach(p -> writeLine(out, p, port, webrootLen));
            }

            out.write("</body>\n</html>\n".getBytes());
            out.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void writeLine(FileOutputStream out, Path p, int port, int webRootLen) {
        try {
            File f = p.toFile();

            Date date = new Date(f.lastModified());
            String modifiedDate = FORMAT.format(date);
            String fileName = f.getName();
            if (f.isDirectory()) {
                fileName += "/";
            }

            String path = p.toString().substring(webRootLen);
            path = path.replace("\\", "/");
            String link = String.format("<a href=\"http:\\\\localhost:%d%s\">%s</a>", port, path, fileName);
            String line = String.format("<p>%s  %s</p>%n", modifiedDate, link);
            out.write(line.getBytes());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
