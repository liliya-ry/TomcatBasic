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
    private final Class<?> servletClass;

    public RequestDispatcher(Class<?> servletClass) {
        this.servletClass = servletClass;
    }

    public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpServlet servlet = servlets.get(servletClass);
        if (servlet == null) {
            try {
                servlet = (HttpServlet) servletClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        servlets.put(servletClass, servlet);
        servlet.service(request, response);
    }
}
