package cardgame.service.card;

import cardgame.model.Card;
import cardgame.model.Card.ElementType;
import database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CardService {

    // 1️⃣ Eine Karte in die Datenbank speichern
    public boolean saveCard(Card card) {
        String sql = "INSERT INTO cards (id, name, damage, element_type, is_spell) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            System.out.println("🔍 Speichere Karte: " + card.getId() + " - " + card.getName());

            pstmt.setObject(1, card.getId());
            pstmt.setString(2, card.getName());
            pstmt.setDouble(3, card.getDamage());
            pstmt.setString(4, card.getElementType().name());
            pstmt.setBoolean(5, card.isSpell());

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Karte erfolgreich gespeichert!");
            } else {
                System.out.println("❌ Karte wurde nicht gespeichert!");
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ SQL-Fehler beim Speichern der Karte!");
            return false;
        }
    }



    // 2️⃣ Mehrere Karten in die Datenbank speichern
    public boolean saveCards(List<Card> cards) {
        String sql = "INSERT INTO cards (id, name, damage, element_type, is_spell) "
                + "VALUES (?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // 🔹 Transaktion starten

            for (Card card : cards) {
                pstmt.setObject(1, card.getId());
                pstmt.setString(2, card.getName());
                pstmt.setDouble(3, card.getDamage());
                pstmt.setString(4, card.getElementType().name());
                pstmt.setBoolean(5, card.isSpell());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit(); // 🔹 Transaktion abschließen
            System.out.println("✅ Alle Karten erfolgreich gespeichert.");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ SQL-Fehler beim Speichern der Karten!");
            e.printStackTrace();
            return false;
        }
    }

    // 3️⃣ Alle Karten aus der Datenbank abrufen
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
            System.out.println("✅ " + cards.size() + " Karten aus der Datenbank geladen.");
        } catch (SQLException e) {
            System.err.println("❌ SQL-Fehler beim Abrufen der Karten!");
            e.printStackTrace();
        }
        return cards;
    }
}
