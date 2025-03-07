package model;

import cardgame.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        User user = new User("Alice", "password123");

        assertEquals("Alice", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertNull(user.getToken());  // Token sollte anfangs null sein
    }

    @Test
    void testSetUsername() {
        User user = new User("Alice", "password123");
        user.setUsername("Bob");

        assertEquals("Bob", user.getUsername());
    }

    @Test
    void testSetPassword() {
        User user = new User("Alice", "password123");
        user.setPassword("newPass");

        assertEquals("newPass", user.getPassword());
    }

    @Test
    void testSetToken() {
        User user = new User("Alice", "password123");
        user.setToken("token123");

        assertEquals("token123", user.getToken());
    }

    @Test
    void testToString() {
        User user = new User("Alice", "password123");
        user.setToken("token123");

        String expectedString = "User{username='Alice', password='password123', token='token123'}";
        assertEquals(expectedString, user.toString());
    }
}
