package service;

import model.User;
import mappers.UserMapper;
import org.apache.ibatis.session.*;

import javax.servlet.ServletContext;
import java.io.InputStream;

public class UserService {
    private final SqlSessionFactory factory;

    public UserService(ServletContext sc) {
        InputStream in = sc.getResourceAsStream("/WEB-INF/mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(in);
    }

    public User getUser(String username) {
        try (SqlSession sqlSession = factory.openSession()) {
            UserMapper loginMapper = sqlSession.getMapper(UserMapper.class);
            return loginMapper.getUser(username);
        }
    }

    public void insertUser(User user) {
        try (SqlSession sqlSession = factory.openSession()) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            userMapper.insertUser(user);
            sqlSession.commit();
        }
    }
}
