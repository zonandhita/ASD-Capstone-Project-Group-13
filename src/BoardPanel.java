import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {
    private GameBoard gameBoard;

    public BoardPanel(GameBoard gameBoard) {
        this.gameBoard = gameBoard;

        // Kalkulasi dimensi panel berdasarkan jumlah kotak dan ukuran tiap cell
        int totalSize = gameBoard.getBoardSize() * gameBoard.getCellSize();
        setPreferredSize(new Dimension(totalSize + 20, totalSize + 20));

        // Background abu-abu muda untuk kontras area permainan
        setBackground(new Color(240, 240, 240));
    }

    /**
     * Menangani proses rendering elemen visual papan permainan.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Optimasi rendering agar teks dan bentuk objek terlihat lebih halus
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Memberikan margin/padding sebesar 10px dari tepi panel
        g2.translate(10, 10);

        if (gameBoard != null) {
            gameBoard.draw(g2);
        }
    }

    /**
     * Memicu pembaruan tampilan grafis secara manual.
     */
    public void updateBoard() {
        repaint();
    }
}