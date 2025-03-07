package cardgame.service.trading;

import database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradingService {

    // 1️⃣ Ein Handelsangebot erstellen
    public boolean createTrade(UUID tradeId, int userId, UUID cardId, double minDamage, String type) {
        String sql = "INSERT INTO trades (id, user_id, card_id, min_damage, type, status) VALUES (?, ?, ?, ?, ?, 'open')";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Starten einer Transaktion für Atomarität
            conn.setAutoCommit(false);

            try {
                stmt.setObject(1, tradeId);
                stmt.setInt(2, userId);
                stmt.setObject(3, cardId);
                stmt.setDouble(4, minDamage);
                stmt.setString(5, type);

                int rowsInserted = stmt.executeUpdate();

                // Erfolg: Commit der Transaktion
                conn.commit();
                return rowsInserted > 0;
            } catch (SQLException e) {
                // Fehler: Rollback der Transaktion
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                // Wiederherstellen des Auto-Commit Modus
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2️⃣ Alle offenen Handelsangebote abrufen
    public List<String> getAllTrades() {
        List<String> trades = new ArrayList<>();
        String sql = "SELECT t.id, t.user_id, t.card_id, t.min_damage, t.type, c.name, c.damage " +
                "FROM trades t " +
                "JOIN cards c ON t.card_id = c.id " +
                "WHERE t.status = 'open'";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                trades.add("{" +
                        "\"Id\":\"" + rs.getObject("id") + "\"," +
                        "\"CardToTrade\":\"" + rs.getObject("card_id") + "\"," +
                        "\"Type\":\"" + rs.getString("type") + "\"," +
                        "\"MinimumDamage\":" + rs.getDouble("min_damage") +
                        "}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trades;
    }

    // 3️⃣ Ein Handelsangebot annehmen (Tausch durchführen)
    public boolean acceptTrade(UUID tradeId, int buyerId, UUID offeredCardId) {
        String getTradeSQL = "SELECT user_id, card_id, min_damage, type FROM trades WHERE id = ? AND status = 'open'";
        String getCardSQL = "SELECT damage, element_type FROM cards WHERE id = ?";
        String updateTradeSQL = "UPDATE trades SET status = 'completed' WHERE id = ?";
        String updateCardOwnerSQL = "UPDATE cards SET owner_id = ? WHERE id = ?";

        try (Connection conn = Database.connect()) {
            // Starten einer Transaktion für Atomarität
            conn.setAutoCommit(false);

            try {
                // 🔍 Handelsangebot abrufen
                PreparedStatement getTradeStmt = conn.prepareStatement(getTradeSQL);
                getTradeStmt.setObject(1, tradeId);
                ResultSet tradeRs = getTradeStmt.executeQuery();

                if (!tradeRs.next()) {
                    conn.rollback();
                    return false;
                }

                int sellerId = tradeRs.getInt("user_id");
                UUID sellerCardId = (UUID) tradeRs.getObject("card_id");
                double minDamage = tradeRs.getDouble("min_damage");
                String requiredType = tradeRs.getString("type");

                // 🔍 Prüfen, ob die angebotene Karte die Bedingungen erfüllt
                PreparedStatement getCardStmt = conn.prepareStatement(getCardSQL);
                getCardStmt.setObject(1, offeredCardId);
                ResultSet cardRs = getCardStmt.executeQuery();

                if (!cardRs.next()) {
                    conn.rollback();
                    return false;
                }

                double offeredDamage = cardRs.getDouble("damage");
                String offeredType = cardRs.getString("element_type");

                // Convert card element_type to match the requirement type (monster/spell)
                String offeredCardType = offeredType.equalsIgnoreCase("NORMAL") ||
                        offeredType.equalsIgnoreCase("FIRE") ||
                        offeredType.equalsIgnoreCase("WATER") ? "monster" : "spell";

                // Prüfen, ob die Karte die Anforderungen erfüllt
                if (offeredDamage < minDamage || !requiredType.equalsIgnoreCase(offeredCardType)) {
                    conn.rollback();
                    return false;
                }

                // 🔄 Kartenbesitz aktualisieren
                PreparedStatement updateCardOwnerStmt = conn.prepareStatement(updateCardOwnerSQL);

                // Käuferkarte -> Verkäufer
                updateCardOwnerStmt.setInt(1, sellerId);
                updateCardOwnerStmt.setObject(2, offeredCardId);
                updateCardOwnerStmt.executeUpdate();

                // Verkäuferkarte -> Käufer
                updateCardOwnerStmt.setInt(1, buyerId);
                updateCardOwnerStmt.setObject(2, sellerCardId);
                updateCardOwnerStmt.executeUpdate();

                // ✅ Handel als abgeschlossen markieren
                PreparedStatement updateTradeStmt = conn.prepareStatement(updateTradeSQL);
                updateTradeStmt.setObject(1, tradeId);
                updateTradeStmt.executeUpdate();

                // Erfolg: Commit der Transaktion
                conn.commit();
                return true;
            } catch (SQLException e) {
                // Fehler: Rollback der Transaktion
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                // Wiederherstellen des Auto-Commit Modus
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4️⃣ Ein Handelsangebot abbrechen
    public boolean cancelTrade(UUID tradeId, int userId) {
        String sql = "DELETE FROM trades WHERE id = ? AND user_id = ? AND status = 'open'";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Starten einer Transaktion für Atomarität
            conn.setAutoCommit(false);

            try {
                stmt.setObject(1, tradeId);
                stmt.setInt(2, userId);

                int rowsDeleted = stmt.executeUpdate();

                // Erfolg: Commit der Transaktion
                conn.commit();
                return rowsDeleted > 0;
            } catch (SQLException e) {
                // Fehler: Rollback der Transaktion
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                // Wiederherstellen des Auto-Commit Modus
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}