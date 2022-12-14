package tomcat.servlet;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;

public class HttpServletResponse {
    private static final String DEFAULT_CONTENT_TYPE = "text/html";
    public static final int SC_OK = 200,
                            SC_BAD_REQUEST = 400,
                            SC_NOT_AUTHORISED = 401,
                            SC_FORBIDDEN = 403,
                            SC_NOT_FOUND = 404,
                            SC_NOT_IMPLEMENTED = 501;

    private final String protocol;
    private int status;
    private String contentType = DEFAULT_CONTENT_TYPE;

    private final Socket clientSocket;

    public HttpServletResponse(Socket clientSocket, String protocol, int status) throws IOException {
        this.clientSocket = clientSocket;
        this.protocol = protocol;
        this.status = status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public PrintWriter getWriter() throws IOException {
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
        printHeaders(writer);
        return writer;
    }

    private void printHeaders(PrintWriter writer) {
        writer.print(protocol + " " + status + "\r\n");
        writer.print("Content-Type: " + contentType + "\r\n");
        writer.print("\r\n");
        writer.flush();
    }
}
