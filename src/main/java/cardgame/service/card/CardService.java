package cardgame.service.card;

import cardgame.model.Card;
import cardgame.model.Card.ElementType;
import database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CardService {

    // Eine Karte in die Datenbank speichern
    public boolean saveCard(Card card) {
        String sql = "INSERT INTO cards (id, name, damage, element_type, is_spell) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, card.getId());
            pstmt.setString(2, card.getName());
            pstmt.setDouble(3, card.getDamage());
            pstmt.setString(4, card.getElementType().name()); // Enum in String speichern
            pstmt.setBoolean(5, card.isSpell());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Alle Karten aus der Datenbank abrufen
    public List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                UUID id = (UUID) rs.getObject("id");
                String name = rs.getString("name");
                double damage = rs.getDouble("damage");
                ElementType elementType = ElementType.valueOf(rs.getString("element_type"));
                boolean isSpell = rs.getBoolean("is_spell");

                cards.add(new Card(id, name, damage, elementType, isSpell));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }
}
