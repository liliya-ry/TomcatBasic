package tomcat.servlet;

import tomcat.servlet.HttpServletRequest;
import tomcat.servlet.HttpServletResponse;

public class RequestDispatcher {
    private String url;

    public RequestDispatcher(String url) {
        this.url = url;
    }

    public void forward(HttpServletRequest request, HttpServletResponse response) {

    }
}
