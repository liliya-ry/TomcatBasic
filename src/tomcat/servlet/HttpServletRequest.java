package tomcat.servlet;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class HttpServletRequest {
    private static final Set<String> VALID_PROTOCOLS = Set.of("HTTP/0.9", "HTTP/1.0", "HTTP/1.1", "HTTP/2.0.");
    private static final Set<String> VALID_METHODS = Set.of("GET", "POST", "PUT", "DELETE");

    private String method;
    private String contextPath;
    private String pathInfo;
    private String protocol;
    private String host;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private String body = null;
    private Socket clientSocket;

    public HttpServletRequest(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        InputStream clientInputStream = clientSocket.getInputStream();
        var in = new InputStreamReader(clientInputStream);
        BufferedReader reader = new BufferedReader(in);

        readFirstLine(reader);
        readHeaders(reader);

        readBody(reader);
    }

    private void readFirstLine(BufferedReader reader) throws IOException {
        String[] firstLineParts = reader.readLine().split(" ");
        if (firstLineParts.length != 3) {
            throw new IOException("Missing method/protocol/path");
        }

        method = firstLineParts[0];
        if (!VALID_METHODS.contains(method)) {
            throw new IOException("Illegal method");
        }

        String path = firstLineParts[1];
        readParameters(path);

        protocol = firstLineParts[2];
        if (!VALID_PROTOCOLS.contains(protocol)) {
            throw new IOException("Invalid protocol");
        }
    }

    private void readBody(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (br.ready()) {
            char ch = (char) br.read();
            sb.append(ch);
        }

        if (!sb.isEmpty()) {
            body = sb.toString();
        }
    }

    private void readHeaders(BufferedReader br) throws IOException {
        for (String line = br.readLine(); line != null && !line.isBlank(); line = br.readLine()) {
            String[] parts = line.split(": ");
            if (parts.length != 2) {
                throw new IOException("invalid headers");
            }
            String headerName = parts[0];
            String headerValue = parts[1];
            headers.put(headerName, headerValue);
        }

        host = headers.getOrDefault("Host", null);
        if (host == null) {
            throw new IOException("Missing host");
        }
    }

    private void readParameters(String path) {
        String[] pathParts = path.split("\\?");
        contextPath = pathParts[0];

        if (pathParts.length != 2) {
            return;
        }

        String[] paramParts = pathParts[1].split("&");
        for (String param : paramParts) {
            int equalsIndex = param.indexOf("=");
            String paramName = param.substring(0, equalsIndex);
            String paramValue = param.substring(equalsIndex + 1);
            parameters.put(paramName, paramValue);
        }
    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getPathInfo() {
        return this.pathInfo;
    }

    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return new RequestDispatcher(s);
    }

    public BufferedReader getReader() {
        StringReader stringReader = new StringReader(body);
        return new BufferedReader(stringReader);
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getMethod() {
        return this.method;
    }
}
