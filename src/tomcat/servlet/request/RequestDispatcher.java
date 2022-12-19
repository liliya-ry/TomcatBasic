package tomcat.servlet.request;

import tomcat.servlet.HttpServlet;
import tomcat.servlet.HttpServletResponse;
import tomcat.servlet.request.HttpServletRequest;
import tomcat.servlet_context.ServletContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class RequestDispatcher {
    Map<Class<?>, HttpServlet> servlets = new HashMap<>();
    private ServletContext context;
    private Class<?> servletClass;
    private String url;
    String servletPath;
    String pathInfo;


    RequestDispatcher(ServletContext context, String url) {
        this.context = context;
        this.url = url;
        processPaths();
    }

    //TODO: if servlet with this url doesn't exist
    private void processPaths() {
        for (Map.Entry<String, String> patternEntry : context.getServletMappings().entrySet()) {
            String pattern = patternEntry.getValue();
            if (!url.startsWith(pattern)) {
                continue;
            }

            servletPath = pattern;
            pathInfo = url.substring(pattern.length());
            if (pathInfo.isEmpty() || pathInfo.startsWith("/?")) {
                pathInfo = "/";
            }

            String servletName = patternEntry.getKey();
            servletClass = context.getServlets().get(servletName);
            return;
        }
    }

    public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpServlet servlet = servlets.get(servletClass);
        if (servlet == null) {
            try {
                servlet = (HttpServlet) servletClass.getDeclaredConstructor().newInstance();
                servlet.init();
                servlets.put(servletClass, servlet);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST); //change error type
                return;
            }
        }
        servlet.service(request, response);
    }


}
