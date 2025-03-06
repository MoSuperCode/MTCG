package cardgame.service.card;

import database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class TransactionService {

    public boolean buyPackage(int userId) {
        String getCoinsSQL = "SELECT coins FROM users WHERE id = ?";
        String updateCoinsSQL = "UPDATE users SET coins = coins - 5 WHERE id = ?";
        String getPackageSQL = "SELECT package_id FROM package_cards GROUP BY package_id LIMIT 1";
        String getCardsSQL = "SELECT card_id FROM package_cards WHERE package_id = ?";
        String deletePackageSQL = "DELETE FROM packages WHERE id = ?";

        try (Connection conn = Database.connect()) {
            // 1️⃣ Prüfen, ob der Spieler genug Coins hat
            PreparedStatement stmt = conn.prepareStatement(getCoinsSQL);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt("coins") < 5) {
                System.out.println("❌ Spieler hat nicht genug Coins!");
                return false;
            }

            // 2️⃣ Ein Paket zum Kauf abrufen
            stmt = conn.prepareStatement(getPackageSQL);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ Keine Pakete verfügbar!");
                return false;
            }
            int packageId = rs.getInt("package_id");

            // 3️⃣ Karten aus dem Paket abrufen
            stmt = conn.prepareStatement(getCardsSQL);
            stmt.setInt(1, packageId);
            rs = stmt.executeQuery();
            List<UUID> cardIds = new ArrayList<>();
            while (rs.next()) {
                cardIds.add((UUID) rs.getObject("card_id"));
            }

            // 4️⃣ Karten dem Spieler zuweisen
            String insertUserCardSQL = "INSERT INTO user_cards (user_id, card_id) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertUserCardSQL);
            for (UUID cardId : cardIds) {
                insertStmt.setInt(1, userId);
                insertStmt.setObject(2, cardId);
                insertStmt.executeUpdate();
            }

            // 5️⃣ Coins abziehen
            stmt = conn.prepareStatement(updateCoinsSQL);
            stmt.setInt(1, userId);
            stmt.executeUpdate();

            // 6️⃣ Paket löschen
            stmt = conn.prepareStatement(deletePackageSQL);
            stmt.setInt(1, packageId);
            stmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
