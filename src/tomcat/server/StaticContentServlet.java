package tomcat.server;

import tomcat.servlet.*;
import tomcat.servlet.HttpServletResponse;
import tomcat.servlet.request.HttpServletRequest;
import tomcat.servlet_context.ServletContext;
import tomcat.utility.*;
import java.io.IOException;
import java.nio.file.*;

public class StaticContentServlet extends HttpServlet {
    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(StaticContentServlet.class);
    private static final String INDEX_PATH = "index.html";
    private final ServletContext context;
    public StaticContentServlet(ServletContext context) {
        this.context = context;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String afterContextStr = request.getRequestURI().substring(request.getContextPath().length() + 1);
        Path filePath = Paths.get(context.getWebRoot(), afterContextStr);
        if (!Files.exists(filePath)) {
            sendNotFoundResponse(request, response);
            return;
        }

        if (!Files.isDirectory(filePath)) {
            response.sendResponse(filePath);
            return;
        }

        sendDirResponse(request, response, filePath);
    }

    private void sendNotFoundResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        LOGGING_HANDLER.logError(request, StatusCode.NOT_FOUND);
    }

    private void sendDirResponse(HttpServletRequest request, HttpServletResponse response, Path filePath) throws IOException {
        Path indexPath = filePath.resolve(INDEX_PATH);
        if (Files.exists(indexPath)) {
            response.sendResponse(indexPath);
            return;
        }

//        if (!listDir) {
//            sendNotFoundResponse(request, response);
//            return;
//        }

        String dirName = filePath.toString();
        String dirFileName = dirName + ".html";
        Path dirFilePath = Path.of(dirFileName);
        HtmlWriter.createListDirFile(filePath, dirFileName, context.getWebRoot(), context.getContextPath(), 8081);
        response.sendResponse(dirFilePath);
    }
}
