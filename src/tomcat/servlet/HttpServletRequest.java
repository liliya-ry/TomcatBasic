package tomcat.servlet;

import tomcat.servlet_context.ServletContext;
import tomcat.session.Cookie;
import tomcat.session.HttpSession;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class HttpServletRequest {
    private static final Set<String> VALID_PROTOCOLS;
    private static final Set<String> VALID_METHODS;

    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, ServletContext> servletContexts;
    private RequestDispatcher dispatcher;
    private String protocol;
    private String method;
    private String body = null;
    private String requestURL;
    private String contextPath;
    private String afterContextPath;
    private BufferedReader reader;
    private HttpSession session = null;
    private List<Cookie> cookies = new ArrayList<>();
    Socket clientSocket;

    static {
        VALID_PROTOCOLS = Set.of("HTTP/0.9", "HTTP/1.0", "HTTP/1.1", "HTTP/2.0.");
        VALID_METHODS = Set.of("GET", "POST", "PUT", "DELETE");
    }

    public HttpServletRequest(Socket clientSocket, Map<String, ServletContext> servletContexts) throws IOException {
        this.clientSocket = clientSocket;
        this.servletContexts = servletContexts;
        setReader(clientSocket);
        readFirstLine();
        readHeaders();
        assignCookies();
        setHost();
        readBody();
        ServletContext context = servletContexts.get(contextPath);
        dispatcher = context.getRequestDispatcher(afterContextPath);
    }

    private void setReader(Socket clientSocket) throws IOException {
        InputStream clientInputStream = clientSocket.getInputStream();
        var in = new InputStreamReader(clientInputStream);
        reader = new BufferedReader(in);
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

    private void assignCookies() throws IOException {
        String cookiesStr = headers.get("Cookie");
        if (cookiesStr == null) {
            return;
        }

        String[] cookieParts = cookiesStr.split(";");
        for (int i = 0; i < cookieParts.length; i++) {
            String[] attrParts = cookieParts[i].split("=");
            if (attrParts.length != 2) {
                throw new IOException("invalid cookie");
            }
            Cookie cookie = new Cookie(attrParts[0], attrParts[1]);
            cookies.add(cookie);
        }
    }

    private void setHost() throws IOException {
        String host = headers.getOrDefault("Host", null);
        if (host == null) {
            throw new IOException("Missing host");
        }
    }

    private void readParameters(String path) throws IOException {
        String[] pathParts = path.split("\\?");
        requestURL = pathParts[0];
        contextPath = findContextPath();
        if (contextPath == null) {
            throw new IOException("No context for url : " + requestURL);
        }
        int startIndex = contextPath.length();
        afterContextPath = requestURL.substring(startIndex);

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

    private String findContextPath() {
        for (String path : servletContexts.keySet()) {
            if (requestURL.startsWith(path)) {
                return path;
            }
        }
        return null;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getPathInfo() {
        return dispatcher.pathInfo;
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

    public String getRequestURL() {
        return requestURL;
    }

    public String getServletPath() {
        return dispatcher.servletPath;
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        ServletContext context = servletContexts.get(contextPath);
        return dispatcher = context.getRequestDispatcher(s);
    }

    public RequestDispatcher getRequestDispatcher() {
        return dispatcher;
    }

    public HttpSession getSession() {
        if (session == null) {
            session = new HttpSession();
        }

        Cookie cookie = new Cookie("JSESSIONID", session.getSessionId());
        cookie.setPath(contextPath);
        cookie.setHttpOnly(true);

        cookies.add(cookie);

        return session;
    }

    public HttpSession getSession(boolean create) {
        return create ? getSession() : null;
    }

    public Cookie[] getCookies() {
        Cookie[] cookiesArr = new Cookie[cookies.size()];
        for (int i = 0; i < cookiesArr.length; i++) {
            cookiesArr[i] = cookies.get(i);
         }
        return cookiesArr;
    }
}
