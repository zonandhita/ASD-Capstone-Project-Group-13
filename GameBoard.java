import java.awt.*;
import java.util.*;
import java.util.List;

public class GameBoard {
    private Cell[][] cells;
    private int boardSize;
    private int cellSize;
    private Map<Integer, Integer> snakesAndLadders;

    public GameBoard(int boardSize, int cellSize) {
        this.boardSize = boardSize;
        this.cellSize = cellSize;
        this.cells = new Cell[boardSize][boardSize];
        this.snakesAndLadders = new HashMap<>();
        initializeBoard();
        addSnakesAndLadders();
    }

    private void initializeBoard() {
        int cellNumber = boardSize * boardSize;

        // Snake pattern (zig-zag dari bawah ke atas)
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                int x = col * cellSize;
                int y = row * cellSize;

                // Hitung nomor cell (dari kiri ke kanan, bawah ke atas, zig-zag)
                int actualRow = boardSize - 1 - row;
                int number;

                if (actualRow % 2 == 0) {
                    // Baris genap: kiri ke kanan
                    number = actualRow * boardSize + col + 1;
                } else {
                    // Baris ganjil: kanan ke kiri
                    number = actualRow * boardSize + (boardSize - col);
                }

                cells[row][col] = new Cell(number, x, y, cellSize);
            }
        }

        // Set START dan FINISH
        Cell startCell = getCellByNumber(1);
        if (startCell != null) startCell.setType(Cell.CellType.START);

        Cell finishCell = getCellByNumber(boardSize * boardSize);
        if (finishCell != null) finishCell.setType(Cell.CellType.FINISH);
    }

    private void addSnakesAndLadders() {
        // Tangga (dari bawah ke atas)
        addLadder(3, 14);
        addLadder(7, 21);
        addLadder(11, 26);
        addLadder(18, 39);
        addLadder(28, 44);

        // Ular (dari atas ke bawah)
        addSnake(47, 19);
        addSnake(43, 24);
        addSnake(38, 15);
        addSnake(34, 6);
        addSnake(25, 9);
    }

    private void addSnake(int from, int to) {
        snakesAndLadders.put(from, to);
        Cell fromCell = getCellByNumber(from);
        if (fromCell != null) {
            fromCell.setType(Cell.CellType.SNAKE);
            fromCell.setTargetCell(to);
        }
    }

    private void addLadder(int from, int to) {
        snakesAndLadders.put(from, to);
        Cell fromCell = getCellByNumber(from);
        if (fromCell != null) {
            fromCell.setType(Cell.CellType.LADDER);
            fromCell.setTargetCell(to);
        }
    }

    public Cell getCellByNumber(int number) {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (cells[row][col].getNumber() == number) {
                    return cells[row][col];
                }
            }
        }
        return null;
    }

    public List<Integer> getNeighbors(int cellNumber) {
        List<Integer> neighbors = new ArrayList<>();

        // Dari cell saat ini, kita bisa melempar dadu 1-6
        for (int dice = 1; dice <= 6; dice++) {
            int nextCell = cellNumber + dice;

            if (nextCell <= boardSize * boardSize) {
                // Cek apakah ada ular atau tangga
                if (snakesAndLadders.containsKey(nextCell)) {
                    nextCell = snakesAndLadders.get(nextCell);
                }
                neighbors.add(nextCell);
            }
        }

        return neighbors;
    }

    public void resetVisualization() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                cells[row][col].setPath(false);
                cells[row][col].setVisited(false);
            }
        }
    }

    public void draw(Graphics2D g2) {
        // Draw all cells
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                cells[row][col].draw(g2);
            }
        }

        // Draw snakes and ladders connections
        g2.setStroke(new BasicStroke(3));
        for (Map.Entry<Integer, Integer> entry : snakesAndLadders.entrySet()) {
            Cell fromCell = getCellByNumber(entry.getKey());
            Cell toCell = getCellByNumber(entry.getValue());

            if (fromCell != null && toCell != null) {
                Point from = fromCell.getCenter();
                Point to = toCell.getCenter();

                if (fromCell.getType() == Cell.CellType.SNAKE) {
                    g2.setColor(new Color(220, 20, 60, 150)); // Merah transparan
                } else {
                    g2.setColor(new Color(34, 139, 34, 150)); // Hijau transparan
                }

                // Draw curved line
                int ctrlX = (from.x + to.x) / 2 + (from.x > to.x ? -30 : 30);
                int ctrlY = (from.y + to.y) / 2;

                Graphics2D g2d = (Graphics2D) g2.create();
                g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Draw bezier curve
                for (double t = 0; t <= 1; t += 0.01) {
                    double x = Math.pow(1-t, 2) * from.x + 2*(1-t)*t*ctrlX + Math.pow(t, 2)*to.x;
                    double y = Math.pow(1-t, 2) * from.y + 2*(1-t)*t*ctrlY + Math.pow(t, 2)*to.y;
                    g2d.fillOval((int)x-2, (int)y-2, 4, 4);
                }

                g2d.dispose();
            }
        }
    }

    public int getBoardSize() { return boardSize; }
    public int getCellSize() { return cellSize; }
}