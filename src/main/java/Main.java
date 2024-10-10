import httpserver.server.Server;
import httpserver.utils.Router;
import cardgame.service.user.UserService;
import cardgame.service.card.CardService;
import cardgame.controller.UserController;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(10001, configureRouter());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Router configureRouter() {
        Router router = new Router();

        // Erstellt mir einen UserService und bindet ihn an den UserController
        UserService userService = new UserService();
        UserController userController = new UserController(userService);

        // Services zu den Routen hinzufügt
        router.addService("/users", userController);  // Registrierung und Login von Benutzern
        router.addService("/sessions", userController);  // Login von Benutzern
        //router.addService("/card", new CardService());  // Kartenservice

        return router;
    }
}
