package tomcat.servlet;

import tomcat.utility.Encryptor;

import java.util.HashMap;
import java.util.Map;

public class HttpSession {
    private final String sessionId;
    private final Map<String, Object> attributes = new HashMap<>();
    private final ServletContext servletContext;
    boolean isNew = false;
    private final long creationTime;

    public HttpSession(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.sessionId = generateSessionId();
        this.creationTime = System.currentTimeMillis();
        servletContext.sessions.put(sessionId, this);
    }

    private String generateSessionId() {
        int randomNum = Encryptor.generateRandom();
        String generatedId = Encryptor.encrypt(String.valueOf(randomNum));

        if (servletContext.sessions.containsKey(generatedId)) {
            generateSessionId();
        }

        return generatedId;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public String getId() {
        return sessionId;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public boolean isNew() {
        return isNew;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
