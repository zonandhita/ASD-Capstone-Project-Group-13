import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class DijkstraBoardGame extends JFrame {

    private GameBoard gameBoard;
    private BoardPanel boardPanel;
    private DijkstraAlgorithm dijkstra;
    private Random random;
    private SoundManager soundManager;
    private List<Player> players;
    private int currentPlayerIndex = 0;
    private Timer movementTimer;
    private PriorityQueue<PlayerStat> leaderboard;

    private static final int BOARD_SIZE = 7;
    private static final int CELL_SIZE = 80;

    private JTextArea resultArea;
    private JLabel statusLabel;
    private JLabel diceLabel;
    private JButton rollDiceButton;
    private JButton playModeButton;
    private JButton findPathButton;
    private JButton resetButton;
    private JCheckBox safeModeCheck;

    private boolean isGameStarted = false;
    private String player1Name = "Courier 1";
    private String player2Name = "Courier 2";

    public DijkstraBoardGame() {
        setTitle("Code City Courier - Group 13"); // Judul Baru
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        soundManager = new SoundManager();
        random = new Random();
        leaderboard = new PriorityQueue<>();

        showLoginDialog();

        gameBoard = new GameBoard(BOARD_SIZE, CELL_SIZE);
        gameBoard.generateFeatures(5, 4);
        gameBoard.generateScores(10);

        dijkstra = new DijkstraAlgorithm(gameBoard, soundManager);

        players = new ArrayList<>();
        players.add(new Player(player1Name, Color.RED));
        players.add(new Player(player2Name, Color.BLUE));

        boardPanel = new BoardPanel(gameBoard, players);

        add(boardPanel, BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.NORTH);
        add(createInfoPanel(), BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        updateTurnLabel();
        rollDiceButton.setEnabled(false);
    }

    private void showLoginDialog() {
        JPanel loginPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        JTextField p1Field = new JTextField("Courier A");
        JTextField p2Field = new JTextField("Courier B");

        loginPanel.add(new JLabel("Name for Courier 1 (RED):"));
        loginPanel.add(p1Field);
        loginPanel.add(new JLabel("Name for Courier 2 (BLUE):"));
        loginPanel.add(p2Field);

        int result = JOptionPane.showConfirmDialog(null, loginPanel, "Courier Login", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (!p1Field.getText().trim().isEmpty()) player1Name = p1Field.getText().trim();
            if (!p2Field.getText().trim().isEmpty()) player2Name = p2Field.getText().trim();
        }
    }

    private void startGame() {
        isGameStarted = true;
        rollDiceButton.setEnabled(true);
        playModeButton.setEnabled(false);
        gameBoard.generateFeatures(5, 4);
        gameBoard.generateScores(10);
        for(Player p : players) p.reset();
        boardPanel.repaint();
        updateTurnLabel();
        log("DISPATCH STARTED! Deliver packages!");
    }

    private void rollDice() {
        if (!isGameStarted) return;
        Player p = players.get(currentPlayerIndex);
        p.incrementSteps();

        int dice = random.nextInt(6) + 1;
        boolean isGreen = random.nextInt(100) < 80;

        diceLabel.setText("🎲 " + dice + (isGreen ? " (GAS)" : " (STALL)")); // Istilah Baru
        diceLabel.setForeground(isGreen ? new Color(34, 139, 34) : Color.RED);

        int currentPos = p.getPosition();
        StringBuilder logMsg = new StringBuilder(p.getName() + " moves " + dice);

        if (!isGreen) {
            int target = Math.max(1, currentPos - dice);
            logMsg.append(" [ENGINE TROUBLE] -> Back to ").append(target);
            animateMovement(p, target, false, logMsg);
        } else {
            if (isPrime(currentPos)) {
                logMsg.append(" [GPS CHECKPOINT] -> ");
                dijkstra.setSafeMode(safeModeCheck.isSelected());
                List<Integer> path = dijkstra.findShortestPath(currentPos, BOARD_SIZE * BOARD_SIZE);
                int stepIndex = Math.min(dice, path.size() - 1);
                int target = path.get(stepIndex);
                logMsg.append("Smart Route to ").append(target);
                animateMovement(p, target, true, logMsg);
            } else {
                int target = currentPos + dice;
                if (target > BOARD_SIZE * BOARD_SIZE) {
                    logMsg.append(" -> Overshoot! Wait.");
                    log(logMsg.toString());
                    nextTurn();
                } else {
                    animateMovement(p, target, false, logMsg);
                }
            }
        }
    }

    private void animateMovement(Player p, int target, boolean isJump, StringBuilder logMsg) {
        rollDiceButton.setEnabled(false);
        movementTimer = new Timer(isJump ? 400 : 200, e -> {
            int curr = p.getPosition();
            if (curr == target) {
                ((Timer)e.getSource()).stop();
                handleLand(p, logMsg);
            } else {
                if (isJump) p.setPosition(target);
                else {
                    if (curr < target) p.setPosition(curr+1);
                    else p.setPosition(curr-1);
                }
                boardPanel.repaint();
            }
        });
        movementTimer.start();
    }

    private void handleLand(Player p, StringBuilder logMsg) {
        int pos = p.getPosition();
        Cell cell = gameBoard.getCellByNumber(pos);

        // ✅ LOGIC PAKET (Dulu Coin)
        if (cell != null && cell.hasScore()) {
            int bonus = cell.getScore();
            p.addScore(bonus);
            soundManager.playSFX("resources/star.wav");
            logMsg.append(" -> 📦 PICKUP PACKAGE (+$").append(bonus).append(")");
            cell.setScore(0);
        }

        // ✅ LOGIC JALANAN (Dulu Trap/Snake/Ladder)
        if (cell != null) {
            if (cell.isTrap()) {
                soundManager.playSFX("resources/trap.wav");
                logMsg.append(" -> 👮 POLICE RAID! License Suspended! (Back to HQ)");
                p.setPosition(1);
            }
            else if (cell.getType() == Cell.CellType.SNAKE) {
                soundManager.playSFX("resources/snake.wav");
                logMsg.append(" -> 🚧 TRAFFIC JAM! Detour to ").append(cell.getTargetCell());
                p.setPosition(cell.getTargetCell());
            }
            else if (cell.getType() == Cell.CellType.LADDER) {
                soundManager.playSFX("resources/ladder.wav");
                logMsg.append(" -> 🛣️ HIGHWAY! Speed up to ").append(cell.getTargetCell());
                p.setPosition(cell.getTargetCell());
            }
        }

        boardPanel.repaint();
        log(logMsg.toString());

        if (p.getPosition() == BOARD_SIZE * BOARD_SIZE) {
            soundManager.playSFX("resources/win.wav");
            leaderboard.add(new PlayerStat(p.getName(), p.getScore(), p.getTotalSteps()));
            showLeaderboard(p);
            resetGame();
        }
        else if (p.getPosition() % 5 == 0 && p.getPosition() != 1) {
            soundManager.playSFX("resources/star.wav");
            log("⛽ Gas Station! Free Refill (Roll Again).");
            rollDiceButton.setEnabled(true);
            updateTurnLabel();
        }
        else {
            nextTurn();
        }
    }

    private void showLeaderboard(Player winner) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎉 DELIVERY COMPLETE: ").append(winner.getName()).append(" 🎉\n");
        sb.append("Total Earnings: $").append(winner.getScore()).append("\n\n");
        sb.append("=== 🏆 BEST COURIERS 🏆 ===\n");

        PriorityQueue<PlayerStat> tempQueue = new PriorityQueue<>(leaderboard);
        int rank = 1;
        while (!tempQueue.isEmpty()) {
            PlayerStat stat = tempQueue.poll();
            sb.append("#").append(rank).append(" ").append(stat.toString()).append("\n");
            rank++;
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Shift Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        updateTurnLabel();
        rollDiceButton.setEnabled(true);
    }

    private void updateTurnLabel() {
        Player p = players.get(currentPlayerIndex);
        statusLabel.setText("Driver: " + p.getName() + " (Earnings: $" + p.getScore() + ")");
        statusLabel.setBackground(p.getColor());
        statusLabel.setForeground(Color.WHITE);
    }

    private void resetGame() {
        isGameStarted = false;
        currentPlayerIndex = 0;
        for(Player p : players) p.reset();
        gameBoard.generateFeatures(5, 4);
        gameBoard.generateScores(10);
        boardPanel.repaint();
        playModeButton.setEnabled(true);
        rollDiceButton.setEnabled(false);
        log("--- SYSTEM RESET ---");
    }

    private void showDemoPath() {
        dijkstra.setSafeMode(safeModeCheck.isSelected());
        List<Integer> path = dijkstra.findShortestPath(1, BOARD_SIZE * BOARD_SIZE);
        log("Calculating GPS Route... " + path);
    }

    private void log(String s) {
        resultArea.append(s + "\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i=2; i<=Math.sqrt(n); i++) if (n%i == 0) return false;
        return true;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(new Color(50, 50, 60));

        JLabel titleLabel = new JLabel("CITY COURIER 📦");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        playModeButton = new JButton("🚀 Start Shift");
        playModeButton.setBackground(new Color(46, 204, 113));
        playModeButton.setForeground(Color.WHITE);
        playModeButton.setFocusPainted(false);
        playModeButton.addActionListener(e -> startGame());

        rollDiceButton = new JButton("🎲 Drive");
        rollDiceButton.setBackground(new Color(241, 196, 15));
        rollDiceButton.setForeground(Color.BLACK);
        rollDiceButton.setFocusPainted(false);
        rollDiceButton.addActionListener(e -> rollDice());

        safeModeCheck = new JCheckBox("Premium GPS");
        safeModeCheck.setBackground(new Color(50,50,60));
        safeModeCheck.setForeground(Color.WHITE);
        safeModeCheck.setToolTipText("Avoid Traffic & Police");

        findPathButton = new JButton("📡 GPS Test");
        findPathButton.setBackground(new Color(52, 152, 219));
        findPathButton.setForeground(Color.WHITE);
        findPathButton.setFocusPainted(false);
        findPathButton.addActionListener(e -> showDemoPath());

        resetButton = new JButton("🔄 Reset");
        resetButton.setBackground(new Color(231, 76, 60));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetGame());

        panel.add(titleLabel);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(playModeButton);
        panel.add(rollDiceButton);
        panel.add(safeModeCheck);
        panel.add(findPathButton);
        panel.add(resetButton);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        statusLabel = new JLabel("Waiting for Dispatch...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(52, 152, 219));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        diceLabel = new JLabel("⛽ Engine: OFF");
        diceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        diceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        diceLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        diceLabel.setPreferredSize(new Dimension(280, 50));

        JPanel legendPanel = new JPanel(new GridLayout(7, 1, 5, 5));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Map Legend"));
        legendPanel.add(createLegendItem(new Color(255, 215, 0), "GPS Point (Gold)"));
        legendPanel.add(createLegendItem(new Color(255, 100, 100), "Police Raid (Red)"));
        legendPanel.add(createLegendItem(new Color(205, 133, 63), "Package ($$$)"));
        legendPanel.add(createLegendItem(Color.RED, "Courier A"));
        legendPanel.add(createLegendItem(Color.BLUE, "Courier B"));

        resultArea = new JTextArea(15, 20);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        JPanel topInfo = new JPanel(new BorderLayout(5,5));
        topInfo.add(statusLabel, BorderLayout.NORTH);
        topInfo.add(diceLabel, BorderLayout.CENTER);

        panel.add(topInfo, BorderLayout.NORTH);
        panel.add(legendPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLegendItem(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        p.add(colorBox);
        p.add(new JLabel(text));
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DijkstraBoardGame().setVisible(true));
    }

    class BoardPanel extends JPanel {
        GameBoard gb;
        List<Player> pl;

        public BoardPanel(GameBoard gb, List<Player> pl) {
            this.gb = gb;
            this.pl = pl;
            int size = BOARD_SIZE * CELL_SIZE;
            setPreferredSize(new Dimension(size, size));

            ToolTipManager.sharedInstance().setInitialDelay(100);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Cell c = gb.getCellAtPixel(e.getX(), e.getY());
                    if (c != null) {
                        String txt = "Loc " + c.getNumber();
                        if (c.hasScore()) txt += " [PACKAGE +$" + c.getScore() + "]";
                        if (c.isPrime()) txt += " [GPS SIGNAL]";
                        if (c.isTrap()) txt += " [POLICE RAID]";
                        if (c.getType() == Cell.CellType.SNAKE) txt += " (Traffic Jam -> "+c.getTargetCell()+")";
                        if (c.getType() == Cell.CellType.LADDER) txt += " (Highway -> "+c.getTargetCell()+")";
                        setToolTipText(txt);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            gb.draw(g2);

            for (int i = 0; i < pl.size(); i++) {
                Player p = pl.get(i);
                Cell c = gb.getCellByNumber(p.getPosition());
                if (c != null) {
                    Point center = c.getCenter();
                    int offset = (i * 12) - 6;
                    g2.setColor(p.getColor());
                    g2.fillOval(center.x - 15 + offset, center.y - 15 + offset, 30, 30);
                    g2.setColor(Color.WHITE);
                    g2.drawOval(center.x - 15 + offset, center.y - 15 + offset, 30, 30);

                    String label = p.getName().substring(0, 1).toUpperCase();
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    g2.drawString(label, center.x - 3 + offset, center.y + 4 + offset);
                }
            }
        }
    }
}