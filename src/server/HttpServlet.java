package server;

import java.io.IOException;

public abstract class HttpServlet {
    protected void init() {}
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {}
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {}
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {}
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {}
}
