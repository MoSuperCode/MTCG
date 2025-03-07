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
    private static final int MAX_ROUNDS = 100;

    public synchronized BattleResult joinBattleQueue(int userId) {
        battleQueue.add(userId);
        System.out.println("🔹 Player with ID " + userId + " waiting for a battle...");

        if (battleQueue.size() >= 2) {
            int player1 = battleQueue.poll();
            int player2 = battleQueue.poll();
            System.out.println("⚔ Match found: Player " + player1 + " vs. Player " + player2);
            String battleLog = startBattle(player1, player2);
            return new BattleResult(true, battleLog);
        }

        return new BattleResult(false, null);
    }

    private String startBattle(int player1, int player2) {
        List<Card> player1Deck = getDeck(player1);
        List<Card> player2Deck = getDeck(player2);

        // Create a battle log that will be returned to the clients
        StringBuilder battleLog = new StringBuilder();
        battleLog.append("Battle between Player ").append(player1).append(" and Player ").append(player2).append("\n");

        int roundCount = 0;
        boolean draw = true;

        // Continue battle until a player runs out of cards or max rounds reached
        while (!player1Deck.isEmpty() && !player2Deck.isEmpty() && roundCount < MAX_ROUNDS) {
            roundCount++;
            battleLog.append("\nRound ").append(roundCount).append(":\n");

            // Randomly select cards from each deck
            Random random = new Random();
            int p1CardIndex = random.nextInt(player1Deck.size());
            int p2CardIndex = random.nextInt(player2Deck.size());

            Card p1Card = player1Deck.get(p1CardIndex);
            Card p2Card = player2Deck.get(p2CardIndex);

            battleLog.append("Player ").append(player1).append(" plays: ").append(p1Card.getName())
                    .append(" (").append(p1Card.getDamage()).append(" damage)\n");
            battleLog.append("Player ").append(player2).append(" plays: ").append(p2Card.getName())
                    .append(" (").append(p2Card.getDamage()).append(" damage)\n");

            // Calculate the effective damage based on battle rules
            double p1EffectiveDamage = calculateEffectiveDamage(p1Card, p2Card);
            double p2EffectiveDamage = calculateEffectiveDamage(p2Card, p1Card);

            // Apply special rule effects
            applySpecialRules(p1Card, p2Card, battleLog);

            // Determine round winner
            int result = fight(p1EffectiveDamage, p2EffectiveDamage);

            if (result > 0) {
                // Player 1 wins round
                battleLog.append("Player ").append(player1).append(" wins this round!\n");
                player1Deck.add(p2Card);
                player2Deck.remove(p2CardIndex);
                draw = false;
            } else if (result < 0) {
                // Player 2 wins round
                battleLog.append("Player ").append(player2).append(" wins this round!\n");
                player2Deck.add(p1Card);
                player1Deck.remove(p1CardIndex);
                draw = false;
            } else {
                // Draw
                battleLog.append("Round ends in a draw! No cards are exchanged.\n");
            }

            battleLog.append("Cards left - Player ").append(player1).append(": ").append(player1Deck.size())
                    .append(", Player ").append(player2).append(": ").append(player2Deck.size()).append("\n");
        }

        // Determine the final winner or if it's a draw
        int winner, loser;
        if (draw) {
            battleLog.append("\nBattle ended in a DRAW after ").append(roundCount).append(" rounds!\n");
            System.out.println(battleLog.toString());
            return null;
        } else if (player1Deck.isEmpty()) {
            winner = player2;
            loser = player1;
            battleLog.append("\nPlayer ").append(player2).append(" WINS the battle!\n");
        } else {
            winner = player1;
            loser = player2;
            battleLog.append("\nPlayer ").append(player1).append(" WINS the battle!\n");
        }

        // Update the ELO scores and stats
        updateStats(winner, loser);
        System.out.println(battleLog.toString());
        return null;
    }

    private void applySpecialRules(Card card1, Card card2, StringBuilder log) {
        // Apply special battle rules
        // Goblins are afraid of Dragons
        if (card1.getName().contains("Goblin") && card2.getName().contains("Dragon")) {
            log.append("Special rule: Goblin is too afraid of Dragon to attack!\n");
            // Set card1's damage to 0
        }

        // Wizzard can control Orks
        if (card1.getName().contains("Wizard") && card2.getName().contains("Ork")) {
            log.append("Special rule: Wizard controls Ork, making it unable to attack!\n");
            // Set card2's damage to 0
        }

        // Knights drown by water spells
        if (card1.getName().contains("Knight") && card2.getName().contains("WaterSpell")) {
            log.append("Special rule: Knight drowns instantly due to heavy armor when facing WaterSpell!\n");
            // Set card1's damage to 0
        }

        // Kraken is immune to spells
        if (card1.getName().contains("Kraken") && card2.isSpell()) {
            log.append("Special rule: Kraken is immune to spells!\n");
            // Set card2's damage to 0
        }

        // FireElves can evade Dragon attacks
        if (card1.getName().contains("FireElf") && card2.getName().contains("Dragon")) {
            log.append("Special rule: FireElf evades Dragon attack!\n");
            // Set card2's damage to 0
        }
    }

    private double calculateEffectiveDamage(Card attackingCard, Card defendingCard) {
        double damage = attackingCard.getDamage();

        // Element type effectiveness only applies when at least one spell card is involved
        if (attackingCard.isSpell() || defendingCard.isSpell()) {
            // Water -> Fire (water is effective against fire)
            if (attackingCard.getElementType() == Card.ElementType.WATER &&
                    defendingCard.getElementType() == Card.ElementType.FIRE) {
                damage *= 2;  // Double damage
            }
            // Fire -> Normal (fire is effective against normal)
            else if (attackingCard.getElementType() == Card.ElementType.FIRE &&
                    defendingCard.getElementType() == Card.ElementType.NORMAL) {
                damage *= 2;  // Double damage
            }
            // Normal -> Water (normal is effective against water)
            else if (attackingCard.getElementType() == Card.ElementType.NORMAL &&
                    defendingCard.getElementType() == Card.ElementType.WATER) {
                damage *= 2;  // Double damage
            }
            // Fire -> Water (fire is not effective against water)
            else if (attackingCard.getElementType() == Card.ElementType.FIRE &&
                    defendingCard.getElementType() == Card.ElementType.WATER) {
                damage /= 2;  // Half damage
            }
            // Normal -> Fire (normal is not effective against fire)
            else if (attackingCard.getElementType() == Card.ElementType.NORMAL &&
                    defendingCard.getElementType() == Card.ElementType.FIRE) {
                damage /= 2;  // Half damage
            }
            // Water -> Normal (water is not effective against normal)
            else if (attackingCard.getElementType() == Card.ElementType.WATER &&
                    defendingCard.getElementType() == Card.ElementType.NORMAL) {
                damage /= 2;  // Half damage
            }
        }

        return damage;
    }

    private int fight(double damage1, double damage2) {
        if (damage1 > damage2) {
            return 1;  // Player 1 wins
        } else if (damage1 < damage2) {
            return -1; // Player 2 wins
        } else {
            return 0;  // Draw
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

    private void updateStats(int winnerId, int loserId) {
        // Update ELO scores
        updateElo(winnerId, 3);
        updateElo(loserId, -5);

        // Update win/loss counts
        updateWinLossRecord(winnerId, true);
        updateWinLossRecord(loserId, false);
    }

    private void updateElo(int userId, int eloChange) {
        String sql = "UPDATE users SET elo = elo + ? WHERE id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eloChange);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("📊 ELO for player " + userId + " changed by " + eloChange);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateWinLossRecord(int userId, boolean isWin) {
        String sql = isWin ?
                "UPDATE users SET wins = wins + 1 WHERE id = ?" :
                "UPDATE users SET losses = losses + 1 WHERE id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}