package cardgame.model;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {
    @JsonProperty("Id")
    private UUID id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Damage")
    private double damage;

    @JsonProperty("ElementType")
    private ElementType elementType;

    @JsonProperty("IsSpell")
    private boolean isSpell;

    public enum ElementType {
        FIRE, WATER, NORMAL
    }



    // Konstruktor mit automatischer UUID
    public Card(String name, double damage, ElementType elementType, boolean isSpell) {
        this.id = UUID.randomUUID(); // Automatisch generierte UUID
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.isSpell = isSpell;
    }

    // Konstruktor mit existierender UUID (falls Karten aus DB geladen werden)
    public Card(UUID id, String name, double damage, ElementType elementType, boolean isSpell) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.isSpell = isSpell;
    }

    // Getter & Setter
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public boolean isSpell() {
        return isSpell;
    }

    public void setSpell(boolean spell) {
        isSpell = spell;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                ", elementType=" + elementType +
                ", isSpell=" + isSpell +
                '}';
    }
}
