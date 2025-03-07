package controller;




import cardgame.controller.BattleController;
import cardgame.service.battle.BattleService;
import httpserver.http.Method;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BattleControllerTest {

    private BattleService battleService;
    private BattleController battleController;

    @BeforeEach
    void setUp() {
        battleService = mock(BattleService.class);
        battleController = new BattleController(battleService);
    }

    @Test
    void testHandleRequest_NotFound() {
        Request request = mock(Request.class);
        when(request.getMethod()).thenReturn(Method.valueOf("GET"));
        when(request.getPathname()).thenReturn("/invalid-path");

        Response response = battleController.handleRequest(request);

        assertEquals("HTTP/1.1 404 Not Found", response.get().split("\r\n")[0]);
    }

    @Test
    void testHandleRequest_JoinBattle_Success() {
        Request request = mock(Request.class);
        when(request.getMethod()).thenReturn(Method.valueOf("POST"));
        when(request.getPathname()).thenReturn("/battles");

        BattleController spyController = spy(battleController);
        doReturn(1).when(spyController).getUserIdFromToken(request);
        when(battleService.joinBattleQueue(1)).thenReturn(true);

        Response response = spyController.handleRequest(request);

        assertEquals("HTTP/1.1 200 OK", response.get().split("\r\n")[0]);
        assertTrue(response.get().contains("{\"message\":\"Match gefunden!\"}"));
    }

    @Test
    void testHandleRequest_JoinBattle_Waiting() {
        Request request = mock(Request.class);
        when(request.getMethod()).thenReturn(Method.valueOf("POST"));
        when(request.getPathname()).thenReturn("/battles");

        BattleController spyController = spy(battleController);
        doReturn(1).when(spyController).getUserIdFromToken(request);
        when(battleService.joinBattleQueue(1)).thenReturn(false);

        Response response = spyController.handleRequest(request);

        assertEquals("HTTP/1.1 200 OK", response.get().split("\r\n")[0]);
        assertTrue(response.get().contains("{\"message\":\"Warte auf einen Gegner...\"}"));
    }

    @Test
    void testHandleRequest_JoinBattle_InvalidToken() {
        Request request = mock(Request.class);
        when(request.getMethod()).thenReturn(Method.valueOf("POST"));
        when(request.getPathname()).thenReturn("/battles");

        BattleController spyController = spy(battleController);
        doReturn(-1).when(spyController).getUserIdFromToken(request);

        Response response = spyController.handleRequest(request);

        assertEquals("HTTP/1.1 401 Unauthorized", response.get().split("\r\n")[0]);
        assertTrue(response.get().contains("{\"error\":\"Invalid token\"}"));
    }
}
