package tomcat.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tomcat.servlet.request.HttpServletRequest;
import tomcat.utility.GzipCompressor;
import tomcat.utility.HtmlWriter;
import tomcat.utility.StatusCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class StaticContentServlet extends HttpServlet {
    private static final Path INDEX_PATH = Path.of("index.html");
    private static final Path NOT_FOUND_PATH = Path.of("404.html");
    private static final Set<String> COMPRESSED_MIME_TYPES = Set.of("application/json", "application/xml", "image/svg+xml");
    private static final Logger LOGGER = LogManager.getLogger(StaticContentServlet.class);
    private static final String WEB_ROOT = "webapps";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path filePath = Paths.get(WEB_ROOT, request.getRequestURI());
        System.out.println(filePath);

        if (!Files.exists(filePath)) {
            sendNotFoundResponse(request, response);
            return;
        }

        if (!Files.isDirectory(filePath)) {
            sendOKResponse(request, response, filePath);
            return;
        }

        sendDirResponse(request, response, filePath);
    }

    private void sendNotFoundResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.sendResponse(NOT_FOUND_PATH);
        logError(request, StatusCode.NOT_FOUND);
    }

    private void sendOKResponse(HttpServletRequest request, HttpServletResponse response, Path filePath) throws IOException {
        Path path = getPathForResponse(request, filePath);
        response.sendResponse(path);
    }

    private Path getPathForResponse(HttpServletRequest request, Path filePath) throws IOException {
        if (!canCompress) {
            return filePath;
        }

        String acceptEncoding = request.getHeader("Accept-Encoding");
        if (!acceptEncoding.contains("gzip") || !isCompressMimeType(filePath)) {
            return filePath;
        }

        String originalFile = filePath.toString();
        String archiveFile = originalFile + ".gz";
        Path archivePath = Path.of(archiveFile);

        if (!Files.exists(archivePath)) {
            GzipCompressor.gzipFile(originalFile, archiveFile);
            return filePath;
        }

        return returnGzip ? archivePath : filePath;
    }

    private boolean isCompressMimeType(Path filePath) throws IOException {
        String contentType = Files.probeContentType(filePath);
        return contentType.startsWith("text/") || COMPRESSED_MIME_TYPES.contains(contentType);
    }

    private void sendDirResponse(HttpServletRequest request, HttpServletResponse response, Path filePath) throws IOException {
        Path indexPath = filePath.resolve(INDEX_PATH);
        if (Files.exists(indexPath)) {
            sendOKResponse(request, response, indexPath);
            return;
        }

        if (!listDir) {
            sendNotFoundResponse(request, response);
            return;
        }

        String dirName = filePath.toString();
        String dirFileName = dirName + ".html";
        Path dirFilePath = Path.of(dirFileName);
        HtmlWriter.createListDirFile(filePath, dirFileName, WEB_ROOT, port);

        sendOKResponse(request, response, dirFilePath);
    }

    private void logError(HttpServletRequest request, StatusCode statusCode) {
        String method = request != null ? request.getMethod() : "unknown";
        String path = request != null ? request.getRequestURI() : "unknown";
        LOGGER.error("{} {} Error ({}): \"{}\"", method, path, statusCode.code, statusCode.message);
    }
}
