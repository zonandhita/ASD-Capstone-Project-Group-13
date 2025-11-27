import java.awt.*;
import java.awt.geom.*;
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
        // Snake pattern (zig-zag dari bawah ke atas)
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                int x = col * cellSize;
                int y = row * cellSize;

                // Hitung nomor cell (dari kiri ke kanan, bawah ke atas, zig-zag)
                int actualRow = boardSize - 1 - row;
                int number;

                if (actualRow % 2 == 0) {
                    number = actualRow * boardSize + col + 1;
                } else {
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

        for (int dice = 1; dice <= 6; dice++) {
            int nextCell = cellNumber + dice;

            if (nextCell <= boardSize * boardSize) {
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
        // Draw background with wood texture effect
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(222, 184, 135),
                boardSize * cellSize, boardSize * cellSize, new Color(205, 133, 63)
        );
        g2.setPaint(bgGradient);
        g2.fillRect(0, 0, boardSize * cellSize, boardSize * cellSize);

        // Draw all cells
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                cells[row][col].draw(g2);
            }
        }

        // Draw snakes and ladders
        for (Map.Entry<Integer, Integer> entry : snakesAndLadders.entrySet()) {
            Cell fromCell = getCellByNumber(entry.getKey());
            Cell toCell = getCellByNumber(entry.getValue());

            if (fromCell != null && toCell != null) {
                if (fromCell.getType() == Cell.CellType.SNAKE) {
                    drawSnake(g2, fromCell, toCell);
                } else {
                    drawLadder(g2, fromCell, toCell);
                }
            }
        }
    }

    private void drawSnake(Graphics2D g2, Cell from, Cell to) {
        Point fromPoint = from.getCenter();
        Point toPoint = to.getCenter();

        // Draw snake body with gradient
        GradientPaint snakeGradient = new GradientPaint(
                fromPoint.x, fromPoint.y, new Color(220, 20, 60),
                toPoint.x, toPoint.y, new Color(139, 0, 0)
        );

        g2.setPaint(snakeGradient);
        g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw curved snake path
        Path2D snakePath = new Path2D.Double();
        snakePath.moveTo(fromPoint.x, fromPoint.y);

        int ctrlX1 = fromPoint.x + (toPoint.x - fromPoint.x) / 3;
        int ctrlY1 = fromPoint.y - 40;
        int ctrlX2 = fromPoint.x + 2 * (toPoint.x - fromPoint.x) / 3;
        int ctrlY2 = toPoint.y + 40;

        snakePath.curveTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, toPoint.x, toPoint.y);
        g2.draw(snakePath);

        // Draw snake head
        g2.setColor(new Color(139, 0, 0));
        g2.fillOval(toPoint.x - 10, toPoint.y - 10, 20, 20);

        // Draw snake eyes
        g2.setColor(Color.YELLOW);
        g2.fillOval(toPoint.x - 6, toPoint.y - 4, 5, 5);
        g2.fillOval(toPoint.x + 1, toPoint.y - 4, 5, 5);

        g2.setColor(Color.BLACK);
        g2.fillOval(toPoint.x - 4, toPoint.y - 2, 2, 2);
        g2.fillOval(toPoint.x + 3, toPoint.y - 2, 2, 2);

        // Draw decorative scales
        g2.setStroke(new BasicStroke(2));
        for (double t = 0.2; t <= 0.8; t += 0.15) {
            double x = Math.pow(1-t, 3) * fromPoint.x +
                    3 * Math.pow(1-t, 2) * t * ctrlX1 +
                    3 * (1-t) * Math.pow(t, 2) * ctrlX2 +
                    Math.pow(t, 3) * toPoint.x;
            double y = Math.pow(1-t, 3) * fromPoint.y +
                    3 * Math.pow(1-t, 2) * t * ctrlY1 +
                    3 * (1-t) * Math.pow(t, 2) * ctrlY2 +
                    Math.pow(t, 3) * toPoint.y;

            g2.setColor(new Color(255, 255, 0, 150));
            g2.drawOval((int)x - 4, (int)y - 4, 8, 8);
        }
    }

    private void drawLadder(Graphics2D g2, Cell from, Cell to) {
        Point fromPoint = from.getCenter();
        Point toPoint = to.getCenter();

        // Calculate ladder angle
        double angle = Math.atan2(toPoint.y - fromPoint.y, toPoint.x - fromPoint.x);

        int ladderWidth = 15;
        int dx = (int)(ladderWidth * Math.sin(angle));
        int dy = (int)(-ladderWidth * Math.cos(angle));

        // Draw ladder rails
        g2.setColor(new Color(139, 69, 19)); // Brown
        g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Left rail
        g2.drawLine(fromPoint.x - dx, fromPoint.y - dy, toPoint.x - dx, toPoint.y - dy);
        // Right rail
        g2.drawLine(fromPoint.x + dx, fromPoint.y + dy, toPoint.x + dx, toPoint.y + dy);

        // Draw ladder rungs
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(160, 82, 45));

        int numRungs = 5;
        for (int i = 1; i < numRungs; i++) {
            double t = (double) i / numRungs;
            int rungX = (int)(fromPoint.x + t * (toPoint.x - fromPoint.x));
            int rungY = (int)(fromPoint.y + t * (toPoint.y - fromPoint.y));

            g2.drawLine(rungX - dx, rungY - dy, rungX + dx, rungY + dy);
        }

        // Draw decorative rope at top
        g2.setColor(new Color(205, 133, 63));
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(toPoint.x - 12, toPoint.y - 12, 24, 24);
    }

    public int getBoardSize() { return boardSize; }
    public int getCellSize() { return cellSize; }
}