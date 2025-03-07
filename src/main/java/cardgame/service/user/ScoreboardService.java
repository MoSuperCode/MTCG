package cardgame.service.user;

import database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardService {

    public List<Map<String, Object>> getScoreboard() {
        String sql = "SELECT username, elo, wins, losses, draws FROM users ORDER BY elo DESC";
        List<Map<String, Object>> scoreboard = new ArrayList<>();

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("Username", rs.getString("username"));
                entry.put("ELO", rs.getInt("elo"));
                entry.put("Wins", rs.getInt("wins"));
                entry.put("Losses", rs.getInt("losses"));
                entry.put("Games", rs.getInt("wins") + rs.getInt("losses") + rs.getInt("draws"));
                scoreboard.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return scoreboard;
    }
}