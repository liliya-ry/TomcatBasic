package webapps.blogApp.src.utility;

public class ErrorResponse {
    int code;
    String message;

    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
