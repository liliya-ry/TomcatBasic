package tomcat.filter;

import tomcat.servlet.HttpServletResponse;
import tomcat.servlet.HttpServletRequest;

import java.io.IOException;

public interface Filter {
    void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException;
}
