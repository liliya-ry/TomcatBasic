package webapps.blogApp.src.servlets;

import com.google.gson.*;
import webapps.blogApp.src.service.CommentService;
import webapps.blogApp.src.model.Comment;
import tomcat.servlet.HttpServlet;
import tomcat.servlet.request.HttpServletRequest;
import tomcat.servlet.HttpServletResponse;
import webapps.blogApp.src.utility.ResponseHandler;

import java.io.*;
import java.util.List;

public class CommentsServlet extends HttpServlet {
    private CommentService service;
    private ResponseHandler responseHandler;

    public CommentsServlet() {
        init();
    }

    @Override
    public void init() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        service = new CommentService();
        responseHandler = new ResponseHandler(gson);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            responseHandler.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad request: invalid url");
            return;
        }

        String postIdStr = request.getParameter("postId");
        int postId = Integer.parseInt(postIdStr);

        List<Comment> comments = service.getCommentsByPostId(postId);
        responseHandler.sendAsJson(response, comments);
    }
}
