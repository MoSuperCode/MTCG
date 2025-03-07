package cardgame.controller;

import cardgame.service.user.ScoreboardService;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;
import httpserver.server.Service;
import database.Database;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ScoreboardController implements Service {
    private final ScoreboardService scoreboardService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScoreboardController(ScoreboardService scoreboardService) {
        this.scoreboardService = scoreboardService;
    }

    @Override
    public Response handleRequest(Request request) {
        if ("GET".equals(request.getMethod().toString()) && "/scoreboard".equals(request.getPathname())) {
            return getScoreboard(request);
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Route not found\"}");
    }

    private Response getScoreboard(Request request) {
        String token = extractToken(request);
        if (token.isEmpty()) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        // Verify token is valid
        if (!isValidToken(token)) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        List<Map<String, Object>> scoreboard = scoreboardService.getScoreboard();
        try {
            String json = objectMapper.writeValueAsString(scoreboard);
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"Error retrieving scoreboard\"}");
        }
    }

    private String extractToken(Request request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "";
        }
        return authHeader.substring(7);
    }

    private boolean isValidToken(String token) {
        String sql = "SELECT 1 FROM users WHERE token = ?";
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}