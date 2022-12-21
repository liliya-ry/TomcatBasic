package tomcat.server;

import org.apache.logging.log4j.*;
import tomcat.servlet.request.*;
import tomcat.servlet_context.ServletContext;
import tomcat.servlet.*;
import tomcat.utility.StatusCode;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class HttpServerTask implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(HttpServerTask.class);

    private final Socket clientSocket;
    private final Map<String, ServletContext> servletContexts;

    HttpServerTask(Socket clientSocket, Map<String, ServletContext> servletContexts) {
        this.clientSocket = clientSocket;
        this.servletContexts = servletContexts;
    }

    @Override
    public void run() {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return;
            }
            logRequest(request);
            HttpServletResponse response = new HttpServletResponse(clientSocket, request.getProtocol());
            RequestDispatcher requestDispatcher = request.getRequestDispatcher();
            requestDispatcher.forward(request, response);
        } catch (IOException e) {
            LOGGER.error("Could not send response");
        }
    }

    private HttpServletRequest getRequest() throws IOException {
        HttpServletRequest request = null;
        try {
            request = new HttpServletRequest(clientSocket, servletContexts);
        } catch (IOException e) {
            sendBadRequestResponse();
            return null;
        } catch (NullPointerException e) {}
        return request;
    }

    private void logRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        LOGGER.info("{} {} {}", request.getMethod(), request.getRequestURI(), userAgent);
    }

    private void logError(HttpServletRequest request, StatusCode statusCode) {
        String method = request != null ? request.getMethod() : "unknown";
        String path = request != null ? request.getRequestURI() : "unknown";
        LOGGER.error("{} {} Error ({}): \"{}\"", method, path, statusCode.code, statusCode.message);
    }

    private void sendBadRequestResponse() throws IOException {
        HttpServletResponse response = new HttpServletResponse(clientSocket, null);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        logError(null, StatusCode.BAD_REQUEST);
    }
}