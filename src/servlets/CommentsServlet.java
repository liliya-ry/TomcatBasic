package servlets;

import static javax.servlet.http.HttpServletResponse.*;

import com.google.gson.*;
import model.Comment;
import service.CommentService;
import utility.ResponseHandler;

import javax.servlet.http.*;
import java.io.*;
import java.util.List;

public class CommentsServlet extends HttpServlet {
    private CommentService service;
    private ResponseHandler responseHandler;

    @Override
    public void init() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        service = new CommentService(this.getServletContext());
        responseHandler = new ResponseHandler(gson);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            responseHandler.sendError(response, SC_BAD_REQUEST, "Bad request: invalid url");
            return;
        }

        String postIdStr = request.getParameter("postId");
        int postId = Integer.parseInt(postIdStr);

        List<Comment> comments = service.getCommentsByPostId(postId);
        responseHandler.sendAsJson(response, comments);
    }
}
