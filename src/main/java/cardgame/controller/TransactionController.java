package cardgame.controller;

import cardgame.service.card.TransactionService;
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

public class TransactionController implements Service {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public Response handleRequest(Request request) {
        if ("POST".equalsIgnoreCase(request.getMethod().toString().trim())
                && request.getPathname().equals("/transactions/packages")) {
            return buyPackage(request);
        }
        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Not Found\"}");
    }

    private Response buyPackage(Request request) {
        int userId = getUserIdFromToken(request);
        if (userId == -1) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        boolean success = transactionService.buyPackage(userId);
        if (success) {
            return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Package gekauft!\"}");
        } else {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Kauf fehlgeschlagen\"}");
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
}
