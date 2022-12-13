package mappers;

import model.Post;

import java.util.List;

public interface PostMapper {
    Post getPostById(Integer id);
    List<Post> getAllPosts();
    void insertPost(Post post);
    int updatePost(Post post);
    int deletePost(Integer id);
}
