package service;

import model.Post;
import mappers.PostMapper;
import org.apache.ibatis.session.*;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.List;

public class PostService {
    private final SqlSessionFactory factory;

    public PostService(ServletContext sc) {
        InputStream in = sc.getResourceAsStream("/WEB-INF/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(in);
    }

    public void insertPost(Post post) {
        try (SqlSession sqlSession = factory.openSession()) {
            PostMapper postMapper = sqlSession.getMapper(PostMapper.class);
            postMapper.insertPost(post);
            sqlSession.commit();
        }
    }

    public Post getPostById(Integer postId) {
        try (SqlSession sqlSession = factory.openSession()) {
            PostMapper postMapper = sqlSession.getMapper(PostMapper.class);
            return postMapper.getPostById(postId);
        }
    }

    public List<Post> getAllPosts() {
        try (SqlSession sqlSession = factory.openSession()) {
            PostMapper postMapper = sqlSession.getMapper(PostMapper.class);
            return postMapper.getAllPosts();
        }
    }

    public int updatePost(Post post) {
        try (SqlSession sqlSession = factory.openSession()) {
            PostMapper postMapper = sqlSession.getMapper(PostMapper.class);
            int affectedRows = postMapper.updatePost(post);
            sqlSession.commit();
            return affectedRows;
        }
    }

    public int deletePost(Integer postId) {
        try (SqlSession sqlSession = factory.openSession()) {
            PostMapper postMapper = sqlSession.getMapper(PostMapper.class);
            int affectedRows = postMapper.deletePost(postId);
            sqlSession.commit();
            return affectedRows;
        }
    }
}