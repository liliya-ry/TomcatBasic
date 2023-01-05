package tomcat.servlet;

import java.util.HashMap;
import java.util.Map;

public class Cookie {
    static final String SESSION_NAME = "JSESSIONID";
    String name;
    String value;
    Map<String, String> attributes = new HashMap<>();

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return attributes.get("Domain");
    }

    public void setDomain(String domain) {
        attributes.put("Domain", domain);
    }

    public String getPath() {
        return attributes.get("Path");
    }

    public void setPath(String path) {
        attributes.put("Path", path);
    }

    public int getMaxAge() {
        String maxAgeStr = attributes.get("Max-Age");
        return maxAgeStr == null ? -1 : Integer.parseInt(maxAgeStr);
    }

    public void setMaxAge(int maxAge) {
        attributes.put("Max-Age", maxAge < 0 ? null : String.valueOf(maxAge));
    }

    public boolean isHttpOnly() {
        String httpOnlyStr = attributes.get("HttpOnly");
        return Boolean.parseBoolean(httpOnlyStr);
    }

    public void setHttpOnly(boolean httpOnly) {
        attributes.put("HttpOnly", String.valueOf(httpOnly));
    }

    public boolean isSecure() {
        String secureStr = attributes.get("Secure");
        return Boolean.parseBoolean(secureStr);
    }

    public void setSecure(boolean secure) {
        attributes.put("Secure", String.valueOf(secure));
    }

    @Override
    public String toString() {
        StringBuilder cookieBuilder = new StringBuilder();
        cookieBuilder.append(name).append("=").append(value);
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            cookieBuilder.append(";").append(attribute.getKey()).append("=").append(attribute.getValue());
        }
        return cookieBuilder.toString();
    }
}
