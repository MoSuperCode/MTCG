import cardgame.controller.*;
import cardgame.service.battle.BattleService;
import cardgame.service.card.CardService;
import cardgame.service.card.DeckService;
import cardgame.service.card.PackageService;
import cardgame.service.card.TransactionService;
import httpserver.server.Server;
import httpserver.utils.Router;
import cardgame.service.user.UserService;
import database.Database; // NEU: Datenbankimport

import java.io.IOException;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // 1️⃣ Datenbankverbindung vor dem Start des Servers testen
        try (Connection conn = Database.connect()) {
            if (conn != null) {
                System.out.println("🎉 Verbindung zur Datenbank steht!");
            } else {
                System.out.println("⚠️ Verbindung fehlgeschlagen!");
                return; // Beende das Programm, falls die DB nicht läuft
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 2️⃣  Server starten, wenn die Datenbankverbindung erfolgreich ist
        Server server = new Server(10001, configureRouter());
        try {
            System.out.println("🚀 Server wird gestartet auf Port 10001...");
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Router configureRouter() {
        Router router = new Router();

        // 3️⃣ UserService erstelle mit Datenbankintegration
        UserService userService = new UserService();
        UserController userController = new UserController(userService);

        CardService cardService = new CardService();
        CardController cardController = new CardController(cardService);

        PackageService packageService = new PackageService();
        PackageController packageController = new PackageController(packageService);

        TransactionService transactionService = new TransactionService();
        TransactionController transactionController = new TransactionController(transactionService);

        DeckService deckService = new DeckService();
        DeckController deckController = new DeckController(deckService);

        BattleService battleService = new BattleService();
        BattleController battleController = new BattleController(battleService);


        // 4️⃣ Services an die Routen binden
        router.addService("/users", userController);  // Registrierung und Login von Benutzern
        router.addService("/sessions", userController);  // Login von Benutzern
        router.addService("/cards", cardController); // route für cards
        router.addService("/packages", packageController);
        router.addService("/transactions/packages", transactionController);
        router.addService("/deck", deckController);
        router.addService("/battles", battleController);

        System.out.println("🔍 Registrierte Routen: " + router.getRoutes());


        return router;
    }
}
