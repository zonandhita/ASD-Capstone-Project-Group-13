import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GraphPanel extends JPanel {
    private Graph graph;
    private Node draggedNode;
    private int offsetX, offsetY;

    public GraphPanel(Graph graph) {
        this.graph = graph;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        // Add mouse listeners for drag and drop
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (graph == null) return;

                // Find if a node was clicked
                for (Node node : graph.getNodes()) {
                    double dx = e.getX() - node.getX();
                    double dy = e.getY() - node.getY();
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance <= node.getRadius()) {
                        draggedNode = node;
                        offsetX = (int)(e.getX() - node.getX());
                        offsetY = (int)(e.getY() - node.getY());
                        break;
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNode != null) {
                    draggedNode.setPosition(e.getX() - offsetX, e.getY() - offsetY);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNode = null;
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (graph != null) {
            graph.draw(g2);
        }
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        draggedNode = null;
        repaint();
    }
}