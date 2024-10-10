package cardgame.model;

public class Card {
    private String name;
    private int damage;
    private String elementType; // fire, water, normal
    private boolean isSpell;    // true if spell-card, false if monster-card

    public Card(String name, int damage, String elementType, boolean isSpell) {
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.isSpell = isSpell;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
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
                "name='" + name + '\'' +
                ", damage=" + damage +
                ", elementType='" + elementType + '\'' +
                ", isSpell=" + isSpell +
                '}';
    }
}
