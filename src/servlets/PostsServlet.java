package servlets;

import static javax.servlet.http.HttpServletResponse.*;

import com.google.gson.*;
import model.Post;
import service.PostService;
import utility.ResponseHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class PostsServlet extends HttpServlet {
    private static final Pattern SINGLE_POST_PATTERN = Pattern.compile("/(\\d+)/?");
    private static final Pattern COMMENTS_PATTERN = Pattern.compile("/(\\d+)/comments/?");
    private static final String INVALID_URL_MESSAGE = "Bad request: invalid url";
    private static final String INVALID_JSON_MESSAGE = "Bad request: invalid json";
    private static final String NO_POST_MESSAGE = "No post with id ";

    private Gson gson;
    private PostService service;
    private ResponseHandler responseHandler;

    @Override
    public void init() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        service = new PostService(this.getServletContext());
        responseHandler = new ResponseHandler(gson);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Post> posts = service.getAllPosts();
            responseHandler.sendAsJson(response, posts);
            return;
        }

        Matcher matcher = SINGLE_POST_PATTERN.matcher(pathInfo);
        if (matcher.matches()) {
            int postId = Integer.parseInt(matcher.group(1));
            Post post = service.getPostById(postId);

            if (post == null) {
                responseHandler.sendError(response, SC_NOT_FOUND, NO_POST_MESSAGE + postId);
            } else {
                responseHandler.sendAsJson(response, post);
            }
            return;
        }

        matcher = COMMENTS_PATTERN.matcher(pathInfo);
        if (matcher.matches()) {
            int postId = Integer.parseInt(matcher.group(1));
            RequestDispatcher dispatcher = request.getRequestDispatcher("/comments?postId=" + postId);
            dispatcher.forward(request, response);
            return;
        }

        responseHandler.sendError(response, SC_BAD_REQUEST, INVALID_URL_MESSAGE);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            responseHandler.sendError(response, SC_BAD_REQUEST, INVALID_URL_MESSAGE);
            return;
        }

        Post post;
        try (BufferedReader reader = request.getReader()) {
            post = gson.fromJson(reader, Post.class);
        } catch (Exception e) {
            responseHandler.sendError(response, SC_BAD_REQUEST, INVALID_JSON_MESSAGE);
            return;
        }

        service.insertPost(post);
        responseHandler.sendAsJson(response, post);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Matcher matcher = SINGLE_POST_PATTERN.matcher(pathInfo);
        if (!matcher.matches()) {
            responseHandler.sendError(response, SC_BAD_REQUEST, INVALID_URL_MESSAGE);
            return;
        }

        Post post;
        try (BufferedReader reader = request.getReader()) {
            post = gson.fromJson(reader, Post.class);
        } catch (Exception e) {
            responseHandler.sendError(response, SC_BAD_REQUEST, INVALID_JSON_MESSAGE);
            return;
        }

        int postId = Integer.parseInt(matcher.group(1));
        post.setId(postId);
        int affectedRows = service.updatePost(post);

        if (affectedRows != 1) {
          responseHandler.sendError(response, SC_NOT_FOUND, NO_POST_MESSAGE + postId + " was updated");
          return;
        }

        responseHandler.sendAsJson(response, post);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Matcher matcher = SINGLE_POST_PATTERN.matcher(pathInfo);
        if (!matcher.matches()) {
            responseHandler.sendError(response, SC_BAD_REQUEST, INVALID_URL_MESSAGE);
            return;
        }

        int postId = Integer.parseInt(matcher.group(1));
        int affectedRows = service.deletePost(postId);

        if (affectedRows != 1) {
            responseHandler.sendError(response, SC_NOT_FOUND, NO_POST_MESSAGE + postId + " was deleted");
        }
    }
}