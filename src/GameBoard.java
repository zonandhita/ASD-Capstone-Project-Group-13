import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class GameBoard {
    private Cell[][] cells;
    private int boardSize;
    private int cellSize;
    private Map<Integer, Integer> snakesAndLadders;
    private Set<Integer> redTiles;

    public GameBoard(int boardSize, int cellSize) {
        this.boardSize = boardSize;
        this.cellSize = cellSize;
        this.cells = new Cell[boardSize][boardSize];
        this.snakesAndLadders = new HashMap<>();
        this.redTiles = new HashSet<>();

        initializeBoard();
    }

    /**
     * Membangun grid kota dengan pola zigzag (Boustrophedon).
     * Setiap petak dikalkulasi posisinya agar urutan nomor urut dari bawah ke atas.
     */
    private void initializeBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                int x = col * cellSize;
                int y = row * cellSize;
                int actualRow = boardSize - 1 - row;
                int number;

                // Logika alur angka zigzag agar nomor berlanjut di setiap baris
                if (actualRow % 2 == 0) number = actualRow * boardSize + col + 1;
                else number = actualRow * boardSize + (boardSize - col);

                Cell c = new Cell(number, x, y, cellSize);

                // Menentukan fitur otomatis berdasarkan nilai angka (Prima/Kelipatan)
                if (isPrime(number)) c.setPrime(true);
                if (number % 5 == 0) c.setStar(true);

                cells[row][col] = c;
            }
        }

        // Penetapan titik awal (Depot) dan titik akhir (Customer)
        Cell start = getCellByNumber(1);
        if(start!=null) start.setType(Cell.CellType.START);
        Cell finish = getCellByNumber(boardSize*boardSize);
        if(finish!=null) finish.setType(Cell.CellType.FINISH);
    }

    /**
     * Menyebarkan paket belanjaan secara acak di seluruh peta kota.
     */
    public void generateScores(int count) {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                cells[row][col].setScore(0);
            }
        }
        Random rand = new Random();
        int generated = 0;
        int max = boardSize * boardSize;
        while (generated < count) {
            int pos = rand.nextInt(max - 2) + 2;
            // Memastikan paket tidak ditaruh di jalur tol atau area razia
            if (!snakesAndLadders.containsKey(pos) && !redTiles.contains(pos)) {
                Cell c = getCellByNumber(pos);
                if (c != null && c.getScore() == 0) {
                    int randomScore = (rand.nextInt(10) + 1) * 10;
                    c.setScore(randomScore);
                    generated++;
                }
            }
        }
    }

    /**
     * Membuat rute khusus seperti jalan tol, area macet, dan titik razia polisi.
     */
    public void generateFeatures(int linkCount, int trapCount) {
        snakesAndLadders.clear();
        redTiles.clear();
        Random rand = new Random();
        int max = boardSize * boardSize;

        // Distribusi acak rute Highway (naik) dan Detour (turun)
        int links = 0;
        while (links < linkCount) {
            int start = rand.nextInt(max - 2) + 2;
            int end = rand.nextInt(max - 2) + 2;
            if (start != end && !snakesAndLadders.containsKey(start) && !snakesAndLadders.containsKey(end)) {
                if (start < end) addLadder(start, end);
                else addSnake(start, end);
                links++;
            }
        }

        // Penempatan zona razia polisi (Trap)
        int traps = 0;
        while (traps < trapCount) {
            int pos = rand.nextInt(max - 5) + 3;
            if (!snakesAndLadders.containsKey(pos) && !redTiles.contains(pos) && pos != 1 && pos != max) {
                redTiles.add(pos);
                Cell c = getCellByNumber(pos);
                if (c != null) c.setTrap(true);
                traps++;
            }
        }
    }

    private void addSnake(int from, int to) {
        snakesAndLadders.put(from, to);
        Cell c = getCellByNumber(from);
        if(c!=null) { c.setType(Cell.CellType.SNAKE); c.setTargetCell(to); }
    }

    private void addLadder(int from, int to) {
        snakesAndLadders.put(from, to);
        Cell c = getCellByNumber(from);
        if(c!=null) { c.setType(Cell.CellType.LADDER); c.setTargetCell(to); }
    }

    public Cell getCellByNumber(int number) {
        for (int r=0; r<boardSize; r++) {
            for (int c=0; c<boardSize; c++) {
                if (cells[r][c].getNumber() == number) return cells[r][c];
            }
        }
        return null;
    }

    public Cell getCellAtPixel(int x, int y) {
        int col = x / cellSize;
        int row = y / cellSize;
        if (row >= 0 && row < boardSize && col >= 0 && col < boardSize) {
            return cells[row][col];
        }
        return null;
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i=2; i<=Math.sqrt(n); i++) if (n%i == 0) return false;
        return true;
    }

    /**
     * Render grafis untuk seluruh elemen papan, termasuk visualisasi rute jalan raya.
     */
    public void draw(Graphics2D g2) {
        for (int r=0; r<boardSize; r++) {
            for (int c=0; c<boardSize; c++) cells[r][c].draw(g2);
        }

        // Menggambar garis rute GPS (Highway/Macet) antar petak
        for (Map.Entry<Integer, Integer> entry : snakesAndLadders.entrySet()) {
            Cell from = getCellByNumber(entry.getKey());
            Cell to = getCellByNumber(entry.getValue());
            if (from != null && to != null) {
                Point p1 = from.getCenter();
                Point p2 = to.getCenter();
                if (from.getType() == Cell.CellType.SNAKE) {
                    drawRealisticSnake(g2, p1, p2); // Jalur macet (Detour)
                } else {
                    drawRealisticLadder(g2, p1, p2); // Jalur tol (Highway)
                }
            }
        }
    }

    /**
     * Visualisasi rute Highway dengan marka jalan putih putus-putus.
     */
    private void drawRealisticLadder(Graphics2D g2, Point p1, Point p2) {
        g2.setColor(new Color(80, 80, 80));
        g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);

        g2.setColor(Color.WHITE);
        float[] dashPattern = {10f, 10f};
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * Visualisasi area kemacetan menggunakan indikator merah melengkung pada sistem navigasi.
     */
    private void drawRealisticSnake(Graphics2D g2, Point head, Point tail) {
        g2.setColor(new Color(220, 20, 60, 200));
        g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D.Double roadShape = new Path2D.Double();
        roadShape.moveTo(head.x, head.y);
        int ctrlX = (head.x + tail.x) / 2 + 40;
        int ctrlY = (head.y + tail.y) / 2;
        roadShape.quadTo(ctrlX, ctrlY, tail.x, tail.y);
        g2.draw(roadShape);

        // Marker dilarang masuk untuk memperjelas rute yang dihindari (Macet)
        g2.setColor(Color.RED);
        g2.fillOval(head.x - 8, head.y - 8, 16, 16);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(head.x - 4, head.y, head.x + 4, head.y);
    }

    public int getBoardSize() { return boardSize; }
    public int getCellSize() { return cellSize; }
}