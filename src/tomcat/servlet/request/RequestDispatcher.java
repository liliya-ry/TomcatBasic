package tomcat.servlet.request;

import tomcat.server.StaticContentServlet;
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
    private final ServletContext context;
    private final String url;
    private Class<?> servletClass;
    String servletPath;
    String pathInfo;


    RequestDispatcher(ServletContext context, String url) {
        this.context = context;
        this.url = url;
        processPaths();
    }

    private void processPaths() {
        boolean hasServlet = false;

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
            hasServlet = true;
            break;
        }

        if (!hasServlet) {
            servletClass = StaticContentServlet.class;
        }
    }

    public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpServlet servlet = servlets.get(servletClass);
        if (servlet == null) {
            try {
                if (servletClass.equals(StaticContentServlet.class)) {
                    servlet = new StaticContentServlet(context);
                } else {
                    servlet = (HttpServlet) servletClass.getDeclaredConstructor().newInstance();
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        servlets.put(servletClass, servlet);
        servlet.service(request, response);
    }


}
