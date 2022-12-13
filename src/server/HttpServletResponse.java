package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;

public class HttpServletResponse {
    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    String protocol;
    int status;
    String contentType;

    public HttpServletResponse(String protocol, ResponseStatus responseStatus) {
        this.protocol = protocol;
        this.status = responseStatus.code;
        this.contentType = DEFAULT_CONTENT_TYPE;
    }

    public void sendResponse(Socket clientSocket) throws IOException {
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write((protocol + "\r\n" + responseStatus.code + "  " + responseStatus).getBytes());

        clientOutput.write(("Content-Type: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.flush();

        writeContent(clientOutput, contentType);

        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        clientSocket.close();
    }

    private void writeContent(OutputStream clientOutput, String content) throws IOException {
        try (var in = new FileInputStream(content);
             var bufferIn = new BufferedInputStream(in)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = bufferIn.read(buffer, 0, buffer.length)) != -1) {
                clientOutput.write(buffer, 0, read);
                clientOutput.flush();
            }
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
