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
        String updateCardOwnerSQL = "UPDATE cards SET owner_id = ? WHERE id = ?"; // Add this line

        try (Connection conn = Database.connect()) {
            // Start a transaction for atomicity
            conn.setAutoCommit(false);

            try {
                // 1️⃣ Prüfen, ob der Spieler genug Coins hat
                PreparedStatement stmt = conn.prepareStatement(getCoinsSQL);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt("coins") < 5) {
                    System.out.println("❌ Spieler hat nicht genug Coins!");
                    conn.rollback();
                    return false;
                }

                // 2️⃣ Ein Paket zum Kauf abrufen
                stmt = conn.prepareStatement(getPackageSQL);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("❌ Keine Pakete verfügbar!");
                    conn.rollback();
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

                // 4a️⃣ NEW: Update card ownership in the cards table
                PreparedStatement updateOwnerStmt = conn.prepareStatement(updateCardOwnerSQL);

                for (UUID cardId : cardIds) {
                    // Insert into user_cards table
                    insertStmt.setInt(1, userId);
                    insertStmt.setObject(2, cardId);
                    insertStmt.executeUpdate();

                    // Update ownership in cards table
                    updateOwnerStmt.setInt(1, userId);
                    updateOwnerStmt.setObject(2, cardId);
                    updateOwnerStmt.executeUpdate();

                    System.out.println("✅ Card " + cardId + " assigned to user " + userId);
                }

                // 5️⃣ Coins abziehen
                stmt = conn.prepareStatement(updateCoinsSQL);
                stmt.setInt(1, userId);
                stmt.executeUpdate();

                // 6️⃣ Paket löschen
                stmt = conn.prepareStatement(deletePackageSQL);
                stmt.setInt(1, packageId);
                stmt.executeUpdate();

                // Commit the transaction
                conn.commit();
                return true;

            } catch (SQLException e) {
                // Roll back in case of error
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
