package cardgame.service.card;

import database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PackageService {

    public boolean createPackage(List<UUID> cardIds) {
        if (cardIds.size() != 5) {
            System.out.println("❌ Ein Paket muss genau 5 Karten enthalten!");
            return false;
        }

        String insertPackageSQL = "INSERT INTO packages DEFAULT VALUES RETURNING id";
        String insertPackageCardSQL = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement packageStmt = conn.prepareStatement(insertPackageSQL);
             PreparedStatement packageCardStmt = conn.prepareStatement(insertPackageCardSQL)) {

            ResultSet rs = packageStmt.executeQuery();
            if (rs.next()) {
                int packageId = rs.getInt(1);

                for (UUID cardId : cardIds) {
                    packageCardStmt.setInt(1, packageId);
                    packageCardStmt.setObject(2, cardId);
                    packageCardStmt.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
