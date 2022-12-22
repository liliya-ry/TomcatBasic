package tomcat.servlet.request;

import tomcat.servlet.HttpServlet;
import tomcat.servlet.HttpServletResponse;
import java.io.IOException;

public class RequestDispatcher {
    HttpServlet servlet;
    String servletPath;
    String pathInfo;


    public RequestDispatcher(HttpServlet servlet, String servletPath, String pathInfo) {
        this.servlet = servlet;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
    }

    public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
        servlet.service(request, response);
    }
}
