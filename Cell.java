import java.awt.*;

public class Cell {
    private int number;
    private int x, y;
    private int size;
    private CellType type;
    private int targetCell; // untuk ular dan tangga
    private boolean isPath;
    private boolean isVisited;

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
        this.isPath = false;
        this.isVisited = false;
    }

    public int getNumber() { return number; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return size; }
    public CellType getType() { return type; }
    public int getTargetCell() { return targetCell; }
    public boolean isPath() { return isPath; }
    public boolean isVisited() { return isVisited; }

    public void setType(CellType type) { this.type = type; }
    public void setTargetCell(int target) { this.targetCell = target; }
    public void setPath(boolean path) { this.isPath = path; }
    public void setVisited(boolean visited) { this.isVisited = visited; }

    public Point getCenter() {
        return new Point(x + size / 2, y + size / 2);
    }

    public void draw(Graphics2D g2) {
        // Background color based on type
        Color bgColor = Color.WHITE;
        if (isPath) {
            bgColor = new Color(255, 255, 150); // Kuning untuk jalur
        } else if (isVisited) {
            bgColor = new Color(200, 230, 255); // Biru muda untuk visited
        }

        switch (type) {
            case START:
                bgColor = new Color(144, 238, 144); // Hijau muda
                break;
            case FINISH:
                bgColor = new Color(255, 215, 0); // Gold
                break;
            case SNAKE:
                if (!isPath) bgColor = new Color(255, 200, 200); // Merah muda
                break;
            case LADDER:
                if (!isPath) bgColor = new Color(200, 255, 200); // Hijau muda
                break;
        }

        // Draw cell background
        g2.setColor(bgColor);
        g2.fillRect(x, y, size, size);

        // Draw border
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, size, size);

        // Draw cell number
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        String numStr = String.valueOf(number);
        int numWidth = fm.stringWidth(numStr);
        int numHeight = fm.getHeight();
        g2.drawString(numStr, x + (size - numWidth) / 2, y + (size + numHeight / 2) / 2);

        // Draw type indicator
        if (type == CellType.SNAKE || type == CellType.LADDER) {
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            String arrow = type == CellType.SNAKE ? "↓" + targetCell : "↑" + targetCell;
            g2.setColor(type == CellType.SNAKE ? Color.RED : Color.GREEN);
            g2.drawString(arrow, x + 5, y + size - 5);
        }
    }
}