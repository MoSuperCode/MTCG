package cardgame.controller;

import cardgame.service.card.CardService;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;
import httpserver.server.Service;

public class CardController implements Service {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    public Response handleRequest(Request request) {
        System.out.println("🔍 Eingehender Request: " + request.getMethod() + " " + request.getPathname());

        if ("GET".equalsIgnoreCase(request.getMethod().toString().trim())
                && "/cards".equals(request.getPathname().trim())) {
            return getAllCards();
        }
        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Not Found\"}");
    }

    private Response getAllCards() {
        String json = cardService.getAllCards().toString();
        return new Response(HttpStatus.OK, ContentType.JSON, json);
    }
}
