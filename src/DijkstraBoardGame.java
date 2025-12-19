import javax.swing.*;
import javax.swing.border.TitledBorder;
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

    private JLabel[] playerStatusLabels;
    private JLabel[] playerDiceLabels;
    private JPanel[] playerPanels;

    private JButton rollDiceButton;
    private JButton playModeButton;
    private JButton findPathButton;
    private JButton resetButton;
    private JCheckBox safeModeCheck;

    private boolean isGameStarted = false;
    private String player1Name = "Courier 1";
    private String player2Name = "Courier 2";

    public DijkstraBoardGame() {
        setTitle("Code City Courier - Final Group 13");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

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
        add(createDualDashboardPanel(), BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        updateTurnDisplay();
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
        updateTurnDisplay();
        log("SYSTEM: Dispatch started! Deliver all packages.");
    }

    /**
     * MENGEMBALIKAN ANIMASI DADU UNICODE:
     * Menggunakan font Segoe UI Symbol agar karakter ⚀-⚅ muncul dengan transisi warna acak.
     */
    private void rollDice() {
        if (!isGameStarted || (movementTimer != null && movementTimer.isRunning())) return;

        rollDiceButton.setEnabled(false);
        JLabel activeDiceLabel = playerDiceLabels[currentPlayerIndex];

        // Memaksa penggunaan font simbol agar dadu muncul
        activeDiceLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 45));
        String[] diceFaces = {"⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};

        Timer rollingAnimation = new Timer(80, new ActionListener() {
            int ticks = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                int randomIndex = random.nextInt(6);
                activeDiceLabel.setText(diceFaces[randomIndex]);
                activeDiceLabel.setForeground(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));

                ticks++;
                if (ticks > 12) {
                    ((Timer)e.getSource()).stop();
                    finalizeRoll();
                }
            }
        });
        rollingAnimation.start();
    }

    private void finalizeRoll() {
        Player p = players.get(currentPlayerIndex);
        p.incrementSteps();

        int dice = random.nextInt(6) + 1;
        boolean isGreen = random.nextInt(100) < 80;

        JLabel activeDiceLabel = playerDiceLabels[currentPlayerIndex];
        activeDiceLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28));
        activeDiceLabel.setText("🎲 " + dice + (isGreen ? " GAS" : " STALL"));
        activeDiceLabel.setForeground(isGreen ? new Color(34, 139, 34) : Color.RED);

        int currentPos = p.getPosition();
        StringBuilder logMsg = new StringBuilder(p.getName() + " moves " + dice);

        if (!isGreen) {
            int target = Math.max(1, currentPos - dice);
            logMsg.append(" | ENGINE STALL: Back to ").append(target);
            animateMovement(p, target, false, logMsg);
        } else {
            if (isPrime(currentPos)) {
                logMsg.append(" | GPS ACTIVE: ");
                dijkstra.setSafeMode(safeModeCheck.isSelected());
                List<Integer> path = dijkstra.findShortestPath(currentPos, BOARD_SIZE * BOARD_SIZE);
                int target = path.get(Math.min(dice, path.size() - 1));
                logMsg.append("Smart Route to ").append(target);
                animateMovement(p, target, true, logMsg);
            } else {
                int target = currentPos + dice;
                if (target > BOARD_SIZE * BOARD_SIZE) {
                    logMsg.append(" | OVERSHOOT: Waiting...");
                    log(logMsg.toString());
                    nextTurn();
                } else {
                    animateMovement(p, target, false, logMsg);
                }
            }
        }
    }

    private void animateMovement(Player p, int target, boolean isJump, StringBuilder logMsg) {
        movementTimer = new Timer(isJump ? 400 : 200, e -> {
            int curr = p.getPosition();
            if (curr == target) {
                ((Timer)e.getSource()).stop();
                handleLand(p, logMsg);
            } else {
                if (isJump) p.setPosition(target);
                else p.setPosition(curr + (curr < target ? 1 : -1));
                boardPanel.repaint();
            }
        });
        movementTimer.start();
    }

    private void handleLand(Player p, StringBuilder logMsg) {
        Cell cell = gameBoard.getCellByNumber(p.getPosition());

        if (cell != null && cell.hasScore()) {
            int bonus = cell.getScore();
            p.addScore(bonus);
            soundManager.playSFX("resources/star.wav");
            logMsg.append(" | INCOME +$").append(bonus);
            cell.setScore(0);
        }

        if (cell != null) {
            if (cell.isTrap()) {
                soundManager.playSFX("resources/trap.wav");
                logMsg.append(" | POLICE RAID: HQ return");
                p.setPosition(1);
            }
            else if (cell.getType() == Cell.CellType.SNAKE) {
                soundManager.playSFX("resources/snake.wav");
                logMsg.append(" | TRAFFIC: Detour to ").append(cell.getTargetCell());
                p.setPosition(cell.getTargetCell());
            }
            else if (cell.getType() == Cell.CellType.LADDER) {
                soundManager.playSFX("resources/ladder.wav");
                logMsg.append(" | HIGHWAY: Fast to ").append(cell.getTargetCell());
                p.setPosition(cell.getTargetCell());
            }
        }

        boardPanel.repaint();
        log(logMsg.toString());

        if (p.getPosition() == BOARD_SIZE * BOARD_SIZE) {
            soundManager.playSFX("resources/win.wav");
            leaderboard.add(new PlayerStat(p.getName(), p.getScore(), p.getTotalSteps()));
            showLeaderboard();
            resetGame();
        }
        else if (p.getPosition() % 5 == 0 && p.getPosition() != 1) {
            log("INFO: Gas Station - Free turn for " + p.getName());
            rollDiceButton.setEnabled(true);
            updateTurnDisplay();
        }
        else {
            nextTurn();
        }
    }

    private void showLeaderboard() {
        StringBuilder sb = new StringBuilder("=== TOP COURIERS ===\n");
        PriorityQueue<PlayerStat> temp = new PriorityQueue<>(leaderboard);
        int rank = 1;
        while (!temp.isEmpty()) {
            sb.append("#").append(rank++).append(" ").append(temp.poll().toString()).append("\n");
        }
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(sb.toString())), "Shift Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        updateTurnDisplay();
        rollDiceButton.setEnabled(true);
    }

    private void updateTurnDisplay() {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            playerStatusLabels[i].setText(p.getName().toUpperCase() + " | Earnings: $" + p.getScore());

            if (i == currentPlayerIndex) {
                playerPanels[i].setBorder(BorderFactory.createLineBorder(p.getColor(), 4));
                playerPanels[i].setBackground(Color.WHITE);
                playerDiceLabels[i].setText("STATUS: READY");
            } else {
                playerPanels[i].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                playerPanels[i].setBackground(new Color(245, 245, 245));
                playerDiceLabels[i].setText("STATUS: STANDBY");
            }
        }
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
    }

    private void log(String s) {
        resultArea.append(" > " + s + "\n");
        if (s.contains("Shift") || s.contains("started")) {
            resultArea.append(" --------------------------\n");
        }
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i=2; i<=Math.sqrt(n); i++) if (n%i == 0) return false;
        return true;
    }

    private JPanel createDualDashboardPanel() {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setPreferredSize(new Dimension(320, 0));
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        container.setBackground(new Color(235, 235, 240));

        JPanel dashArea = new JPanel(new GridLayout(2, 1, 10, 10));
        dashArea.setOpaque(false);

        playerPanels = new JPanel[2];
        playerStatusLabels = new JLabel[2];
        playerDiceLabels = new JLabel[2];

        for (int i = 0; i < 2; i++) {
            playerPanels[i] = new JPanel(new BorderLayout());
            playerPanels[i].setBackground(Color.WHITE);

            playerStatusLabels[i] = new JLabel("PLAYER " + (i+1));
            playerStatusLabels[i].setOpaque(true);
            playerStatusLabels[i].setBackground(players.get(i).getColor());
            playerStatusLabels[i].setForeground(Color.WHITE);
            playerStatusLabels[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
            playerStatusLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            playerStatusLabels[i].setPreferredSize(new Dimension(0, 35));

            playerDiceLabels[i] = new JLabel("READY");
            playerDiceLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            playerDiceLabels[i].setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));

            playerPanels[i].add(playerStatusLabels[i], BorderLayout.NORTH);
            playerPanels[i].add(playerDiceLabels[i], BorderLayout.CENTER);
            dashArea.add(playerPanels[i]);
        }

        // LOG DENGAN BACKGROUND PUTIH BERSIH
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBackground(Color.WHITE);
        resultArea.setForeground(new Color(44, 62, 80));
        resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "SATELLITE LOG",
                TitledBorder.LEFT, TitledBorder.TOP, null, Color.GRAY));

        container.add(dashArea, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(44, 62, 80));

        playModeButton = new JButton("START SHIFT");
        playModeButton.setBackground(new Color(46, 204, 113));
        playModeButton.setForeground(Color.WHITE);
        playModeButton.addActionListener(e -> startGame());

        rollDiceButton = new JButton("DRIVE");
        rollDiceButton.setBackground(new Color(241, 196, 15));
        rollDiceButton.addActionListener(e -> rollDice());

        safeModeCheck = new JCheckBox("PREMIUM GPS");
        safeModeCheck.setForeground(Color.WHITE);
        safeModeCheck.setOpaque(false);

        resetButton = new JButton("RESET");
        resetButton.addActionListener(e -> resetGame());

        panel.add(playModeButton);
        panel.add(rollDiceButton);
        panel.add(safeModeCheck);
        panel.add(resetButton);

        return panel;
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
            setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
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

                    if (i == currentPlayerIndex) {
                        g2.setColor(new Color(p.getColor().getRed(), p.getColor().getGreen(), p.getColor().getBlue(), 80));
                        g2.fillOval(center.x - 22 + offset, center.y - 22 + offset, 44, 44);
                    }

                    g2.setColor(p.getColor());
                    g2.fillOval(center.x - 15 + offset, center.y - 15 + offset, 30, 30);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(center.x - 15 + offset, center.y - 15 + offset, 30, 30);

                    // Gunakan inisial nama yang bersih
                    g2.setFont(new Font("Arial", Font.BOLD, 12));
                    g2.drawString(p.getName().substring(0, 1).toUpperCase(), center.x - 4 + offset, center.y + 5 + offset);
                }
            }
        }
    }
}