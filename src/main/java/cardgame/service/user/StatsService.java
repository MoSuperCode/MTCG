package cardgame.service.user;

import database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StatsService {

    public Map<String, Object> getUserStats(String username) {
        String sql = "SELECT username, elo, wins, losses, draws FROM users WHERE username = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("Username", rs.getString("username"));
                stats.put("ELO", rs.getInt("elo"));
                stats.put("Wins", rs.getInt("wins"));
                stats.put("Losses", rs.getInt("losses"));
                stats.put("Games", rs.getInt("wins") + rs.getInt("losses") + rs.getInt("draws"));
                return stats;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}