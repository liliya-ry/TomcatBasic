package tomcat.servlet;

import static tomcat.servlet.HttpServletResponse.SC_NOT_IMPLEMENTED;

import java.io.IOException;

public abstract class HttpServlet {
    protected HttpServlet() {
        init();
    }

    protected void init() {}

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(SC_NOT_IMPLEMENTED);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(SC_NOT_IMPLEMENTED);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(SC_NOT_IMPLEMENTED);
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(SC_NOT_IMPLEMENTED);
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (request.getMethod()) {
            case "GET" -> doGet(request, response);
            case "POST" -> doPost(request, response);
            case "PUT" -> doPut(request, response);
            case "DELETE" -> doDelete(request, response);
        }
    }
}
