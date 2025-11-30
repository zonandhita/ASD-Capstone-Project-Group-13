import java.awt.Color;

public class Player {
    private String name;
    private int position;
    private Color color;
    private int score; // ✅ Tambahan Variable Score

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.position = 1; // Start position default 1
        this.score = 0;    // Score awal 0
    }

    // ✅ Method Baru: Menambah Score
    public void addScore(int amount) {
        this.score += amount;
    }

    // ✅ Method Baru: Mendapatkan Score saat ini
    public int getScore() {
        return score;
    }

    // --- Getter & Setter Standard ---
    public String getName() { return name; }

    public int getPosition() { return position; }

    public Color getColor() { return color; }

    public void setPosition(int position) {
        this.position = position;
    }

    // Opsional: Reset score jika game diulang
    public void resetScore() {
        this.score = 0;
    }
}