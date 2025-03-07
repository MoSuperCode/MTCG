package cardgame.controller;

import cardgame.service.user.StatsService;
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
import java.util.Map;

public class StatsController implements Service {
    private final StatsService statsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @Override
    public Response handleRequest(Request request) {
        if ("GET".equals(request.getMethod().toString()) && "/stats".equals(request.getPathname())) {
            return getUserStats(request);
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Route not found\"}");
    }

    private Response getUserStats(Request request) {
        String username = getUsernameFromToken(request);
        if (username.isEmpty()) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        Map<String, Object> stats = statsService.getUserStats(username);
        if (stats == null) {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Stats not found\"}");
        }

        try {
            String json = objectMapper.writeValueAsString(stats);
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"Error retrieving user stats\"}");
        }
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