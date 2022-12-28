package tomcat.filter;

import tomcat.servlet.HttpServletResponse;
import tomcat.servlet.request.HttpServletRequest;

import java.io.IOException;
import java.util.List;

public class FilterChain {
    int counter = 0;
    List<Filter> filters;

    public FilterChain(List<Filter> filters) {
        this.filters = filters;
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (filters == null || counter == filters.size()) {
            return;
        }

        Filter filter = filters.get(counter++);
        filter.doFilter(request, response, this);
    }
}
