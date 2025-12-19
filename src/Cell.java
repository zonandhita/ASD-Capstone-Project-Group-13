import java.awt.*;

public class Cell {
    private int number;
    private int x, y;
    private int size;
    private CellType type;
    private int targetCell;

    // Status visual untuk elemen khusus di map
    private boolean isPrime;
    private boolean isStar;
    private boolean isTrap;

    // Nilai ekonomi dari paket yang bisa diambil kurir
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

    /**
     * Mengatur render visual setiap kotak pada grid board.
     * Mengubah elemen Snake/Ladder menjadi konsep navigasi kota (Detour/Highway).
     */
    public void draw(Graphics2D g2) {
        // Setup warna latar berdasarkan status kotak (Jalanan, Gudang, atau Zona Bahaya)
        Color bgColor = new Color(240, 240, 240);

        if (isTrap) {
            bgColor = new Color(255, 100, 100); // Merah untuk area razia polisi
        } else if (type == CellType.START) {
            bgColor = new Color(144, 238, 144); // Hijau untuk titik awal (Depot)
        } else if (type == CellType.FINISH) {
            bgColor = new Color(135, 206, 235); // Biru untuk titik tujuan (Drop-off)
        }

        g2.setColor(bgColor);
        g2.fillRect(x, y, size, size);

        // Highlight khusus untuk kotak prima (Checkpoint bernilai emas)
        g2.setStroke(new BasicStroke(1));
        if (isPrime) {
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(x+2, y+2, size-4, size-4);
        }

        // Garis tepi kotak agar terlihat seperti blok kota
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(x, y, size, size);

        // Penomoran cell sebagai alamat lokasi
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String numStr = String.valueOf(number);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(numStr, x + size - fm.stringWidth(numStr) - 5, y + size - 5);

        // Render ikon tambahan: Bintang (Bonus) dan Text Peringatan
        if (isStar) {
            drawStarShape(g2, x + 15, y + 15, 12);
        }

        if (isTrap) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("RAID", x + 5, y + 28);
        }

        // Gambar paket barang jika kotak memiliki nilai score
        if (scoreValue > 0) {
            drawPackage(g2, x + size - 30, y + 10, scoreValue);
        }

        // Indikator rute otomatis (Detour = Ular, HWY = Tangga)
        if (type == CellType.SNAKE || type == CellType.LADDER) {
            g2.setColor(Color.BLACK);
            String arrow = (type == CellType.SNAKE ? "DETOUR " : "HWY ") + targetCell;
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString(arrow, x + 5, y + size/2 + 5);
        }
    }

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

    /**
     * Gambar visual paket kardus untuk menggantikan konsep koin tradisional.
     */
    private void drawPackage(Graphics2D g, int x, int y, int value) {
        // Visual kardus utama
        g.setColor(new Color(205, 133, 63));
        g.fillRect(x, y, 24, 20);

        // Aksen tali pengikat paket
        g.setColor(new Color(139, 69, 19));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 12, y, x + 12, y + 20);
        g.drawLine(x, y + 10, x + 24, y + 10);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawRect(x, y, 24, 20);

        // Label harga atau nilai paket
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 9));
        String valStr = "$" + value;
        g.drawString(valStr, x - 2, y - 2);
    }
}