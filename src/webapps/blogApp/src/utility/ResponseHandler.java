package webapps.blogApp.src.utility;

import com.google.gson.Gson;
import org.apache.logging.log4j.*;
import tomcat.servlet.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ResponseHandler {
    private static final String CONTENT_TYPE = "application/json";

    private final Gson gson;
    private final Logger logger;

    public ResponseHandler(Gson gson) {
        this.gson = gson;
        this.logger = LogManager.getLogger("BlogApiLogger");
    }

    public void sendAsJson(HttpServletResponse response, Object obj) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(CONTENT_TYPE);
        String json = gson.toJson(obj);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
        }
    }

    public void sendError(HttpServletResponse response, int status, String message) {
        response.setStatus(status);
        ErrorResponse errorResponse = new ErrorResponse(status, message);
        sendAsJson(response, errorResponse);
        logger.error("{} : {}", errorResponse.code, errorResponse.message);
    }
}
