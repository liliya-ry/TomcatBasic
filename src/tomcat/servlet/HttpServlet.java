package tomcat.servlet;

import java.io.IOException;

public abstract class HttpServlet {
    protected HttpServlet() {
        init();
    }

    protected void init() {}
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {}
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {}
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {}
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {}
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (request.getMethod()) {
            case "GET" -> doGet(request, response);
            case "POST" -> doPost(request, response);
            case "PUT" -> doPut(request, response);
            case "DELETE" -> doDelete(request, response);
        }
    }
}
