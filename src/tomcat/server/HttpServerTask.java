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
import java.util.Map;
import java.util.regex.Pattern;

public class HttpServerTask implements Runnable {
    private static final String DEFAULT_PROTOCOL = "HTTP/1.0";
    private static final Logger LOGGER = LogManager.getLogger(HttpServerTask.class);
    private static final Map<String, HttpServlet> SERVLETS = Map.of("/comments/*", new CommentsServlet(),
            "/posts/*", new PostsServlet());

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
            int indexOfRoot = path.indexOf(webRoot);
            if (indexOfRoot != 0) {
                sendBadRequestResponse();
                return;
            }

            String servletPath = path.substring(webRoot.length());
            System.out.println(servletPath);
            HttpServlet servlet = SERVLETS.get(servletPath);
            if (servlet == null) {
                sendNotFoundResponse(request);
                return;
            }

            request.setPathInfo(path.substring(webRoot.length() + servletPath.length()));
            System.out.println(request.getPathInfo());
            HttpServletResponse response = new HttpServletResponse(clientSocket, request.getProtocol(), HttpServletResponse.SC_OK);
            servlet.service(request, response);
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