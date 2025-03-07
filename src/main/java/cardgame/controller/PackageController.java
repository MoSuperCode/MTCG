package cardgame.controller;

import cardgame.model.Card;
import cardgame.service.card.PackageService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PackageController implements Service {
    private final PackageService packageService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @Override
    public Response handleRequest(Request request) {
        if ("POST".equalsIgnoreCase(request.getMethod().toString().trim())
                && request.getPathname().equals("/packages")) {
            return createPackage(request);
        }
        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Not Found\"}");
    }

    private boolean isAdmin(Request request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        String sql = "SELECT username FROM users WHERE token = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return username.equalsIgnoreCase("admin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Response createPackage(Request request) {
        System.out.println("📦 createPackage wurde aufgerufen!");
        System.out.println("🔍 Request-Body: " + request.getBody());

        try {
            JsonNode jsonNode = objectMapper.readTree(request.getBody());

            // Prüfen, ob es ein JSON-Array mit genau 5 Karten ist
            if (!jsonNode.isArray() || jsonNode.size() != 5) {
                System.out.println("⚠ Fehler: Paket enthält nicht genau 5 Karten!");
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON,
                        "{\"error\":\"Ein Paket muss genau 5 Karten enthalten!\"}");
            }

            List<Card> cards = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                System.out.println("🔍 Verarbeite Karte: " + node.toString());

                if (!node.has("Id") || !node.has("Name") || !node.has("Damage")) {
                    System.out.println("⚠ Fehler: Eine Karte enthält unvollständige Daten!");
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON,
                            "{\"error\":\"Jede Karte muss Id, Name und Damage enthalten!\"}");
                }

                try {
                    UUID cardId = UUID.fromString(node.get("Id").asText());
                    String name = node.get("Name").asText().trim();
                    double damage = node.get("Damage").asDouble();

                    // 🔍 Automatische Erkennung von `IsSpell`
                    boolean isSpell = name.toLowerCase().contains("spell");

                    // 🔍 Automatische Erkennung von `ElementType`
                    Card.ElementType elementType;
                    if (name.toLowerCase().contains("water")) {
                        elementType = Card.ElementType.WATER;
                    } else if (name.toLowerCase().contains("fire")) {
                        elementType = Card.ElementType.FIRE;
                    } else {
                        elementType = Card.ElementType.NORMAL;
                    }

                    if (name.isEmpty() || damage < 0) {
                        System.out.println("⚠ Fehler: Ungültige Werte für eine Karte!");
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON,
                                "{\"error\":\"Name darf nicht leer sein, Schaden darf nicht negativ sein!\"}");
                    }

                    Card card = new Card(cardId, name, damage, elementType, isSpell);
                    cards.add(card);
                    System.out.println("✅ Karte erfolgreich geparst: " + card);

                } catch (Exception e) {
                    System.out.println("❌ Fehler: Ungültige Werte für eine Karte: " + e.getMessage());
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON,
                            "{\"error\":\"Ungültige Werte für eine Karte!\"}");
                }
            }

            System.out.println("📦 Speichere Paket mit Karten: " + cards);
            boolean success = packageService.createPackage(cards);

            if (success) {
                System.out.println("✅ Paket erfolgreich erstellt!");
                return new Response(HttpStatus.CREATED, ContentType.JSON,
                        "{\"message\":\"Package erfolgreich erstellt\"}");
            } else {
                System.out.println("❌ Fehler beim Erstellen des Pakets in packageService!");
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
                        "{\"error\":\"Fehler beim Erstellen des Pakets\"}");
            }

        } catch (Exception e) {
            System.out.println("❌ Fehler beim Verarbeiten der Anfrage: " + e.getMessage());
            e.printStackTrace();
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON,
                    "{\"error\":\"Ungültiges JSON-Format\"}");
        }
    }
}
