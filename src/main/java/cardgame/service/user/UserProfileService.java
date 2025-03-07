package cardgame.service.user;

import database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserProfileService {

    public Map<String, Object> getUserProfile(String username) {
        String sql = "SELECT username, name, bio, image FROM user_profiles WHERE username = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("Username", rs.getString("username"));
                profile.put("Name", rs.getString("name"));
                profile.put("Bio", rs.getString("bio"));
                profile.put("Image", rs.getString("image"));
                return profile;
            }

            // If no profile exists, create a default one with just the username
            Map<String, Object> defaultProfile = new HashMap<>();
            defaultProfile.put("Username", username);
            defaultProfile.put("Name", "");
            defaultProfile.put("Bio", "");
            defaultProfile.put("Image", "");
            return defaultProfile;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUserProfile(String username, Map<String, Object> profileData) {
        // First check if a profile already exists
        String checkSql = "SELECT 1 FROM user_profiles WHERE username = ?";
        String insertSql = "INSERT INTO user_profiles (username, name, bio, image) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE user_profiles SET name = ?, bio = ?, image = ? WHERE username = ?";

        try (Connection conn = Database.connect()) {
            // Check if profile exists
            boolean profileExists = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                profileExists = rs.next();
            }

            // Get the profile data
            String name = (String) profileData.getOrDefault("Name", "");
            String bio = (String) profileData.getOrDefault("Bio", "");
            String image = (String) profileData.getOrDefault("Image", "");

            // Insert or update based on existence
            PreparedStatement stmt;
            if (profileExists) {
                stmt = conn.prepareStatement(updateSql);
                stmt.setString(1, name);
                stmt.setString(2, bio);
                stmt.setString(3, image);
                stmt.setString(4, username);
            } else {
                stmt = conn.prepareStatement(insertSql);
                stmt.setString(1, username);
                stmt.setString(2, name);
                stmt.setString(3, bio);
                stmt.setString(4, image);
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}