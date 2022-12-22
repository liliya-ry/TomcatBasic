package tomcat.server;

import tomcat.servlet.request.*;
import tomcat.servlet_context.ServletContext;
import tomcat.servlet.*;
import tomcat.utility.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class HttpServerTask implements Runnable {
    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(HttpServerTask.class);

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
            LOGGING_HANDLER.logRequest(request);
            HttpServletResponse response = new HttpServletResponse(clientSocket, request.getProtocol());
            RequestDispatcher requestDispatcher = request.getRequestDispatcher();
            requestDispatcher.forward(request, response);
        } catch (IOException e) {
            System.out.println("Could not send response");
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

    private void sendBadRequestResponse() throws IOException {
        HttpServletResponse response = new HttpServletResponse(clientSocket, null);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        LOGGING_HANDLER.logError(null, StatusCode.BAD_REQUEST);
    }
}