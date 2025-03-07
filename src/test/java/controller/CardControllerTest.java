package controller;

import cardgame.controller.CardController;
import cardgame.model.Card;
import cardgame.model.Card.ElementType;
import cardgame.service.card.CardService;
import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.http.Method;
import httpserver.server.Request;
import httpserver.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardControllerTest {

    private CardController cardController;
    private TestCardService cardService; // Test-Implementierung von CardService
    private Request request;

    @BeforeEach
    void setUp() {
        cardService = new TestCardService();
        cardController = new CardController(cardService);
        request = new Request();
    }

    @Test
    void testGetAllCards_Success() {
        // 🛠 Setup: Test-Karten in den Service hinzufügen
        cardService.addCard(new Card(UUID.randomUUID(), "Fire Dragon", 50.0, ElementType.FIRE, false));
        cardService.addCard(new Card(UUID.randomUUID(), "Water Wizard", 40.0, ElementType.WATER, true));

        request.setMethod(Method.valueOf("GET"));
        request.setPathname("/cards");

        // 🎯 Test
        Response response = cardController.handleRequest(request);

        // ✅ Erwartete Werte
        assertEquals(HttpStatus.OK.code, response.getStatus());
        assertEquals(ContentType.JSON.type, response.getContentType());
        assertTrue(response.getContent().contains("Fire Dragon"));
        assertTrue(response.getContent().contains("Water Wizard"));
    }

    @Test
    void testHandleRequest_NotFound() {
        // 🛠 Setup: Anfrage an eine ungültige Route
        request.setMethod(Method.valueOf("GET"));
        request.setPathname("/invalid");

        // 🎯 Test
        Response response = cardController.handleRequest(request);

        // ✅ Erwartete Werte
        assertEquals(HttpStatus.NOT_FOUND.code, response.getStatus());
        assertEquals(ContentType.JSON.type, response.getContentType());
        assertEquals("{\"error\":\"Not Found\"}", response.getContent());
    }

    @Test
    void testHandleRequest_WrongMethod() {
        // 🛠 Setup: Falsche HTTP-Methode (POST statt GET)
        request.setMethod(Method.valueOf("POST"));
        request.setPathname("/cards");

        // 🎯 Test
        Response response = cardController.handleRequest(request);

        // ✅ Erwartete Werte
        assertEquals(HttpStatus.NOT_FOUND.code, response.getStatus());
        assertEquals(ContentType.JSON.type, response.getContentType());
    }

    /**
     * 📌 **TestCardService (Mock für CardService ohne DB-Zugriff)**
     * Verwendet eine **In-Memory-Liste** statt der echten Datenbank.
     */
    private static class TestCardService extends CardService {
        private final List<Card> testCardList = new ArrayList<>();

        @Override
        public List<Card> getAllCards() {
            return testCardList;
        }

        public void addCard(Card card) {
            testCardList.add(card);
        }
    }
}