package cardgame.controller;

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

        boolean battleStarted = battleService.joinBattleQueue(userId);
        if (battleStarted) {
            return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Match gefunden!\"}");
        } else {
            return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Warte auf einen Gegner...\"}");
        }
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
