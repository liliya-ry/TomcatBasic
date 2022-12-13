package model;

public class Comment {
    int id;
    int postId;
    String name;
    String email;
    String body;

    public Comment(int id, int postId, String name, String email, String body) {
        this.id = id;
        this.postId = postId;
        this.name = name;
        this.email = email;
        this.body = body;
    }
}
