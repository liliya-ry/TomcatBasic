

import utility.GzipCompressor;
import utility.HtmlWriter;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

class HttpServerTask implements Runnable {
    private static final String DEFAULT_PROTOCOL = "HTTP/1.0";
    private static final Path INDEX_PATH = Path.of("index.html");
    private static final Path NOT_FOUND_PATH = Path.of("404.html");
    private static final Path BAD_REQUEST_PATH = Path.of("400.html");
    private static final Set<String> COMPRESSED_MIME_TYPES = Set.of("application/json", "application/xml", "image/svg+xml");
    private static final Logger LOGGER = LogManager.getLogger(HttpServerTask.class);

    private final Socket clientSocket;
    private final String webRoot;
    private final boolean listDir;
    private final int port;
    private final boolean canCompress, returnGzip;

    HttpServerTask(Socket clientSocket, String webRoot, boolean listDir, int port, boolean canCompress, boolean returnGzip) {
        this.clientSocket = clientSocket;
        this.webRoot = webRoot;
        this.listDir = listDir;
        this.port = port;
        this.canCompress = canCompress;
        this.returnGzip = returnGzip;
    }

    @Override
    public void run() {
        try {
            Request request = getRequest();

            if (request == null) {
                return;
            }

            Path filePath = Paths.get(webRoot, request.path);
            logRequest(request);
            if (!Files.exists(filePath)) {
                sendNotFoundResponse(request);
                return;
            }

            if (!Files.isDirectory(filePath)) {
                sendOKResponse(request, filePath);
                return;
            }

            sendDirResponse(request, filePath);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private Request getRequest() throws IOException {
        Request request;
        try {
            request = new Request(clientSocket);
        } catch (IOException e) {
            sendBadRequestResponse();
            return null;
        }
        return request;
    }

    private void logRequest(Request request) {
        String userAgent = request.headers.get("User-Agent");
        LOGGER.info("{} {} {}", request.method, request.path, userAgent);
    }

    private void logError(Request request, StatusCode statusCode) {
        String method = request != null ? request.method : "unknown";
        String path = request != null ? request.path : "unknown";
        LOGGER.error("{} {} Error ({}): \"{}\"", method, path, statusCode.code, statusCode.message);
    }


    private void sendNotFoundResponse(Request request) throws IOException {
        String protocol = request != null ? request.protocol : DEFAULT_PROTOCOL;
        Response response = new Response(protocol, StatusCode.NOT_FOUND, NOT_FOUND_PATH);
        response.sendResponse(clientSocket);
        logError(request, StatusCode.NOT_FOUND);
    }

    private void sendBadRequestResponse() throws IOException {
        Response response = new Response(DEFAULT_PROTOCOL, StatusCode.BAD_REQUEST, BAD_REQUEST_PATH);
        response.sendResponse(clientSocket);
        logError(null, StatusCode.BAD_REQUEST);
    }

    private void sendOKResponse(Request request, Path filePath) throws IOException {
        Path path = getPathForResponse(request, filePath);
        Response response = new Response(request.protocol, StatusCode.OK, path);
        response.sendResponse(clientSocket);
    }

    private Path getPathForResponse(Request request, Path filePath) throws IOException {
        if (!canCompress) {
            return filePath;
        }

        String acceptEncoding = request.headers.get("Accept-Encoding");
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

    private void sendDirResponse(Request request, Path filePath) throws IOException {
        Path indexPath = filePath.resolve(INDEX_PATH);
        if (Files.exists(indexPath)) {
            sendOKResponse(request, indexPath);
            return;
        }

        if (!listDir) {
            sendNotFoundResponse(request);
            return;
        }

        String dirName = filePath.toString();
        String dirFileName = dirName + ".html";
        Path dirFilePath = Path.of(dirFileName);
        HtmlWriter.createListDirFile(filePath, dirFileName, webRoot, port);

        sendOKResponse(request, dirFilePath);
    }
}
