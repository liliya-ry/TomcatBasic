package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

public class HttpServerTask implements Runnable {
    private static final String DEFAULT_PROTOCOL = "HTTP/1.0";
    private static final Logger LOGGER = LogManager.getLogger(HttpServerTask.class);

    private final Socket clientSocket;
    private final String webRoot;
    private final int port;

    public HttpServerTask(Socket clientSocket, String webRoot, int port) {
        this.clientSocket = clientSocket;
        this.webRoot = webRoot;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            HttpServletRequest request = getRequest();

            if (request == null) {
                return;
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private HttpServletRequest getRequest() throws IOException {
        HttpServletRequest request;
        try {
            request = new HttpServletRequest(clientSocket);
        } catch (IOException e) {
            sendBadRequestResponse();
            return null;
        }
        return request;
    }

    private void logRequest(HttpServletRequest request) {
        String userAgent = request.headers.get("User-Agent");
        LOGGER.info("{} {} {}", request.method, request.getPathInfo(), userAgent);
    }

    private void logError(HttpServletRequest request, ResponseStatus responseStatus) {
        String method = request != null ? request.method : "unknown";
        String path = request != null ? request.getPathInfo() : "unknown";
        LOGGER.error("{} {} Error ({}): \"{}\"", method, path, responseStatus.code, responseStatus.message);
    }


    private void sendNotFoundResponse(HttpServletRequest request) throws IOException {
        String protocol = request != null ? request.protocol : DEFAULT_PROTOCOL;
        HttpServletResponse response = new HttpServletResponse(protocol, ResponseStatus.NOT_FOUND);
        response.sendResponse(clientSocket);
        logError(request, ResponseStatus.NOT_FOUND);
    }

    private void sendBadRequestResponse() throws IOException {
        HttpServletResponse response = new HttpServletResponse(DEFAULT_PROTOCOL, ResponseStatus.BAD_REQUEST);
        response.sendResponse(clientSocket);
        logError(null, ResponseStatus.BAD_REQUEST);
    }

    private void sendOKResponse(HttpServletRequest request, Path filePath) throws IOException {
        HttpServletResponse response = new HttpServletResponse(request.protocol, ResponseStatus.OK);
        response.sendResponse(clientSocket);
    }
}
