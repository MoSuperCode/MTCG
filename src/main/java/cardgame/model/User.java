package cardgame.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class User {
    @JsonAlias({"username"})
    private String username;

    @JsonAlias({"password"})
    private String password;

    private String token;  // Hier habe ich ein Feld für das Token hinzugefügt

    // Konstruktor für das Erstellen eines neuen Benutzers
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter für den Benutzernamen
    public String getUsername() {
        return username;
    }

    // Hier habe ich einen Setter für den Benutzernamen hinzugefügt (falls du den Benutzernamen ändern möchtest)
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter für das Passwort
    public String getPassword() {
        return password;
    }

    // Hier habe ich einen Setter für das Passwort hinzugefügt (z.B. falls du das Passwort ändern möchtest)
    public void setPassword(String password) {
        this.password = password;
    }

    // Getter für das Token
    public String getToken() {
        return token;
    }

    // Hier habe ich einen Setter für das Token hinzugefügt
    public void setToken(String token) {
        this.token = token;
    }

    // Optionale Methode, um den Benutzer als String darzustellen (für Debugging oder Logging)
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
