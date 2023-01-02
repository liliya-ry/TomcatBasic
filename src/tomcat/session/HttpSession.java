package tomcat.session;

import tomcat.utility.Encryptor;

import java.util.HashMap;
import java.util.Map;

public class HttpSession {
    private String sessionId;
    private Map<String, Object> attributes = new HashMap<>();

    public HttpSession() {
        int randomNum = Encryptor.generateRandom();
        int salt = Encryptor.generateRandom();
        String strToEncrypt = String.valueOf(randomNum + salt);
        this.sessionId = Encryptor.encrypt(strToEncrypt);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public String getSessionId() {
        return sessionId;
    }
}
