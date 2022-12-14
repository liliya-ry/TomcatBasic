package webapps.blogApp.src.service;

import org.apache.ibatis.io.Resources;
import webapps.blogApp.src.mappers.CommentMapper;
import webapps.blogApp.src.model.Comment;
import org.apache.ibatis.session.*;

import java.io.*;
import java.util.List;

public class CommentService {
    private final SqlSessionFactory factory;

    public CommentService() {
        InputStream in;
        try {
            in = Resources.getResourceAsStream("mybatis-config.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        factory = new SqlSessionFactoryBuilder().build(in);
    }

    public List<Comment> getCommentsByPostId(Integer postId) {
        try (SqlSession sqlSession = factory.openSession()) {
            CommentMapper commentMapper = sqlSession.getMapper(CommentMapper.class);
            return commentMapper.getCommentsByPostId(postId);
        }
    }
}
