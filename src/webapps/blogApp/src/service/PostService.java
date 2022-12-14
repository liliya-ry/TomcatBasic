package webapps.blogApp.src.service;

import org.apache.ibatis.io.Resources;
import webapps.blogApp.src.mappers.PostMapper;
import webapps.blogApp.src.model.Post;
import org.apache.ibatis.session.*;

import java.io.*;
import java.util.List;

public class PostService {
    private final SqlSessionFactory factory;

    public PostService() {
        InputStream in;
        try {
            in = Resources.getResourceAsStream("mybatis-config.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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