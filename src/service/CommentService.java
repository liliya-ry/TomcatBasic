package service;

import model.Comment;
import mappers.CommentMapper;
import org.apache.ibatis.session.*;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.List;

public class CommentService {
    private final SqlSessionFactory factory;

    public CommentService(ServletContext sc) {
        InputStream in = sc.getResourceAsStream("/WEB-INF/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(in);
    }

    public List<Comment> getCommentsByPostId(Integer postId) {
        try (SqlSession sqlSession = factory.openSession()) {
            CommentMapper commentMapper = sqlSession.getMapper(CommentMapper.class);
            return commentMapper.getCommentsByPostId(postId);
        }
    }
}
