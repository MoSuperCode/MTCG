package cardgame.controller;

import cardgame.service.battle.BattleResult;
import cardgame.service.battle.BattleService;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;
import httpserver.server.Service;
import database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BattleController implements Service {
    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    public Response handleRequest(Request request) {
        if ("POST".equalsIgnoreCase(request.getMethod().toString().trim())
                && request.getPathname().equals("/battles")) {
            return joinBattle(request);
        }
        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Not Found\"}");
    }

    private Response joinBattle(Request request) {
        int userId = getUserIdFromToken(request);
        if (userId == -1) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        // Check if the user has a valid deck
        if (!userHasValidDeck(userId)) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON,
                    "{\"error\":\"You need to configure a valid deck with 4 cards before battling\"}");
        }

        // Get the username for better logging
        String username = getUsernameFromId(userId);

        // Join the battle queue
        BattleResult result = battleService.joinBattleQueue(userId);

        if (result.isMatchFound()) {
            // A battle has occurred, return the battle log
            return new Response(HttpStatus.OK, ContentType.JSON,
                    "{\"message\":\"Match found!\",\"battleLog\":\"" +
                            escapeJsonString(result.getBattleLog()) + "\"}");
        } else {
            // Still waiting for an opponent
            return new Response(HttpStatus.OK, ContentType.JSON,
                    "{\"message\":\"Waiting for an opponent...\"}");
        }
    }

    // Helper method to escape JSON strings
    private String escapeJsonString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Check if user has a valid deck configured
    private boolean userHasValidDeck(int userId) {
        String sql = "SELECT COUNT(*) FROM user_deck WHERE user_id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 4; // Should have exactly 4 cards
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get username from user ID for better logging
    private String getUsernameFromId(int userId) {
        String sql = "SELECT username FROM users WHERE id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public int getUserIdFromToken(Request request) {
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
}