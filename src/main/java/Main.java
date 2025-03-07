import cardgame.controller.*;
import cardgame.service.battle.BattleService;
import cardgame.service.card.CardService;
import cardgame.service.card.DeckService;
import cardgame.service.card.PackageService;
import cardgame.service.card.TransactionService;
import cardgame.service.trading.TradingService;
import cardgame.service.user.UserProfileService;
import cardgame.service.user.StatsService;
import cardgame.service.user.ScoreboardService;
import httpserver.server.Server;
import httpserver.utils.Router;
import cardgame.service.user.UserService;
import database.Database;

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

        // 2️⃣ Server starten, wenn die Datenbankverbindung erfolgreich ist
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

        // 3️⃣ Services und Controller erstellen
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

        TradingService tradingService = new TradingService();
        TradingController tradingController = new TradingController(tradingService);

        // New services for user profiles, stats, and scoreboard
        UserProfileService userProfileService = new UserProfileService();
        UserProfileController userProfileController = new UserProfileController(userProfileService);

        StatsService statsService = new StatsService();
        StatsController statsController = new StatsController(statsService);

        ScoreboardService scoreboardService = new ScoreboardService();
        ScoreboardController scoreboardController = new ScoreboardController(scoreboardService);

        // 4️⃣ Services an die Routen binden
        router.addService("/users", userController);
        router.addService("/sessions", userController);
        router.addService("/cards", cardController);
        router.addService("/packages", packageController);
        router.addService("/transactions/packages", transactionController);
        router.addService("/deck", deckController);
        router.addService("/battles", battleController);

        // 📌 Sicherstellen, dass alle Handelsrouten erreichbar sind
        router.addService("/tradings", tradingController);
        router.addService("/tradings/", tradingController); // Damit es /tradings/<id> korrekt abfängt

        // 📌 Neue Routes für User Profiles, Stats und Scoreboard
        router.addService("/users/", userProfileController);
        router.addService("/stats", statsController);
        router.addService("/scoreboard", scoreboardController);

        System.out.println("🔍 Registrierte Routen: " + router.getRoutes());

        return router;
    }
}