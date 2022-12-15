package tomcat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tomcat.servlet.HttpServlet;
import tomcat.servlet.HttpServletRequest;
import tomcat.servlet.HttpServletResponse;

import tomcat.utility.StatusCode;
import webapps.blogApp.src.servlets.CommentsServlet;
import webapps.blogApp.src.servlets.PostsServlet;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpServerTask implements Runnable {
    private static final String DEFAULT_PROTOCOL = "HTTP/1.0";
    private static final Logger LOGGER = LogManager.getLogger(HttpServerTask.class);
    private static final List<String> PATTERNS = List.of("/comments", "/posts");
    private static final List<HttpServlet> SERVLETS = List.of(new CommentsServlet(), new PostsServlet());

    private final Socket clientSocket;
    private final String webRoot;


    HttpServerTask(Socket clientSocket, String webRoot) {
        this.clientSocket = clientSocket;
        this.webRoot = webRoot;
    }

    @Override
    public void run() {
        try {
            HttpServletRequest request = getRequest();

            if (request == null) {
                return;
            }

            logRequest(request);

            String path = request.getContextPath();

            for (int i = 0; i < PATTERNS.size(); i++) {
                String pattern = PATTERNS.get(i);
                if (!path.startsWith(pattern)) {
                    continue;
                }

                String pathInfo = path.substring(pattern.length());
                if (pathInfo.isEmpty() || pathInfo.startsWith("/?")) {
                    pathInfo = "/";
;                }
                request.setPathInfo(pathInfo);
                System.out.println();
                HttpServletResponse response = new HttpServletResponse(clientSocket, request.getProtocol(), HttpServletResponse.SC_OK);
                HttpServlet servlet = SERVLETS.get(i);
                servlet.service(request, response);
                return;
            }

            sendNotFoundResponse(request);
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
        String userAgent = request.getHeader("User-Agent");
        LOGGER.info("{} {} {}", request.getMethod(), request.getContextPath(), userAgent);
    }

    private void logError(HttpServletRequest request, StatusCode statusCode) {
        String method = request != null ? request.getMethod() : "unknown";
        String path = request != null ? request.getContextPath() : "unknown";
        LOGGER.error("{} {} Error ({}): \"{}\"", method, path, statusCode.code, statusCode.message);
    }


    private void sendNotFoundResponse(HttpServletRequest request) throws IOException {
        String protocol = request != null ? request.getProtocol() : DEFAULT_PROTOCOL;
        HttpServletResponse response = new HttpServletResponse(clientSocket, protocol, HttpServletResponse.SC_NOT_FOUND);
        //response.sendResponse();
        logError(request, StatusCode.NOT_FOUND);
    }

    private void sendBadRequestResponse() throws IOException {
        HttpServletResponse response = new HttpServletResponse(clientSocket, DEFAULT_PROTOCOL, HttpServletResponse.SC_BAD_REQUEST);
        //response.sendResponse();
        logError(null, StatusCode.BAD_REQUEST);
    }

    private void sendOKResponse(HttpServletRequest request, Path filePath) throws IOException {
        HttpServletResponse response = new HttpServletResponse(clientSocket, request.getProtocol(), HttpServletResponse.SC_OK);
        //response.sendResponse();
    }




}