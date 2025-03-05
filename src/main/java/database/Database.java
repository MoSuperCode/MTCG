package database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String url = "jdbc:postgresql://localhost:5432/mtcg_db";
    private static final String user = "mo";
    private static final String password = "password";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url,user,password);
            System.out.println("Connected to database successfully ✅");
        } catch (SQLException e) {
            System.out.println("Connection failed ❌");
            e.printStackTrace();
        }
        return conn;
    }

}
