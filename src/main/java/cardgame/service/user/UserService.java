package cardgame.service.user;

import cardgame.model.User;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final Map<String, User> userStore = new ConcurrentHashMap<>();

    public Response register(Request request) {
        String username = request.getBodyField("Username");
        String password = request.getBodyField("Password");

        if (username == null || password == null) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Fehlender Username oder Password\"}");
        }

        if (userStore.containsKey(username)) {
            return new Response(HttpStatus.CONFLICT, ContentType.JSON, "{\"error\":\"User  existiert bereits\"}");
        }

        User newUser = new User(username, password);
        userStore.put(username, newUser);

        return new Response(HttpStatus.CREATED, ContentType.JSON, "{\"message\":\"User erfolgreich registriert\"}");
    }

    public Response login(Request request) {
        String username = request.getBodyField("Username");
        String password = request.getBodyField("Password");

        if (username == null || password == null) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Fehlende Username oder Password\"}");
        }

        User user = userStore.get(username);
        if (user == null || !user.getPassword().equals(password)) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Falsche Login-Daten\"}");
        }

        // Token generieren und zuweisen
        String token = generateToken(username);
        user.setToken(token);

        return new Response(HttpStatus.OK, ContentType.JSON, "{\"token\":\"" + token + "\"}");
    }

    private String generateToken(String username) {
        return username + "-mtcgToken";
    }
}
