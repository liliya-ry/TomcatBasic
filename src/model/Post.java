package model;

public class Post {
    int id;
    int userId;
    String title;
    String body;

    public Post(int id, int userId, String title, String body) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.body = body;
    }

    public void setId(int id) {
        this.id = id;
    }
}
