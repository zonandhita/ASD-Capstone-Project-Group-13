import java.awt.*;
import java.awt.geom.Line2D;

public class Edge {
    private Node source;
    private Node target;
    private int weight;

    public Edge(Node source, Node target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public int getWeight() {
        return weight;
    }

    public void draw(Graphics2D g2) {
        // Calculate arrow points
        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        double angle = Math.atan2(dy, dx);

        // Shorten line to stop at node boundary
        double length = Math.sqrt(dx * dx + dy * dy);
        double startX = source.getX() + (source.getRadius() * dx / length);
        double startY = source.getY() + (source.getRadius() * dy / length);
        double endX = target.getX() - (target.getRadius() * dx / length);
        double endY = target.getY() - (target.getRadius() * dy / length);

        // Draw line
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.draw(new Line2D.Double(startX, startY, endX, endY));

        // Draw arrowhead
        double arrowSize = 10;
        double angle1 = angle + Math.PI - Math.PI / 6;
        double angle2 = angle + Math.PI + Math.PI / 6;

        int[] xPoints = {
                (int)endX,
                (int)(endX + arrowSize * Math.cos(angle1)),
                (int)(endX + arrowSize * Math.cos(angle2))
        };
        int[] yPoints = {
                (int)endY,
                (int)(endY + arrowSize * Math.sin(angle1)),
                (int)(endY + arrowSize * Math.sin(angle2))
        };

        g2.fillPolygon(xPoints, yPoints, 3);

        // Draw weight if not 1
        if (weight != 1) {
            double midX = (startX + endX) / 2;
            double midY = (startY + endY) / 2;
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString(String.valueOf(weight), (int)midX + 5, (int)midY - 5);
        }
    }
}