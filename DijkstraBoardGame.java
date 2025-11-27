import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DijkstraBoardGame extends JFrame {
    private GameBoard gameBoard;
    private BoardPanel boardPanel;
    private DijkstraAlgorithm dijkstra;
    private JTextArea resultArea;
    private JLabel statusLabel;

    private static final int BOARD_SIZE = 7;
    private static final int CELL_SIZE = 80;

    public DijkstraBoardGame() {
        setTitle("Snake and Ladder - Dijkstra Shortest Path");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create game board
        gameBoard = new GameBoard(BOARD_SIZE, CELL_SIZE);
        boardPanel = new BoardPanel(gameBoard);
        dijkstra = new DijkstraAlgorithm(gameBoard);

        // Create control panel
        JPanel controlPanel = createControlPanel();

        // Create info panel
        JPanel infoPanel = createInfoPanel();

        // Add components
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(new Color(60, 60, 60));

        JLabel titleLabel = new JLabel("🎲 SNAKE AND LADDER - DIJKSTRA PATHFINDING 🎲");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JButton findPathButton = new JButton("Find Shortest Path");
        findPathButton.setFont(new Font("Arial", Font.BOLD, 14));
        findPathButton.setBackground(new Color(46, 204, 113));
        findPathButton.setForeground(Color.WHITE);
        findPathButton.setFocusPainted(false);
        findPathButton.addActionListener(e -> findShortestPath());

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));
        resetButton.setBackground(new Color(231, 76, 60));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetBoard());

        panel.add(titleLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(findPathButton);
        panel.add(resetButton);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        // Status label
        statusLabel = new JLabel("Ready to find path!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(52, 152, 219));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Legend panel
        JPanel legendPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));

        legendPanel.add(createLegendItem(new Color(144, 238, 144), "Start (Cell 1)"));
        legendPanel.add(createLegendItem(new Color(255, 215, 0), "Finish (Cell 49)"));
        legendPanel.add(createLegendItem(new Color(255, 200, 200), "Snake (↓)"));
        legendPanel.add(createLegendItem(new Color(200, 255, 200), "Ladder (↑)"));
        legendPanel.add(createLegendItem(new Color(200, 230, 255), "Visited"));
        legendPanel.add(createLegendItem(new Color(255, 255, 150), "Shortest Path"));

        // Result area
        resultArea = new JTextArea(15, 20);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Path Details"));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Info text
        JTextArea infoText = new JTextArea(
                "HOW IT WORKS:\n\n" +
                        "1. Dijkstra finds the shortest\n" +
                        "   path from Start to Finish\n\n" +
                        "2. Dice roll: 1-6 steps\n\n" +
                        "3. Snakes move you DOWN\n\n" +
                        "4. Ladders move you UP\n\n" +
                        "5. Yellow = optimal path\n\n" +
                        "6. Blue = explored cells"
        );
        infoText.setEditable(false);
        infoText.setFont(new Font("Arial", Font.PLAIN, 11));
        infoText.setBackground(new Color(255, 255, 220));
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(legendPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(infoText, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.add(panel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createLegendItem(Color color, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(Color.WHITE);

        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(30, 20));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 11));

        panel.add(colorBox);
        panel.add(label);

        return panel;
    }

    private void findShortestPath() {
        gameBoard.resetVisualization();

        int start = 1;
        int end = BOARD_SIZE * BOARD_SIZE;

        statusLabel.setText("Finding path...");
        statusLabel.setBackground(new Color(243, 156, 18));

        List<Integer> path = dijkstra.findShortestPath(start, end);

        if (!path.isEmpty()) {
            statusLabel.setText("Path found! ✓");
            statusLabel.setBackground(new Color(46, 204, 113));

            StringBuilder sb = new StringBuilder();
            sb.append("SHORTEST PATH FOUND!\n");
            sb.append("===================\n\n");
            sb.append("Steps: ").append(path.size() - 1).append("\n");
            sb.append("Distance: ").append(dijkstra.getDistance(end)).append("\n\n");
            sb.append("Path:\n");

            for (int i = 0; i < path.size(); i++) {
                int cell = path.get(i);
                sb.append(cell);

                Cell cellObj = gameBoard.getCellByNumber(cell);
                if (cellObj != null && cellObj.getTargetCell() != -1) {
                    if (cellObj.getType() == Cell.CellType.SNAKE) {
                        sb.append(" (Snake↓").append(cellObj.getTargetCell()).append(")");
                    } else if (cellObj.getType() == Cell.CellType.LADDER) {
                        sb.append(" (Ladder↑").append(cellObj.getTargetCell()).append(")");
                    }
                }

                if (i < path.size() - 1) {
                    sb.append(" → ");
                    if ((i + 1) % 5 == 0) sb.append("\n");
                }
            }

            sb.append("\n\n");
            sb.append("Cells explored: ").append(dijkstra.getVisitedCells().size());

            resultArea.setText(sb.toString());
        } else {
            statusLabel.setText("No path found! ✗");
            statusLabel.setBackground(new Color(231, 76, 60));
            resultArea.setText("No path could be found\nfrom Start to Finish.");
        }

        boardPanel.updateBoard();
    }

    private void resetBoard() {
        gameBoard.resetVisualization();
        boardPanel.updateBoard();
        resultArea.setText("");
        statusLabel.setText("Ready to find path!");
        statusLabel.setBackground(new Color(52, 152, 219));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DijkstraBoardGame game = new DijkstraBoardGame();
            game.setVisible(true);
        });
    }
}