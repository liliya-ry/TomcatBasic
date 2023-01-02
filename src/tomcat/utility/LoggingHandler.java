package tomcat.utility;

import org.apache.logging.log4j.*;
import tomcat.servlet.HttpServletRequest;

public class LoggingHandler {
    private final Logger logger;

    public LoggingHandler(Class<?> loggerClass) {
        logger = LogManager.getLogger(loggerClass);
    }

    public void logRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        logger.info("{} {} {}", request.getMethod(), request.getRequestURL(), userAgent);
    }

    public void logError(HttpServletRequest request, StatusCode statusCode) {
        String method = request != null ? request.getMethod() : "unknown";
        String path = request != null ? request.getRequestURL() : "unknown";
        logger.error("{} {} Error ({}): \"{}\"", method, path, statusCode.code, statusCode.message);
    }
}
