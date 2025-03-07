package model;

import cardgame.model.Card;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testCardCreationWithAutoUUID() {
        Card card = new Card("Fireball", 50.0, Card.ElementType.FIRE, true);

        assertNotNull(card.getId());  // UUID sollte automatisch gesetzt sein
        assertEquals("Fireball", card.getName());
        assertEquals(50.0, card.getDamage());
        assertEquals(Card.ElementType.FIRE, card.getElementType());
        assertTrue(card.isSpell());
    }

    @Test
    void testCardCreationWithExistingUUID() {
        UUID uuid = UUID.randomUUID();
        Card card = new Card(uuid, "WaterGoblin", 30.0, Card.ElementType.WATER, false);

        assertEquals(uuid, card.getId());
        assertEquals("WaterGoblin", card.getName());
        assertEquals(30.0, card.getDamage());
        assertEquals(Card.ElementType.WATER, card.getElementType());
        assertFalse(card.isSpell());
    }

    @Test
    void testSetId() {
        Card card = new Card("Ork", 40.0, Card.ElementType.NORMAL, false);
        UUID newId = UUID.randomUUID();
        card.setId(newId);

        assertEquals(newId, card.getId());
    }

    @Test
    void testSetName() {
        Card card = new Card("OldName", 25.0, Card.ElementType.FIRE, true);
        card.setName("NewName");

        assertEquals("NewName", card.getName());
    }

    @Test
    void testSetDamage() {
        Card card = new Card("Goblin", 15.0, Card.ElementType.NORMAL, false);
        card.setDamage(20.0);

        assertEquals(20.0, card.getDamage());
    }

    @Test
    void testSetElementType() {
        Card card = new Card("Mage", 35.0, Card.ElementType.FIRE, true);
        card.setElementType(Card.ElementType.WATER);

        assertEquals(Card.ElementType.WATER, card.getElementType());
    }

    @Test
    void testSetIsSpell() {
        Card card = new Card("Dragon", 100.0, Card.ElementType.FIRE, false);
        card.setSpell(true);

        assertTrue(card.isSpell());
    }

    @Test
    void testToString() {
        UUID uuid = UUID.randomUUID();
        Card card = new Card(uuid, "Knight", 60.0, Card.ElementType.NORMAL, false);

        String expectedString = "Card{id=" + uuid + ", name='Knight', damage=60.0, elementType=NORMAL, isSpell=false}";
        assertEquals(expectedString, card.toString());
    }
}
