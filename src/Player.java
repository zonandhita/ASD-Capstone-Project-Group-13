import java.awt.Color;

public class Player {
    private String name;
    private int position;
    private Color color;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.position = 1; // Start position
    }

    public String getName() { return name; }
    public int getPosition() { return position; }
    public Color getColor() { return color; }

    public void setPosition(int position) {
        this.position = position;
    }
}