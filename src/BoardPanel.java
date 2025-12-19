import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {
    private GameBoard gameBoard;

    public BoardPanel(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        int totalSize = gameBoard.getBoardSize() * gameBoard.getCellSize();
        setPreferredSize(new Dimension(totalSize + 20, totalSize + 20));
        setBackground(new Color(240, 240, 240));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.translate(10, 10);

        if (gameBoard != null) {
            gameBoard.draw(g2);
        }
    }

    public void updateBoard() {
        repaint();
    }
}