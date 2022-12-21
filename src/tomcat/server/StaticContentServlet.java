package tomcat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tomcat.servlet.HttpServlet;
import tomcat.servlet.HttpServletResponse;
import tomcat.servlet.request.HttpServletRequest;
import tomcat.servlet_context.ServletContext;
import tomcat.utility.StatusCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticContentServlet extends HttpServlet {
    private static final Path INDEX_PATH = Path.of("index.html");
    private static final Path NOT_FOUND_PATH = Path.of("404.html");
    private static final Path BAD_REQUEST_PATH = Path.of("400.html");
    private static final Logger LOGGER = LogManager.getLogger(StaticContentServlet.class);
    private ServletContext context;
    public StaticContentServlet(ServletContext context) {
        this.context = context;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Static servlet");
        Path filePath = Paths.get(context.getWebAppDir(), request.getRequestURI().split("/")[2]);
        System.out.println(filePath);
        if (!Files.exists(filePath)) {
            sendNotFoundResponse(request, response);
            return;
        }

        if (!Files.isDirectory(filePath)) {
            response.sendResponse(filePath);
            return;
        }
    }

    private void sendNotFoundResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.sendResponse(NOT_FOUND_PATH);
        logError(request, StatusCode.NOT_FOUND);
    }

    private void logError(HttpServletRequest request, StatusCode statusCode) {
        String method = request != null ? request.getMethod() : "unknown";
        String path = request != null ? request.getRequestURI() : "unknown";
        LOGGER.error("{} {} Error ({}): \"{}\"", method, path, statusCode.code, statusCode.message);
    }
}
