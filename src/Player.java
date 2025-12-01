import java.awt.Color;

public class Player {
    private String name;
    private int position;
    private Color color;

    // ✅ FITUR SCORE
    private int score;

    // ✅ FITUR STATISTIK (BARU): Menghitung jumlah langkah dadu
    private int totalSteps;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.position = 1; // Start position default 1
        this.score = 0;    // Score awal 0
        this.totalSteps = 0; // Langkah awal 0
    }

    // --- FITUR SCORE ---
    public void addScore(int amount) {
        this.score += amount;
    }

    public int getScore() {
        return score;
    }

    // --- FITUR STATISTIK (BARU) ---
    public void incrementSteps() {
        this.totalSteps++;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    // --- Getter & Setter Standard ---
    public String getName() { return name; }

    public int getPosition() { return position; }

    public Color getColor() { return color; }

    public void setPosition(int position) {
        this.position = position;
    }

    // Reset Score Saja (Kode Lama)
    public void resetScore() {
        this.score = 0;
    }

    // ✅ Reset Lengkap (Untuk Game Ulang)
    public void reset() {
        this.position = 1;
        this.score = 0;
        this.totalSteps = 0;
    }
}