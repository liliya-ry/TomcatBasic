package mappers;

import model.User;

public interface UserMapper {
    User getUser(String username);
    void insertUser(User user);
}
