package tomcat.servlet.request;

import tomcat.servlet_context.ServletContext;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class HttpServletRequest {
    private static final Set<String> VALID_PROTOCOLS;
    private static final Set<String> VALID_METHODS;

    private String protocol;
    private String method;
    private String requestURI;
    private String servletPath;
    private String pathInfo;

    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private String body = null;
    private BufferedReader reader;
    private final ServletContext context;
    private Class<?> servletClass;

    static {
        VALID_PROTOCOLS = Set.of("HTTP/0.9", "HTTP/1.0", "HTTP/1.1", "HTTP/2.0.");
        VALID_METHODS = Set.of("GET", "POST", "PUT", "DELETE");
    }

    public HttpServletRequest(Socket clientSocket, ServletContext context) throws IOException {
        this.context = context;
        setReader(clientSocket);
        readFirstLine();
        readHeaders();
        setHost();
        readBody();
        processPaths();
    }

    private void setReader(Socket clientSocket) throws IOException {
        InputStream clientInputStream = clientSocket.getInputStream();
        var in = new InputStreamReader(clientInputStream);
        reader =  new BufferedReader(in);
    }

    private void readFirstLine() throws IOException {
        String firstLine = reader.readLine();
        String[] firstLineParts = firstLine.split(" ");
        if (firstLineParts.length != 3) {
            throw new IOException("Missing method/protocol/path: " + firstLine);
        }

        method = firstLineParts[0];
        invalidateStr(VALID_METHODS, method, "Invalid method: ");
        String path = firstLineParts[1];
        readParameters(path);

        protocol = firstLineParts[2];
        invalidateStr(VALID_PROTOCOLS, protocol, "Invalid protocol: ");
    }

    private void invalidateStr(Set<String> validStrSet, String value, String exceptionMsg) throws IOException {
        if (!validStrSet.contains(value)) {
            throw new IOException(exceptionMsg + value);
        }
    }

    private void readBody() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (reader.ready()) {
            char ch = (char) reader.read();
            sb.append(ch);
        }

        body = !sb.isEmpty() ? sb.toString() : null;
    }

    private void readHeaders() throws IOException {
        for (String line = reader.readLine(); line != null && !line.isBlank(); line = reader.readLine()) {
            String[] parts = line.split(": ");
            if (parts.length != 2) {
                throw new IOException("Invalid headers: " + Arrays.toString(parts));
            }
            String headerName = parts[0];
            String headerValue = parts[1];
            headers.put(headerName, headerValue);
        }
    }

    private void setHost() throws IOException {
        String host = headers.getOrDefault("Host", null);
        if (host == null) {
            throw new IOException("Missing host");
        }
    }

    private void readParameters(String path) {
        String[] pathParts = path.split("\\?");
        requestURI = pathParts[0];

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

    private void processPaths() {
        int startIndex = context.getContextPath().length();
        String afterContext = requestURI.substring(startIndex);

        for (Map.Entry<String, String> patternEntry : context.getServletMappings().entrySet()) {
            String pattern = patternEntry.getValue();
            if (!afterContext.startsWith(pattern)) {
                continue;
            }

            servletPath = pattern;
            pathInfo = afterContext.substring(pattern.length());
            if (pathInfo.isEmpty() || pathInfo.startsWith("/?")) {
                pathInfo = "/";
            }

            String servletName = patternEntry.getKey();
            servletClass = context.getServlets().get(servletName);
            return;
        }
    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public String getContextPath() {
        return context.getContextPath();
    }

    void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public BufferedReader getReader() {
        StringReader stringReader = new StringReader(body);
        return new BufferedReader(stringReader);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getServletPath() {
        return servletPath;
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        processPaths();
        return new RequestDispatcher(servletClass);
    }
}