package webapps.blogApp.src.mappers;

import webapps.blogApp.src.model.Comment;

import java.util.List;

public interface CommentMapper {
    List<Comment> getCommentsByPostId(Integer postId);
}
