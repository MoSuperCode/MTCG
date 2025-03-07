package cardgame.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

import cardgame.service.trading.TradingService;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;
import httpserver.server.Service;
import database.Database;

import java.util.UUID;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TradingController implements Service {
    private final TradingService tradingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @Override
    public Response handleRequest(Request request) {
        System.out.println("🔍 Eingehender Request: " + request.getMethod() + " " + request.getPathname());

        switch (request.getMethod()) {
            case POST:
                if ("/tradings".equals(request.getPathname().trim())) {
                    return createTrade(request);
                } else if (request.getPathname().startsWith("/tradings/")) {
                    return acceptTrade(request);
                }
                break;
            case GET:
                if ("/tradings".equals(request.getPathname().trim())) {
                    return getAllTrades();
                }
                break;
            case DELETE:
                if (request.getPathname().startsWith("/tradings/")) {
                    return cancelTrade(request);
                }
                break;
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Not Found\"}");
    }

    // 1️⃣ Handelsangebot erstellen
    private Response createTrade(Request request) {
        int userId = getUserIdFromToken(request);
        if (userId == -1) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        try {
            // Jackson ObjectMapper für JSON Parsing
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);

            // Daten aus dem JSON extrahieren
            UUID tradeId = UUID.fromString(body.get("Id").toString());
            UUID cardId = UUID.fromString(body.get("CardToTrade").toString());
            String type = body.get("Type").toString();
            double minDamage = Double.parseDouble(body.get("MinimumDamage").toString());

            // Validiere, dass der Benutzer die Karte besitzt und sie nicht im Deck ist
            if (!userOwnsCard(userId, cardId)) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON,
                        "{\"error\":\"You don't own this card or it's in your deck\"}");
            }

            // Validiere, dass die Karte nicht bereits im Handel ist
            if (isCardInTrade(cardId)) {
                return new Response(HttpStatus.CONFLICT, ContentType.JSON,
                        "{\"error\":\"This card is already in a trade\"}");
            }

            boolean success = tradingService.createTrade(tradeId, userId, cardId, minDamage, type);
            if (success) {
                return new Response(HttpStatus.CREATED, ContentType.JSON, "");
            } else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Could not create trade offer\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Invalid request data: " + e.getMessage() + "\"}");
        }
    }

    // 2️⃣ Alle offenen Handelsangebote abrufen
    private Response getAllTrades() {
        List<String> trades = tradingService.getAllTrades();
        if (trades.isEmpty()) {
            return new Response(HttpStatus.OK, ContentType.JSON, "[]");
        }
        return new Response(HttpStatus.OK, ContentType.JSON, "[" + String.join(",", trades) + "]");
    }

    // 3️⃣ Handelsangebot annehmen
    private Response acceptTrade(Request request) {
        int buyerId = getUserIdFromToken(request);
        if (buyerId == -1) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        try {
            // Extrahiere tradeId aus der URL
            String tradeIdStr = request.getPathname().replace("/tradings/", "").trim();
            UUID tradeId = UUID.fromString(tradeIdStr);

            // Prüfe, ob der Trade existiert und nicht vom selben Benutzer erstellt wurde
            int sellerId = getTradeCreator(tradeId);
            if (sellerId == -1) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Trade not found\"}");
            }
            if (sellerId == buyerId) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{\"error\":\"You cannot accept your own trade\"}");
            }

            // Parse card ID from the request body (just a string with UUID)
            String cardIdStr = request.getBody().trim();
            // Remove quotes if present
            if (cardIdStr.startsWith("\"") && cardIdStr.endsWith("\"")) {
                cardIdStr = cardIdStr.substring(1, cardIdStr.length() - 1);
            }
            UUID offeredCardId = UUID.fromString(cardIdStr);

            // Prüfe, ob der Benutzer die angebotene Karte besitzt und sie nicht im Deck ist
            if (!userOwnsCard(buyerId, offeredCardId)) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON,
                        "{\"error\":\"You don't own this card or it's in your deck\"}");
            }

            // Trade akzeptieren
            boolean success = tradingService.acceptTrade(tradeId, buyerId, offeredCardId);
            if (success) {
                return new Response(HttpStatus.OK, ContentType.JSON, "");
            } else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Trade conditions not met or trade not found\"}");
            }

        } catch (IllegalArgumentException e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Invalid trade ID or card ID format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Invalid trade request: " + e.getMessage() + "\"}");
        }
    }

    // 4️⃣ Handelsangebot abbrechen
    private Response cancelTrade(Request request) {
        int userId = getUserIdFromToken(request);
        if (userId == -1) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid token\"}");
        }

        try {
            String tradeIdStr = request.getPathname().replace("/tradings/", "").trim();
            UUID tradeId = UUID.fromString(tradeIdStr);

            // Prüfe, ob der Trade existiert und dem Benutzer gehört
            int sellerId = getTradeCreator(tradeId);
            if (sellerId == -1) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Trade not found\"}");
            }
            if (sellerId != userId) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{\"error\":\"You can only cancel your own trades\"}");
            }

            boolean success = tradingService.cancelTrade(tradeId, userId);
            if (success) {
                return new Response(HttpStatus.OK, ContentType.JSON, "");
            } else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Trade not found or not owned by user\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Invalid trade cancellation request\"}");
        }
    }

    // 🔍 User-ID aus Token extrahieren
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

    // Hilfsmethode: Prüfen, ob ein Benutzer eine Karte besitzt und ob sie nicht im Deck ist
    private boolean userOwnsCard(int userId, UUID cardId) {
        String sql = "SELECT c.id FROM cards c " +
                "WHERE c.id = ? AND c.owner_id = ? " +
                "AND NOT EXISTS (SELECT 1 FROM user_deck ud WHERE ud.user_id = ? AND ud.card_id = ?)";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, cardId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setObject(4, cardId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hilfsmethode: Prüfen, ob eine Karte bereits im Handel ist
    private boolean isCardInTrade(UUID cardId) {
        String sql = "SELECT id FROM trades WHERE card_id = ? AND status = 'open'";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, cardId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hilfsmethode: Trade-Ersteller abrufen
    private int getTradeCreator(UUID tradeId) {
        String sql = "SELECT user_id FROM trades WHERE id = ? AND status = 'open'";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, tradeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}