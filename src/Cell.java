import java.awt.*;

public class Cell {
    private int number;
    private int x, y;
    private int size;
    private CellType type;
    private int targetCell;

    // Properti Visual
    private boolean isPrime;
    private boolean isStar;
    private boolean isTrap; // Sekarang visualnya jadi "RAID"

    // SCORE (Sekarang visualnya jadi PAKET)
    private int scoreValue = 0;

    public enum CellType {
        NORMAL, SNAKE, LADDER, START, FINISH
    }

    public Cell(int number, int x, int y, int size) {
        this.number = number;
        this.x = x;
        this.y = y;
        this.size = size;
        this.type = CellType.NORMAL;
        this.targetCell = -1;
    }

    // --- Setters & Getters ---
    public void setScore(int score) { this.scoreValue = score; }
    public int getScore() { return scoreValue; }
    public boolean hasScore() { return scoreValue > 0; }

    public void setPrime(boolean prime) { isPrime = prime; }
    public boolean isPrime() { return isPrime; }
    public void setStar(boolean star) { isStar = star; }
    public boolean isStar() { return isStar; }
    public void setTrap(boolean trap) { isTrap = trap; }
    public boolean isTrap() { return isTrap; }

    public int getNumber() { return number; }
    public int getX() { return x; }
    public int getY() { return y; }
    public CellType getType() { return type; }
    public int getTargetCell() { return targetCell; }
    public void setType(CellType type) { this.type = type; }
    public void setTargetCell(int target) { this.targetCell = target; }
    public void setPath(boolean path) { }
    public void setVisited(boolean visited) { }

    public Point getCenter() {
        return new Point(x + size / 2, y + size / 2);
    }

    // --- VISUALISASI UTAMA ---
    public void draw(Graphics2D g2) {
        // 1. Warna Dasar (Jalanan Kota)
        Color bgColor = new Color(240, 240, 240); // Abu-abu sangat muda (Trotoar/Jalan)

        if (isTrap) bgColor = new Color(255, 100, 100); // MERAH (Zona Razia)
        else if (type == CellType.START) bgColor = new Color(144, 238, 144); // Gudang (Start)
        else if (type == CellType.FINISH) bgColor = new Color(135, 206, 235); // Customer (Finish - SkyBlue)

        g2.setColor(bgColor);
        g2.fillRect(x, y, size, size);

        // 2. Border (Emas untuk Prime/Checkpoint)
        g2.setStroke(new BasicStroke(1));
        if (isPrime) {
            g2.setColor(new Color(255, 215, 0)); // Emas
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(x+2, y+2, size-4, size-4);
        }
        g2.setColor(Color.GRAY); // Border kotak jadi abu-abu (seperti blok kota)
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(x, y, size, size);

        // 3. Nomor (Alamat)
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String numStr = String.valueOf(number);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(numStr, x + size - fm.stringWidth(numStr) - 5, y + size - 5);

        // 4. Ikon Star, Raid, dan PACKAGE
        if (isStar) {
            drawStarShape(g2, x + 15, y + 15, 12);
        }

        if (isTrap) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("RAID", x + 5, y + 28); // Text diubah jadi RAID
        }

        // ✅ GAMBAR PAKET (Dulu Koin)
        if (scoreValue > 0) {
            drawPackage(g2, x + size - 30, y + 10, scoreValue);
        }

        // 5. Info Navigasi (Pengganti Ular/Tangga text)
        if (type == CellType.SNAKE || type == CellType.LADDER) {
            g2.setColor(Color.BLACK);
            // Visual panah navigasi
            String arrow = (type == CellType.SNAKE ? "DETOUR " : "HWY ") + targetCell;
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString(arrow, x + 5, y + size/2 + 5);
        }
    }

    // Helper: Menggambar Bintang (Bonus Point)
    private void drawStarShape(Graphics2D g, int x, int y, int radius) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        double centerAng = Math.PI / 2;

        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? radius : radius / 2.5;
            xPoints[i] = x + (int) (r * Math.cos(centerAng));
            yPoints[i] = y - (int) (r * Math.sin(centerAng));
            centerAng += Math.PI / 5;
        }
        g.setColor(new Color(255, 140, 0));
        g.fillPolygon(xPoints, yPoints, 10);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawPolygon(xPoints, yPoints, 10);
    }

    // ✅ Helper Baru: Menggambar PAKET KARDUS (Pengganti Koin)
    private void drawPackage(Graphics2D g, int x, int y, int value) {
        // Kotak Kardus Coklat
        g.setColor(new Color(205, 133, 63)); // Brown
        g.fillRect(x, y, 24, 20);

        // Garis Selotip/Tali
        g.setColor(new Color(139, 69, 19)); // Dark Brown
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 12, y, x + 12, y + 20); // Vertikal
        g.drawLine(x, y + 10, x + 24, y + 10); // Horizontal

        // Border Luar
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawRect(x, y, 24, 20);

        // Nilai Paket (Duit)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 9));
        String valStr = "$" + value;
        g.drawString(valStr, x - 2, y - 2); // Di atas paket
    }
}