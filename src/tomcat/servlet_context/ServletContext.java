package tomcat.servlet_context;

import org.apache.ibatis.io.Resources;
import org.xml.sax.SAXException;
import tomcat.filter.Filter;
import tomcat.server.StaticContentServlet;
import tomcat.servlet.HttpServlet;
import tomcat.servlet.request.RequestDispatcher;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServletContext {
    private final Map<String, Class<?>> filters = new HashMap<>();
    private final Map<Class<?>, Filter> filterInstances = new HashMap<>();
    private final Map<String, Class<?>> servlets = new HashMap<>();
    private final Map<String, String> servletMappings = new HashMap<>();
    private final Map<String, RequestDispatcher> dispatchers = new HashMap<>();
    private final Map<String, List<Filter>> filterChains = new HashMap<>();
    String contextPath;
    String webRoot;
    URLClassLoader classLoader;

    public ServletContext(String webAppDir, String contextPath) throws IOException, ParserConfigurationException, ClassNotFoundException, SAXException {
        this.webRoot = webAppDir;
        assignClassLoader();
        this.contextPath = contextPath;
        WebXmlParser xmlParser = new WebXmlParser(this);
        xmlParser.parseWebXML();
    }

    private void assignClassLoader() throws MalformedURLException {
        List<URL> urlList = new ArrayList<>();

        File classesBaseDir = new File(webRoot + "/WEB-INF/classes");
        URL classesURL = classesBaseDir.toURI().toURL();
        urlList.add(classesURL);

        File resourcesBaseDir = new File(webRoot);
        URL resourcesUrl = resourcesBaseDir.toURI().toURL();
        urlList.add(resourcesUrl);

        File jarsDir = new File(webRoot + "/WEB-INF/lib");
        File[] jars = jarsDir.listFiles();
        for (File jar : jars) {
            URL jarURL = jar.toURI().toURL();
            urlList.add(jarURL);
        }

        URL[] urls = new URL[urlList.size()];
        urlList.toArray(urls);
        classLoader = new URLClassLoader(urls);
        Resources.setDefaultClassLoader(classLoader);
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
        return dispatcher != null ? dispatcher : createDispatcher(path);
    }

    private RequestDispatcher createDispatcher(String path) {
        String servletPath;
        String pathInfo;

        for (Map.Entry<String, String> patternEntry : servletMappings.entrySet()) {
            String patternStr = patternEntry.getValue();
            Matcher matcher = getMatcher(path, patternStr);

            if (!matcher.find()) {
                continue;
            }

            servletPath = matcher.group(1);
            pathInfo = path.substring(servletPath.length());
            if (pathInfo.isEmpty() || pathInfo.startsWith("?")) {
                pathInfo = "/";
            }

            String servletName = patternEntry.getKey();
            Class<?> servletClass = servlets.get(servletName);
            HttpServlet servlet = null;
            try {
                servlet = (HttpServlet) servletClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                System.out.println("can not create servlet");
                e.printStackTrace();
            }
            List<Filter> filterChain = filterChains.get(servletName);
            return new RequestDispatcher(servlet, servletPath, pathInfo, filterChain);
        }

        HttpServlet servlet = new StaticContentServlet(this);
        return new RequestDispatcher(servlet, "", path, null);
    }

    private Matcher getMatcher(String path, String patternStr) {
        patternStr = getPatternString(patternStr);
        Pattern pattern = Pattern.compile(patternStr);
        return pattern.matcher(path);
    }

    private String getPatternString(String patternStr) {
        String newPatternStr = patternStr.replace("/*", ").*");
        return patternStr.equals(newPatternStr) ?
                "(" + newPatternStr + ")" :
                "(" + newPatternStr;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Map<String, Class<?>> getServlets() {
        return servlets;
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

    void addFilterMapping(Class<?> filterClass, String servletName) {
        List<Filter> filterChain = filterChains.computeIfAbsent(servletName, k -> new ArrayList<>());
        try {
            Filter filter = filterInstances.get(filterClass);
            if (filter == null) {
                Constructor constructor = filterClass.getDeclaredConstructor();
                filter = (Filter) constructor.newInstance();
                filterInstances.put(filterClass, filter);
            }
            filterChain.add(filter);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    void addFilter(String filterName, Class<?> filterClass) {
        filters.put(filterName, filterClass);
    }

    public Map<String, Class<?>> getFilters() {
        return filters;
    }
}