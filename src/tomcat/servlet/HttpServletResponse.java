package tomcat.servlet;

import tomcat.session.Cookie;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpServletResponse {
    private static final String DEFAULT_PROTOCOL = "HTTP/1.0";
    public static final int SC_OK = 200,
                            SC_BAD_REQUEST = 400,
                            SC_UNAUTHORIZED = 401,
                            SC_FORBIDDEN = 403,
                            SC_NOT_FOUND = 404,
                            SC_NOT_IMPLEMENTED = 501;

    private final String protocol;
    private final PrintWriter writer;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final Map<String, String> headers = new HashMap<>();
    private int status = SC_OK;
    private HttpServletRequest request;
    private final Socket clientSocket;


    public HttpServletResponse(HttpServletRequest request) throws IOException {
        this.request = request;
        this.clientSocket = request.clientSocket;
        OutputStream clientOS = clientSocket.getOutputStream();
        this.writer = new PrintWriter(clientOS);
        this.protocol = request.getProtocol();
        setCookies();
    }

    public HttpServletResponse(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.protocol = DEFAULT_PROTOCOL;
        OutputStream clientOS = clientSocket.getOutputStream();
        this.writer = new PrintWriter(clientOS);
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
        out.writeTo(request.clientSocket.getOutputStream());
        clientSocket.close();
   }

    public void sendError(int status) throws IOException {
        this.status = status;
        printStatus();
        printHeaders();
        clientSocket.close();
    }

    public int getStatus() {
        return status;
    }

    public void addCookie(Cookie cookie) {
        String cookieStr = cookie.getName() + "=" + cookie.getValue() + ";";
        String path = cookie.getPath();
        if (path != null) {
            cookieStr += "path=" + path + ";";
        }

        boolean httpOnly = cookie.isHttpOnly();
        if (httpOnly) {
            cookieStr += "HttpOnly;";
        }

        boolean secure = cookie.isSecure();
        if (secure) {
            cookieStr += "Secure;";
        }

        headers.put("Set-Cookie", cookieStr);
    }

    private void setCookies() {
        Cookie[] cookies = request.getCookies();
        if (cookies.length == 0) {
            return;
        }

        for (Cookie cookie : cookies) {
            addCookie(cookie);
        }
    }
}
