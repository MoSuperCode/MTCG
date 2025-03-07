package cardgame.controller;

import cardgame.service.card.DeckService;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;
import httpserver.server.Service;
import database.Database;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class DeckController implements Service {
    private final DeckService deckService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    public Response handleRequest(Request request) {
        if ("GET".equalsIgnoreCase(request.getMethod().toString().trim())
                && request.getPathname().equals("/deck")) {
            return getDeck(request);
        }
        if ("PUT".equalsIgnoreCase(request.getMethod().toString().trim())
                && request.getPathname().equals("/deck")) {
            return setDeck(request);
        }
        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Not Found\"}");
    }

    private Response getDeck(Request request) {
        int userId = getUserIdFromToken(request);
        if (userId == -1) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        List<UUID> deck = deckService.getDeck(userId);
        try {
            String json = objectMapper.writeValueAsString(deck);
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"Fehler beim Abrufen des Decks\"}");
        }
    }

    private Response setDeck(Request request) {
        int userId = getUserIdFromToken(request);
        if (userId == -1) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(request.getBody());
            if (!jsonNode.isArray() || jsonNode.size() != 4) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Bad request: Ein Deck muss genau 4 Karten enthalten!\"}");
            }

            List<UUID> cardIds = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                cardIds.add(UUID.fromString(node.asText()));
            }

            boolean success = deckService.setDeck(userId, cardIds);
            if (success) {
                return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Deck gespeichert!\"}");
            } else {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"Fehler beim Speichern des Decks\"}");
            }
        } catch (Exception e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Ungültiges JSON-Format\"}");
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
