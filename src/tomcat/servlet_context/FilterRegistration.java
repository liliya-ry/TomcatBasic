package tomcat.servlet_context;

public class FilterRegistration {
    String filterName;
    String servletName;
    String servletPath;

    public FilterRegistration(String filterName, String servletName, String servletPath) {
        this.filterName = filterName;
        this.servletName = servletName;
        this.servletPath = servletPath;
    }
}
