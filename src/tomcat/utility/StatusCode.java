package tomcat.utility;

public enum StatusCode {
    OK(200, "OK"),
    NOT_FOUND(404, "Not found"),
    BAD_REQUEST(400, "Bad request");

    public final int code;
    public final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}