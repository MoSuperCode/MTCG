package cardgame.controller;

import cardgame.service.user.UserService;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.Service;
import httpserver.http.HttpStatus;
import httpserver.http.ContentType;

public class UserController implements Service {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Response handleRequest(Request request) {
        String path = request.getPathname();
        String method = String.valueOf(request.getMethod());

        if (path.equals("/users") && method.equals("POST")) {
            return userService.register(request);
        } else if (path.equals("/sessions") && method.equals("POST")) {
            return userService.login(request);
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Route not found\"}");
    }
}
