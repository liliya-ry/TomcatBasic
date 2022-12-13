package mappers;

import model.Comment;

import java.util.List;

public interface CommentMapper {
    List<Comment> getCommentsByPostId(Integer postId);
}
