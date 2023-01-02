package tomcat.servlet;

import tomcat.filter.Filter;
import tomcat.filter.FilterChain;
import tomcat.servlet.HttpServlet;
import tomcat.servlet.HttpServletRequest;
import tomcat.servlet.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class RequestDispatcher {
    HttpServlet servlet;
    String servletPath;
    String pathInfo;
    FilterChain filterChain;


    public RequestDispatcher(HttpServlet servlet, String servletPath, String pathInfo, List<Filter> filters) {
        this.servlet = servlet;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.filterChain = new FilterChain(filters);
    }

    public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
        filterChain.doFilter(request, response);
        servlet.service(request, response);
    }
}
