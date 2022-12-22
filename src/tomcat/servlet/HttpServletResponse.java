package tomcat.servlet;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpServletResponse {
    private static final String DEFAULT_PROTOCOL = "HTTP/1.0";
    public static final int SC_OK = 200,
                            SC_BAD_REQUEST = 400,
                            SC_NOT_AUTHORISED = 401,
                            SC_FORBIDDEN = 403,
                            SC_NOT_FOUND = 404,
                            SC_NOT_IMPLEMENTED = 501;

    private final String protocol;
    private final PrintWriter writer;
    private final Socket clientSocket;
    private int status = SC_OK;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final Map<String, String> headers = new HashMap<>();


    public HttpServletResponse(Socket clientSocket, String protocol) throws IOException {
        OutputStream clientOS = clientSocket.getOutputStream();
        this.writer = new PrintWriter(clientOS);
        this.protocol = protocol != null ? protocol : DEFAULT_PROTOCOL;
        this.clientSocket = clientSocket;
    }

    public PrintWriter getWriter() {
        printStatus();
        printHeaders();
        return writer;
    }

    private void printStatus() {
        writer.print(protocol + " " + status + "\r\n");
    }

    private void printHeaders() {
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            writer.print(headerEntry.getKey() + ": " + headerEntry.getValue() + "\r\n");
        }
        writer.print("\r\n");
        writer.flush();
    }

    public void sendError(int status) throws IOException {
        this.status = status;
        printHeaders();
        clientSocket.close();
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setContentType(String contentType) {
        headers.put("Content-Type", contentType);
    }

   public OutputStream getOutputStream() {
        return out;
   }

   public void setHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
   }

   public void sendResponse() throws IOException {
        printStatus();
        setHeader("Content-Length", String.valueOf(out.size()));
        printHeaders();
        writer.flush();
        out.writeTo(clientSocket.getOutputStream());
        clientSocket.close();
   }
}
