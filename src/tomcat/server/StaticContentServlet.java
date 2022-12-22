package tomcat.server;

import tomcat.servlet.*;
import tomcat.servlet.HttpServletResponse;
import tomcat.servlet.request.HttpServletRequest;
import tomcat.servlet_context.ServletContext;
import tomcat.utility.*;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.Date;

public class StaticContentServlet extends HttpServlet {
    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(StaticContentServlet.class);
    private static final String INDEX_PATH = "index.html";
    private final ServletContext context;
    public StaticContentServlet(ServletContext context) {
        this.context = context;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String afterContextStr = request.getRequestURL().substring(request.getContextPath().length() + 1);
        Path filePath = Paths.get(context.getWebRoot(), afterContextStr);
        if (!Files.exists(filePath)) {
            sendNotFoundResponse(request, response);
            return;
        }

        if (!Files.isDirectory(filePath)) {
            sendResponse(response, filePath);
            return;
        }

        sendDirResponse(response, filePath);
    }

    private void sendNotFoundResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        LOGGING_HANDLER.logError(request, StatusCode.NOT_FOUND);
    }

    private void sendDirResponse(HttpServletResponse response, Path filePath) throws IOException {
        Path indexPath = filePath.resolve(INDEX_PATH);
        if (Files.exists(indexPath)) {
            sendResponse(response, indexPath);
            return;
        }

        String dirName = filePath.toString();
        String dirFileName = dirName + ".html";
        Path dirFilePath = Path.of(dirFileName);
        HtmlWriter.createListDirFile(filePath, dirFileName, context.getWebRoot(), context.getContextPath(), 8081);
        sendResponse(response, dirFilePath);
    }

    public void sendResponse(HttpServletResponse response, Path filePath) throws IOException {
        String fileContentType = Files.probeContentType(filePath);
        response.setHeader("Content-Type", fileContentType);

        OutputStream clientOutput = response.getOutputStream();
        writeContent(clientOutput, filePath.toString());
        response.sendResponse();
    }

    private void writeContent(OutputStream clientOutput, String content) throws IOException {
        try (var in = new FileInputStream(content);
             var bufferIn = new BufferedInputStream(in)) {
            byte[] buffer = new byte[4 * 1024];
            for (int read; (read = bufferIn.read(buffer, 0, buffer.length)) != -1;) {
                clientOutput.write(buffer, 0, read);
                clientOutput.flush();
            }
        }
    }
}
