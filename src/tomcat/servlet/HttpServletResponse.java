package tomcat.servlet;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;;

public class HttpServletResponse {
    private static final String DEFAULT_PROTOCOL = "HTTP/1.0";
    private static final String DEFAULT_CONTENT_TYPE = "text/html";
    public static final int SC_OK = 200,
                            SC_BAD_REQUEST = 400,
                            SC_NOT_AUTHORISED = 401,
                            SC_FORBIDDEN = 403,
                            SC_NOT_FOUND = 404,
                            SC_NOT_IMPLEMENTED = 501;

    private final String protocol;
    private int status = SC_OK;
    private String contentType = DEFAULT_CONTENT_TYPE;
    private final PrintWriter writer;
    private Socket clientSocket;

    public HttpServletResponse(Socket clientSocket, String protocol) throws IOException {
        OutputStream clientOS = clientSocket.getOutputStream();
        this.writer = new PrintWriter(clientOS);
        this.protocol = protocol != null ? protocol : DEFAULT_PROTOCOL;
        this.clientSocket = clientSocket;
    }

    public PrintWriter getWriter() {
        printHeaders();
        return writer;
    }

    private void printHeaders() {
        writer.print(protocol + " " + status + "\r\n");
        writer.print("Content-Type: " + contentType + "\r\n");
        writer.print("\r\n");
        writer.flush();
    }

    public void sendError(int status) {
        this.status = status;
        printHeaders();
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void sendResponse(Path filePath) throws IOException {
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write((protocol + " " + status + "\r\n").getBytes());

        File f = filePath.toFile();
        clientOutput.write(("Content-Length: " + f.length() + "\r\n").getBytes());
        clientOutput.write(("Last-Modified: " + new Date(f.lastModified()) + "\r\n").getBytes());
        clientOutput.write(("Date: " + Date.from(Instant.now()) + "\r\n").getBytes());

        String content = filePath.toString();
        if (content.endsWith(".gz")) {
            clientOutput.write(("Content-Encoding: gzip\r\n").getBytes());
            String originalFile = content.substring(0, content.length() - 3);
            filePath = Path.of(originalFile);
        }

        String contentType = Files.probeContentType(filePath);
        clientOutput.write(("Content-Type: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.flush();

        writeContent(clientOutput, content);

        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        clientSocket.close();
    }

    private void writeContent(OutputStream clientOutput, String content) throws IOException {
        try (var in = new FileInputStream(content);
             var bufferIn = new BufferedInputStream(in)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = bufferIn.read(buffer, 0, buffer.length)) != -1) {
                clientOutput.write(buffer, 0, read);
                clientOutput.flush();
            }
        }
    }
}
