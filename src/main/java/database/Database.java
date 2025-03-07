package database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String url = "jdbc:postgresql://localhost:5432/mtcg_db";
    private static final String user = "mo";
    private static final String password = "password";

    public static Connection connect() throws SQLException {
        System.out.println("🔗 Verbinde zur Datenbank...");
        Connection conn = DriverManager.getConnection(url, user, password);
        System.out.println("✅ Verbindung erfolgreich!");

        return conn;  // Falls erfolgreich, wird die Verbindung zurückgegeben.
    }


}
