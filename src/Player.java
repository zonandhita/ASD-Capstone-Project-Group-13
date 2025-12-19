import java.awt.Color;

public class Player {
    private String name;
    private int position;
    private Color color;

    // Statistik performa kurir selama pengiriman
    private int score;      // Total pendapatan dari paket yang diantar
    private int totalSteps; // Akumulasi penggunaan bahan bakar (langkah)

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.position = 1;
        this.score = 0;
        this.totalSteps = 0;
    }

    /**
     * Menambahkan nilai pendapatan setiap kali berhasil mengambil paket.
     */
    public void addScore(int amount) {
        this.score += amount;
    }

    /**
     * Mencatat setiap pergerakan kendaraan sebagai penggunaan bensin.
     */
    public void incrementSteps() {
        this.totalSteps++;
    }

    /**
     * Mengembalikan status kurir ke kondisi awal (Depot) saat pergantian shift.
     */
    public void reset() {
        this.position = 1;
        this.score = 0;
        this.totalSteps = 0;
    }

    public int getScore() { return score; }
    public int getTotalSteps() { return totalSteps; }
    public String getName() { return name; }
    public int getPosition() { return position; }
    public Color getColor() { return color; }
    public void setPosition(int position) { this.position = position; }
}