package servlets;

import static javax.servlet.http.HttpServletResponse.*;

import com.google.gson.*;
import model.Token;
import model.User;
import service.TokenService;
import service.UserService;
import utility.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.*;
import java.time.LocalDate;
import java.util.regex.*;

@MultipartConfig
public class UsersServlet extends HttpServlet {
    private static final Pattern LOGIN_PATTERN = Pattern.compile("/login/?");
    private static final Pattern REGISTER_PATTERN = Pattern.compile("/register/?");
    private UserService userService;
    private TokenService tokenService;
    private ResponseHandler responseHandler;
    private Gson gson;

    @Override
    public void init() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        userService = new UserService(this.getServletContext());
        tokenService = new TokenService(this.getServletContext());
        responseHandler = new ResponseHandler(gson);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String pathInfo = request.getPathInfo();
        Matcher matcher = LOGIN_PATTERN.matcher(pathInfo);
        if (matcher.matches()) {
            boolean hasLoggedIn = loginUser(request, response);
            if (hasLoggedIn) {
                return;
            }
        }

        matcher = REGISTER_PATTERN.matcher(pathInfo);
        if (matcher.matches()) {
            registerUser(request, response);
        }

        responseHandler.sendError(response, SC_BAD_REQUEST, "Bad request: invalid url");
    }

    private void registerUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user;
        try (BufferedReader reader = request.getReader()) {
            user = gson.fromJson(reader, User.class);
        } catch (JsonSyntaxException e) {
            responseHandler.sendError(response, SC_BAD_REQUEST, "Bad request: invalid json");
            return;
        }

        String validPassword = user.getPassword();
        int salt = user.generateSalt();
        String encryptedPassword = PasswordEncryptor.encryptPassword(validPassword + salt);

        user.setPassword(encryptedPassword);
        userService.insertUser(user);
        responseHandler.sendAsJson(response, user);
    }

    private boolean loginUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = getPartStringFromRequest(request, "username");
        String password = getPartStringFromRequest(request, "password");

        if (username == null || password == null) {
            responseHandler.sendError(response, SC_UNAUTHORIZED, "Bad credentials: user or password is null");
            return false;
        }

        User user = userService.getUser(username);
        if (user == null) {
            responseHandler.sendError(response, SC_UNAUTHORIZED, "Bad credentials: no user with this username/password");
            return false;
        }

        String validPassword = user.getPassword();
        int salt = user.getSalt();
        String encryptedPassword = PasswordEncryptor.encryptPassword(password + salt);

        if (!encryptedPassword.equals(validPassword)) {
            responseHandler.sendError(response, SC_UNAUTHORIZED, "Bad credentials: no user with this username/password");
            return false;
        }

        Token token = tokenService.getTokenByUser(username);
        if (token == null) {
            LocalDate today = LocalDate.now();
            token = new Token("token", username, today, today.plusMonths(1));
            tokenService.insertToken(token);
        }

        response.addHeader("Authorisation", "Bearer " + token.getToken());
        responseHandler.sendAsJson(response, token);
        return true;
    }

    private String getPartStringFromRequest(HttpServletRequest request, String partName) throws IOException, ServletException {
        Part userPart = request.getPart(partName);
        String username;
        try (InputStream in = userPart.getInputStream();
             InputStreamReader inReader = new InputStreamReader(in);
             BufferedReader reader = new BufferedReader(inReader)) {
            username = reader.readLine();
        }
        return username;
    }
}
