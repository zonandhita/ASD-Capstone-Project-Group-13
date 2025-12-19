import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class DijkstraBoardGame extends JFrame {
    private GameBoard gameBoard;
    private BoardPanel boardPanel;
    private DijkstraAlgorithm dijkstra;
    private JTextArea resultArea;
    private JLabel statusLabel;
    private JLabel diceLabel;
    private JButton rollDiceButton;
    private JButton playModeButton;
    private JButton findPathButton;

    private int currentPlayerPosition;
    private int rollCount;
    private boolean isPlayMode;
    private Random random;

    private static final int BOARD_SIZE = 7;
    private static final int CELL_SIZE = 80;

    public DijkstraBoardGame() {
        setTitle("Snake and Ladder - Dijkstra Shortest Path");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Initialize
        gameBoard = new GameBoard(BOARD_SIZE, CELL_SIZE);
        boardPanel = new BoardPanel(gameBoard);
        dijkstra = new DijkstraAlgorithm(gameBoard);
        random = new Random();
        currentPlayerPosition = 1;
        rollCount = 0;
        isPlayMode = false;

        // Create panels
        JPanel controlPanel = createControlPanel();
        JPanel infoPanel = createInfoPanel();

        // Add components
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        updatePlayerPosition();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(new Color(60, 60, 60));

        JLabel titleLabel = new JLabel("🎲 SNAKE AND LADDER 🎲");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        // Play Mode Button
        playModeButton = new JButton("🎮 Play Game");
        playModeButton.setFont(new Font("Arial", Font.BOLD, 14));
        playModeButton.setBackground(new Color(52, 152, 219));
        playModeButton.setForeground(Color.WHITE);
        playModeButton.setFocusPainted(false);
        playModeButton.addActionListener(e -> togglePlayMode());

        // Roll Dice Button
        rollDiceButton = new JButton("🎲 Roll Dice");
        rollDiceButton.setFont(new Font("Arial", Font.BOLD, 14));
        rollDiceButton.setBackground(new Color(241, 196, 15));
        rollDiceButton.setForeground(Color.WHITE);
        rollDiceButton.setFocusPainted(false);
        rollDiceButton.setEnabled(false);
        rollDiceButton.addActionListener(e -> rollDice());

        // Find Path Button
        findPathButton = new JButton("🔍 Find Shortest Path");
        findPathButton.setFont(new Font("Arial", Font.BOLD, 14));
        findPathButton.setBackground(new Color(46, 204, 113));
        findPathButton.setForeground(Color.WHITE);
        findPathButton.setFocusPainted(false);
        findPathButton.addActionListener(e -> findShortestPath());

        JButton resetButton = new JButton("🔄 Reset");
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));
        resetButton.setBackground(new Color(231, 76, 60));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetGame());

        panel.add(titleLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(playModeButton);
        panel.add(rollDiceButton);
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
        statusLabel = new JLabel("Select a mode to start!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(52, 152, 219));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Dice display
        diceLabel = new JLabel("🎲 Roll: -");
        diceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        diceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        diceLabel.setOpaque(true);
        diceLabel.setBackground(Color.WHITE);
        diceLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        diceLabel.setPreferredSize(new Dimension(280, 50));

        // Legend panel
        JPanel legendPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));

        legendPanel.add(createLegendItem(new Color(152, 251, 152), "Start (Cell 1)"));
        legendPanel.add(createLegendItem(new Color(255, 215, 0), "Finish (Cell 49)"));
        legendPanel.add(createLegendItem(new Color(255, 182, 193), "Snake (↓)"));
        legendPanel.add(createLegendItem(new Color(144, 238, 144), "Ladder (↑)"));
        legendPanel.add(createLegendItem(new Color(173, 216, 230), "Visited"));
        legendPanel.add(createLegendItem(new Color(255, 223, 0), "Shortest Path"));

        // Result area
        resultArea = new JTextArea(15, 20);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Game Info"));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Info text
        JTextArea infoText = new JTextArea(
                "🎮 PLAY MODE:\n" +
                        "Click 'Play Game' to start\n" +
                        "Roll dice to move (1-6)\n" +
                        "Reach cell 49 to win!\n\n" +
                        "🔍 PATHFINDING MODE:\n" +
                        "Find optimal path using\n" +
                        "Dijkstra's algorithm\n\n" +
                        "🐍 Snakes: Move DOWN\n" +
                        "🪜 Ladders: Move UP"
        );
        infoText.setEditable(false);
        infoText.setFont(new Font("Arial", Font.PLAIN, 11));
        infoText.setBackground(new Color(255, 255, 220));
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with status and dice
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(diceLabel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(legendPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(infoText, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
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

    private void togglePlayMode() {
        isPlayMode = !isPlayMode;

        if (isPlayMode) {
            // Start play mode
            resetGame();
            playModeButton.setText("🔍 Path Mode");
            playModeButton.setBackground(new Color(155, 89, 182));
            rollDiceButton.setEnabled(true);
            findPathButton.setEnabled(false);
            statusLabel.setText("🎮 PLAY MODE - Roll the dice!");
            statusLabel.setBackground(new Color(52, 152, 219));

            resultArea.setText("🎮 GAME STARTED!\n" +
                    "=================\n\n" +
                    "Current Position: 1\n" +
                    "Rolls: 0\n\n" +
                    "Roll the dice to move!\n" +
                    "First to reach 49 wins!");
        } else {
            // Switch to pathfinding mode
            playModeButton.setText("🎮 Play Game");
            playModeButton.setBackground(new Color(52, 152, 219));
            rollDiceButton.setEnabled(false);
            findPathButton.setEnabled(true);
            statusLabel.setText("Ready to find path!");
            statusLabel.setBackground(new Color(46, 204, 113));
            resetGame();
        }
    }

    private void rollDice() {
        if (currentPlayerPosition >= BOARD_SIZE * BOARD_SIZE) {
            return;
        }

        // Roll dice
        int diceValue = random.nextInt(6) + 1;
        rollCount++;
        diceLabel.setText("🎲 Roll: " + diceValue);

        // Move player
        int oldPosition = currentPlayerPosition;
        int newPosition = currentPlayerPosition + diceValue;

        // Check if exceeds board
        if (newPosition > BOARD_SIZE * BOARD_SIZE) {
            resultArea.append("\n❌ Roll too high! Stay at " + currentPlayerPosition);
            return;
        }

        currentPlayerPosition = newPosition;

        // Check for snake or ladder
        Cell currentCell = gameBoard.getCellByNumber(currentPlayerPosition);
        String moveInfo = "Roll #" + rollCount + ": " + diceValue + "\n";
        moveInfo += oldPosition + " → " + currentPlayerPosition;

        if (currentCell != null && currentCell.getTargetCell() != -1) {
            if (currentCell.getType() == Cell.CellType.SNAKE) {
                moveInfo += " 🐍 Snake! → " + currentCell.getTargetCell();
                currentPlayerPosition = currentCell.getTargetCell();
            } else if (currentCell.getType() == Cell.CellType.LADDER) {
                moveInfo += " 🪜 Ladder! → " + currentCell.getTargetCell();
                currentPlayerPosition = currentCell.getTargetCell();
            }
        }

        resultArea.append("\n" + moveInfo);

        // Check win condition
        if (currentPlayerPosition >= BOARD_SIZE * BOARD_SIZE) {
            statusLabel.setText("🎉 YOU WIN!");
            statusLabel.setBackground(new Color(46, 204, 113));
            rollDiceButton.setEnabled(false);
            resultArea.append("\n\n🎉🎉🎉 WINNER! 🎉🎉🎉");
            resultArea.append("\nTotal Rolls: " + rollCount);
        }

        updatePlayerPosition();
    }

    private void updatePlayerPosition() {
        gameBoard.resetVisualization();
        Cell playerCell = gameBoard.getCellByNumber(currentPlayerPosition);
        if (playerCell != null) {
            playerCell.setPath(true);
        }
        boardPanel.updateBoard();
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

    private void resetGame() {
        currentPlayerPosition = 1;
        rollCount = 0;
        diceLabel.setText("🎲 Roll: -");
        gameBoard.resetVisualization();
        updatePlayerPosition();

        if (isPlayMode) {
            resultArea.setText("🎮 GAME RESET!\n" +
                    "=================\n\n" +
                    "Current Position: 1\n" +
                    "Rolls: 0\n\n" +
                    "Roll the dice to start!");
            rollDiceButton.setEnabled(true);
            statusLabel.setText("🎮 PLAY MODE - Roll the dice!");
            statusLabel.setBackground(new Color(52, 152, 219));
        } else {
            resultArea.setText("");
            statusLabel.setText("Ready to find path!");
            statusLabel.setBackground(new Color(52, 152, 219));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DijkstraBoardGame game = new DijkstraBoardGame();
            game.setVisible(true);
        });
    }
}