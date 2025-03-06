package cardgame.controller;

import cardgame.service.card.PackageService;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;
import httpserver.server.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PackageController implements Service {
    private final PackageService packageService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    public Response handleRequest(Request request) {
        if ("POST".equalsIgnoreCase(request.getMethod().toString().trim())
                && request.getPathname().equals("/packages")) {
            return createPackage(request);
        }
        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Not Found\"}");
    }

    private Response createPackage(Request request) {
        try {
            JsonNode jsonNode = objectMapper.readTree(request.getBody());
            if (!jsonNode.isArray() || jsonNode.size() != 5) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Ein Paket muss genau 5 Karten enthalten!\"}");
            }

            List<UUID> cardIds = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                cardIds.add(UUID.fromString(node.get("Id").asText()));
            }

            boolean success = packageService.createPackage(cardIds);
            if (success) {
                return new Response(HttpStatus.CREATED, ContentType.JSON, "{\"message\":\"Package erfolgreich erstellt\"}");
            } else {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"Fehler beim Erstellen des Pakets\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Ungültiges JSON-Format\"}");
        }
    }

}
