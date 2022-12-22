package tomcat.servlet_context;

import org.xml.sax.SAXException;
import tomcat.server.StaticContentServlet;
import tomcat.servlet.HttpServlet;
import tomcat.servlet.request.RequestDispatcher;

import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ServletContext {
    private final Map<String, Class<?>> filters = new LinkedHashMap<>(); //key - filter name, value - filter type
    private Map<String, FilterRegistration> filterRegistrations; // key - filter name, value - filter mapping
    private final Map<String, Class<?>> servlets = new LinkedHashMap<>(); //key - servlet name, value - servlet type
    private final Map<String, String> servletMappings = new HashMap<>(); //key = servlet name, value - url-pattern
    private final Map<String, RequestDispatcher> dispatchers = new HashMap<>();
    String contextPath;
    String webRoot;

    public ServletContext(String webAppDir, String contextPath) throws IOException, ParserConfigurationException, SAXException {
        this.webRoot = webAppDir;
        this.contextPath = contextPath;
        WebXmlParser xmlParser = new WebXmlParser(this);
        xmlParser.parseWebXML();
    }

    public String getContextPath() {
        return contextPath;
    }

    public Map<String, String> getServletMappings() {
        return servletMappings;
    }

    public Map<String, Class<?>> getServlets() {
        return servlets;
    }

    public Map<String, Class<?>> getFilters() {
        return filters;
    }

    public Map<String, FilterRegistration> getFilterRegistrations() {
        return filterRegistrations;
    }

    public String getWebRoot() {
        return webRoot;
    }

    void addServlet(String servletName, Class<?> servletClass) {
        servlets.put(servletName, servletClass);
    }

    void addServletMapping(String servletName, String url) {
        servletMappings.put(servletName, url);
    }

    public URL getResource(String path) throws MalformedURLException {
        File f = new File(path);
        if (!f.exists()) {
            return null;
        }

        String p = path.substring(webRoot.length());
        p = contextPath + p.replace("\\", "/");
        String urlStr = "http://localhost:8081" + p;
        return new URL(urlStr);
    }

    public InputStream getResourceAsStream(String path) {
        try {
             return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    
    public RequestDispatcher getRequestDispatcher(String path) {
        RequestDispatcher dispatcher = dispatchers.get(path);
        if (dispatcher == null) {
            dispatcher = createDispatcher(path);
        }
        return dispatcher;
    }

    private RequestDispatcher createDispatcher(String path) {
        String servletPath = null;
        String pathInfo = null;

        for (Map.Entry<String, String> patternEntry : servletMappings.entrySet()) {
            String pattern = patternEntry.getValue();
            if (!path.startsWith(pattern)) {
                continue;
            }

            servletPath = pattern;
            pathInfo = path.substring(pattern.length());
            if (pathInfo.isEmpty() || pathInfo.startsWith("/?")) {
                pathInfo = "/";
            }

            String servletName = patternEntry.getKey();
            Class<?> servletClass = servlets.get(servletName);
            HttpServlet servlet;
            try {
                servlet = (HttpServlet) servletClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return new RequestDispatcher(servlet, servletPath, pathInfo);
        }

        HttpServlet servlet = new StaticContentServlet(this);
        return new RequestDispatcher(servlet, servletPath, pathInfo);
    }
    
    
}
