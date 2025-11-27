import java.awt.*;
import java.awt.geom.*;

public class Cell {
    private int number;
    private int x, y;
    private int size;
    private CellType type;
    private int targetCell;
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
        // Determine colors based on state and type
        Color bgColor = new Color(245, 245, 220); // Beige default
        Color borderColor = new Color(139, 69, 19); // Brown

        if (isPath) {
            bgColor = new Color(255, 223, 0); // Gold untuk path
        } else if (isVisited) {
            bgColor = new Color(173, 216, 230); // Light blue
        }

        // Special colors for special cells
        switch (type) {
            case START:
                bgColor = new Color(152, 251, 152); // Pale green
                break;
            case FINISH:
                bgColor = new Color(255, 215, 0); // Gold
                break;
            case SNAKE:
                if (!isPath) bgColor = new Color(255, 182, 193); // Light pink
                break;
            case LADDER:
                if (!isPath) bgColor = new Color(144, 238, 144); // Light green
                break;
        }

        // Draw cell with rounded corners
        g2.setColor(bgColor);
        RoundRectangle2D roundRect = new RoundRectangle2D.Double(x + 2, y + 2, size - 4, size - 4, 15, 15);
        g2.fill(roundRect);

        // Draw border with shadow effect
        g2.setColor(new Color(0, 0, 0, 50));
        g2.setStroke(new BasicStroke(1));
        g2.draw(new RoundRectangle2D.Double(x + 3, y + 3, size - 4, size - 4, 15, 15));

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2));
        g2.draw(roundRect);

        // Draw decorative pattern for normal cells
        if (type == CellType.NORMAL && !isPath && !isVisited) {
            g2.setColor(new Color(210, 180, 140, 50));
            for (int i = 0; i < 3; i++) {
                g2.drawLine(x + 10 + i * 10, y + 5, x + 5, y + 10 + i * 10);
            }
        }

        // Draw cell number with shadow
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        String numStr = String.valueOf(number);
        int numWidth = fm.stringWidth(numStr);
        int numHeight = fm.getHeight();
        int numX = x + (size - numWidth) / 2;
        int numY = y + (size + numHeight / 2) / 2;

        // Number shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString(numStr, numX + 1, numY + 1);

        // Number
        g2.setColor(new Color(50, 50, 50));
        g2.drawString(numStr, numX, numY);

        // Draw type indicator with icon
        if (type == CellType.START) {
            g2.setColor(new Color(0, 128, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("START", x + 5, y + size - 8);
        } else if (type == CellType.FINISH) {
            g2.setColor(new Color(218, 165, 32));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("FINISH", x + 5, y + size - 8);
        } else if (type == CellType.SNAKE || type == CellType.LADDER) {
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            String arrow = type == CellType.SNAKE ? "→" + targetCell : "→" + targetCell;
            g2.setColor(type == CellType.SNAKE ? new Color(220, 20, 60) : new Color(34, 139, 34));
            g2.drawString(arrow, x + 5, y + size - 8);
        }

        // Draw path indicator
        if (isPath) {
            g2.setColor(new Color(255, 140, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(x + size/2 - 8, y + size/2 - 8, 16, 16);
        }
    }
}