package tomcat.servlet;

import java.io.*;
import java.net.Socket;;

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

    public HttpServletResponse(Socket clientSocket, String protocol) throws IOException {
        OutputStream clientOS = clientSocket.getOutputStream();
        this.writer = new PrintWriter(clientOS);
        this.protocol = protocol != null ? protocol : DEFAULT_PROTOCOL;
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

    private void printEnd() {
        writer.print("\r\n\r\n");
        writer.flush();
        writer.close();
    }

    public void sendError(int status) {
        this.status = status;
        printHeaders();
        printEnd();
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
