package cardgame.service.card;

import database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class DeckService {

    public boolean setDeck(int userId, List<UUID> cardIds) {
        if (cardIds.size() != 4) {
            System.out.println("❌ Ein Deck muss genau 4 Karten enthalten!");
            return false;
        }

        String deleteDeckSQL = "DELETE FROM user_deck WHERE user_id = ?";
        String insertDeckSQL = "INSERT INTO user_deck (user_id, card_id) VALUES (?, ?)";

        try (Connection conn = Database.connect()) {
            // 1️⃣ Vorheriges Deck löschen
            PreparedStatement stmt = conn.prepareStatement(deleteDeckSQL);
            stmt.setInt(1, userId);
            stmt.executeUpdate();

            // 2️⃣ Neues Deck speichern
            stmt = conn.prepareStatement(insertDeckSQL);
            for (UUID cardId : cardIds) {
                stmt.setInt(1, userId);
                stmt.setObject(2, cardId);
                stmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<UUID> getDeck(int userId) {
        List<UUID> deck = new ArrayList<>();
        String sql = "SELECT card_id FROM user_deck WHERE user_id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                deck.add((UUID) rs.getObject("card_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deck;
    }
}
