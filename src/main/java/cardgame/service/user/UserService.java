package cardgame.service.user;

import cardgame.model.User;
import database.Database;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    public Response register(Request request) {
        String username = request.getBodyField("Username");
        String password = request.getBodyField("Password");

        if (username == null || password == null) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Fehlender Username oder Password\"}");
        }

        // Prüfen, ob der Benutzer schon existiert
        if (userExists(username)) {
            return new Response(HttpStatus.CONFLICT, ContentType.JSON, "{\"error\":\"User existiert bereits\"}");
        }

        // Neuen Benutzer in DB speichern
        if (saveUser(username, password)) {
            return new Response(HttpStatus.CREATED, ContentType.JSON, "{\"message\":\"User erfolgreich registriert\"}");
        } else {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"Fehler beim Speichern\"}");
        }
    }

    public Response login(Request request) {
        String username = request.getBodyField("Username");
        String password = request.getBodyField("Password");

        if (username == null || password == null) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Fehlender Username oder Password\"}");
        }

        User user = getUser(username);
        if (user == null || !user.getPassword().equals(password)) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Falsche Login-Daten\"}");
        }

        // Neuen Token generieren
        String token = generateToken(username);

        // Token in DB speichern
        saveToken(username, token);

        return new Response(HttpStatus.OK, ContentType.JSON, "{\"token\":\"" + token + "\"}");
    }

    private void saveToken(String username, String token) {
        String sql = "UPDATE users SET token = ? WHERE username = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isTokenValid(String token) {
        String sql = "SELECT username FROM users WHERE token = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Falls ein Eintrag existiert, ist der Token gültig.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }




    private boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean saveUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private User getUser(String username) {
        String sql = "SELECT username, password FROM users WHERE username = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateToken(String username) {
        return username + "-mtcgToken";
    }
}
