package cardgame.controller;

import cardgame.service.user.UserProfileService;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;
import httpserver.server.Service;
import database.Database;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfileController implements Service {
    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public Response handleRequest(Request request) {
        String method = request.getMethod().toString();
        String pathname = request.getPathname();

        // Extract username from path like /users/kienboec
        if (pathname.startsWith("/users/")) {
            String username = pathname.substring(7);

            if ("GET".equals(method)) {
                return getUserProfile(request, username);
            } else if ("PUT".equals(method)) {
                return updateUserProfile(request, username);
            }
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Route not found\"}");
    }

    private Response getUserProfile(Request request, String username) {
        int userId = getUserIdFromToken(request);
        if (userId == -1) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        String tokenUsername = getUsernameFromToken(request);
        if (!tokenUsername.equals(username)) {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{\"error\":\"You can only access your own profile\"}");
        }

        Map<String, Object> profile = userProfileService.getUserProfile(username);
        if (profile == null) {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"User not found\"}");
        }

        try {
            String json = objectMapper.writeValueAsString(profile);
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"Error retrieving user profile\"}");
        }
    }

    private Response updateUserProfile(Request request, String username) {
        int userId = getUserIdFromToken(request);
        if (userId == -1) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        String tokenUsername = getUsernameFromToken(request);
        if (!tokenUsername.equals(username)) {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{\"error\":\"You can only update your own profile\"}");
        }

        try {
            Map<String, Object> profileData = objectMapper.readValue(request.getBody(), Map.class);
            boolean success = userProfileService.updateUserProfile(username, profileData);

            if (success) {
                return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"User profile updated\"}");
            } else {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"Error updating user profile\"}");
            }
        } catch (Exception e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Invalid profile data\"}");
        }
    }

    private int getUserIdFromToken(Request request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return -1;
        }

        String token = authHeader.substring(7);
        String sql = "SELECT id FROM users WHERE token = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private String getUsernameFromToken(Request request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "";
        }

        String token = authHeader.substring(7);
        String sql = "SELECT username FROM users WHERE token = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
