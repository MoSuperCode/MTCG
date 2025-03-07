package cardgame.service.battle;

import cardgame.model.Card;
import database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BattleService {
    private static final Queue<Integer> battleQueue = new LinkedList<>();

    public synchronized boolean joinBattleQueue(int userId) {
        battleQueue.add(userId);
        System.out.println("🔹 Spieler mit ID " + userId + " wartet auf einen Kampf...");

        if (battleQueue.size() >= 2) {
            int player1 = battleQueue.poll();
            int player2 = battleQueue.poll();
            System.out.println("⚔ Match gefunden: Spieler " + player1 + " vs. Spieler " + player2);
            startBattle(player1, player2);
            return true;
        }

        return false;
    }



    private void startBattle(int player1, int player2) {
        int winner = determineWinner(player1, player2);
        int loser = (winner == player1) ? player2 : player1;

        updateElo(winner, 3);
        updateElo(loser, -5);
    }
    private int fight(Card card1, Card card2) {
        if (card1.getDamage() > card2.getDamage()) {
            return 1;  // Spieler 1 gewinnt
        } else if (card1.getDamage() < card2.getDamage()) {
            return -1; // Spieler 2 gewinnt
        } else {
            return 0;  // Unentschieden
        }
    }


    private int determineWinner(int player1, int player2) {
        List<Card> player1Deck = getDeck(player1);
        List<Card> player2Deck = getDeck(player2);

        System.out.println("⚔ Kampf beginnt: Spieler " + player1 + " vs. Spieler " + player2);
        System.out.println("🎴 Spieler " + player1 + " Deck: " + player1Deck);
        System.out.println("🎴 Spieler " + player2 + " Deck: " + player2Deck);

        while (!player1Deck.isEmpty() && !player2Deck.isEmpty()) {
            Card card1 = player1Deck.remove(0);
            Card card2 = player2Deck.remove(0);

            System.out.println("🔹 Runde: " + card1.getName() + " (P1) vs. " + card2.getName() + " (P2)");

            int result = fight(card1, card2);
            if (result > 0) {
                System.out.println("✅ Spieler 1 gewinnt die Runde!");
                player2Deck.remove(card2);
            } else if (result < 0) {
                System.out.println("✅ Spieler 2 gewinnt die Runde!");
                player1Deck.remove(card1);
            } else {
                System.out.println("🤝 Unentschieden! Beide Karten bleiben.");
            }
        }

        if (player1Deck.isEmpty()) {
            System.out.println("🏆 Spieler 2 gewinnt den Kampf!");
            return player2;
        } else {
            System.out.println("🏆 Spieler 1 gewinnt den Kampf!");
            return player1;
        }
    }

    private List<Card> getDeck(int userId) {
        List<Card> deck = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.damage, c.element_type, c.is_spell " +
                "FROM user_deck ud " +
                "JOIN cards c ON ud.card_id = c.id " +
                "WHERE ud.user_id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID id = (UUID) rs.getObject("id");
                String name = rs.getString("name");
                double damage = rs.getDouble("damage");
                Card.ElementType elementType = Card.ElementType.valueOf(rs.getString("element_type"));
                boolean isSpell = rs.getBoolean("is_spell");

                deck.add(new Card(id, name, damage, elementType, isSpell));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deck;
    }


    private void updateElo(int userId, int eloChange) {
        String sql = "UPDATE users SET elo = elo + ? WHERE id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eloChange);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("📊 ELO für Spieler " + userId + " geändert um " + eloChange);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
