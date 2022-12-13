package model;

import java.util.Random;

public class User {
    private static final Random RANDOM = new Random();
    int id;
    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    int salt;

    public User(int id, String username, String password, String email, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getSalt() {
        return salt;
    }

    public int generateSalt() {
        return this.salt = RANDOM.nextInt();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}