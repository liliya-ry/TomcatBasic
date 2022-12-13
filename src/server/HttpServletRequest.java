package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;

public class HttpServletRequest {
    private static final Set<String> VALID_PROTOCOLS = Set.of("HTTP/0.9", "HTTP/1.0", "HTTP/1.1", "HTTP/2.0.");
    private static final Set<String> VALID_METHODS = Set.of("GET", "POST", "PUT", "DELETE");

    String method;
    String pathInfo;
    String protocol;
    String host;
    Map<String, String> headers = new HashMap<>();
    Map<String, String> parameters = new HashMap<>();
    String body = null;

    public HttpServletRequest(Socket clientSocket) throws IOException {
        InputStream clientInputStream = clientSocket.getInputStream();
        var in = new InputStreamReader(clientInputStream);
        BufferedReader br = new BufferedReader(in);

        String[] firstLineParts = br.readLine().split(" ");
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

        readHeaders(br);

        host = headers.getOrDefault("Host", null);
        if (host == null) {
            throw new IOException("Missing host");
        }

        readBody(br);
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
    }

    private void readParameters(String path) {
        String[] pathParts = path.split("\\?");
        pathInfo = pathParts[0];

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
        return headers.get(name);
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public String getMethod() {
        return method;
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return new RequestDispatcher(s);
    }
}
