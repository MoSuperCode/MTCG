package cardgame.service.card;

import cardgame.model.Card;
import database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PackageService {

    public boolean saveCards(List<Card> cards) {
        String insertCardSQL = "INSERT INTO cards (id, name, damage, element_type, is_spell) VALUES (?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING";

        try (Connection conn = Database.connect();
             PreparedStatement cardStmt = conn.prepareStatement(insertCardSQL)) {

            for (Card card : cards) {
                cardStmt.setObject(1, card.getId());
                cardStmt.setString(2, card.getName());
                cardStmt.setDouble(3, card.getDamage());
                cardStmt.setString(4, card.getElementType().toString());
                cardStmt.setBoolean(5, card.isSpell());
                cardStmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createPackage(List<Card> cards) {
        if (cards.size() != 5) {
            return false;
        }

        if (!saveCards(cards)) {
            return false;
        }

        String insertPackageSQL = "INSERT INTO packages DEFAULT VALUES RETURNING id";
        String insertPackageCardSQL = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";

        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement packageStmt = conn.prepareStatement(insertPackageSQL);
                 PreparedStatement packageCardStmt = conn.prepareStatement(insertPackageCardSQL)) {

                ResultSet rs = packageStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                int packageId = rs.getInt(1);

                for (Card card : cards) {
                    packageCardStmt.setInt(1, packageId);
                    packageCardStmt.setObject(2, card.getId());
                    packageCardStmt.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
