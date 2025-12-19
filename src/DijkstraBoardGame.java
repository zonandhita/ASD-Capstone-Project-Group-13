import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class DijkstraBoardGame extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContainer = new JPanel(cardLayout);

    private GameBoard gameBoard;
    private BoardPanel boardPanel;
    private DijkstraAlgorithm dijkstra;
    private Random random = new Random();
    private SoundManager soundManager = new SoundManager();
    private List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private Timer movementTimer;
    private PriorityQueue<PlayerStat> leaderboard = new PriorityQueue<>();

    private static final int BOARD_SIZE = 7;
    private static final int CELL_SIZE = 80;

    private JTextArea resultArea;
    private JLabel[] playerStatusLabels = new JLabel[2];
    private JLabel[] playerDiceLabels = new JLabel[2];
    private JPanel[] playerPanels = new JPanel[2];
    private JButton rollDiceButton, playModeButton, resetButton;
    private JCheckBox safeModeCheck;

    private boolean isGameStarted = false;
    private String p1NameInput = "Courier A";
    private String p2NameInput = "Courier B";

    public DijkstraBoardGame() {
        setTitle("Code City Courier - Final Project Group 13");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Inisialisasi Layar Utama
        mainContainer.add(createLoginPanel(), "LOGIN");
        add(mainContainer);

        cardLayout.show(mainContainer, "LOGIN");

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * HOMEPAGE: Menampilkan Ketentuan Bermain dan Login
     */
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(1000, 700));
        panel.setBackground(new Color(44, 62, 80));

        JLabel title = new JLabel("CITY COURIER: DASHBOARD DISPATCH", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 30, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));

        // Form Login
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(236, 240, 241));
        form.setBorder(BorderFactory.createTitledBorder(null, " COURIER REGISTRATION ", 0, 0, new Font("Segoe UI", Font.BOLD, 16), Color.BLACK));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField p1Field = new JTextField(p1NameInput, 15);
        JTextField p2Field = new JTextField(p2NameInput, 15);

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Courier 1 Name (RED):"), gbc);
        gbc.gridy = 1; form.add(p1Field, gbc);
        gbc.gridy = 2; form.add(new JLabel("Courier 2 Name (BLUE):"), gbc);
        gbc.gridy = 3; form.add(p2Field, gbc);

        JButton startBtn = new JButton("CONFIRM & INITIALIZE");
        startBtn.setBackground(new Color(46, 204, 113));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startBtn.addActionListener(e -> {
            p1NameInput = p1Field.getText().trim();
            p2NameInput = p2Field.getText().trim();
            setupGame();
            cardLayout.show(mainContainer, "GAME");
        });
        gbc.gridy = 4; gbc.insets = new Insets(30, 10, 10, 10);
        form.add(startBtn, gbc);

        // KETENTUAN BERMAIN
        JTextArea rules = new JTextArea();
        rules.setEditable(false);
        rules.setBackground(new Color(52, 73, 94));
        rules.setForeground(Color.WHITE);
        rules.setFont(new Font("Consolas", Font.PLAIN, 13));
        rules.setText("--- KETENTUAN BERMAIN: CITY COURIER ---\n\n" +
                "1. MESIN & GERAK\n" +
                " - GAS: Maju (Peluang 80%).\n" +
                " - STALL: Mundur (Peluang 20%).\n" +
                " - BINTANG: Bonus 1x jalan (Kelipatan 5).\n\n" +
                "2. FITUR JALUR\n" +
                " - HIGHWAY: Lompat ke petak atas.\n" +
                " - TRAFFIC: Turun ke petak bawah.\n" +
                " - RAID: Razia! Balik ke Start (Petak 1).\n\n" +
                "3. TEKNOLOGI GPS (Petak Prima)\n" +
                " - GPS ACTIVE: Cari rute tercepat.\n" +
                " - PREMIUM: Hindari Polisi & Macet.\n\n" +
                "4. GOAL\n" +
                " - Capai Petak 49 dengan Earnings terbanyak!");
        rules.setMargin(new Insets(20, 20, 20, 20));

        content.add(form);
        content.add(rules);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void setupGame() {
        gameBoard = new GameBoard(BOARD_SIZE, CELL_SIZE);
        gameBoard.generateFeatures(5, 4);
        gameBoard.generateScores(10);
        dijkstra = new DijkstraAlgorithm(gameBoard, soundManager);

        players.clear();
        players.add(new Player(p1NameInput.isEmpty() ? "Courier 1" : p1NameInput, Color.RED));
        players.add(new Player(p2NameInput.isEmpty() ? "Courier 2" : p2NameInput, Color.BLUE));

        JPanel gamePanel = new JPanel(new BorderLayout(15, 15));
        boardPanel = new BoardPanel(gameBoard, players);
        gamePanel.add(boardPanel, BorderLayout.CENTER);
        gamePanel.add(createControlPanel(), BorderLayout.NORTH);
        gamePanel.add(createDualDashboardPanel(), BorderLayout.EAST);

        mainContainer.add(gamePanel, "GAME");
        updateTurnDisplay();
    }

    private void startGame() {
        isGameStarted = true;
        rollDiceButton.setEnabled(true);
        playModeButton.setEnabled(false);
        for(Player p : players) p.reset();
        boardPanel.repaint();
        updateTurnDisplay();
        log("SYSTEM: Dispatch shift started! Delivering...");
    }

    private void rollDice() {
        if (!isGameStarted || (movementTimer != null && movementTimer.isRunning())) return;
        rollDiceButton.setEnabled(false);
        JLabel activeDiceLabel = playerDiceLabels[currentPlayerIndex];
        activeDiceLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 45));
        String[] diceFaces = {"⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};

        Timer rollingAnimation = new Timer(80, new ActionListener() {
            int ticks = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                activeDiceLabel.setText(diceFaces[random.nextInt(6)]);
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
        activeDiceLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        activeDiceLabel.setText(dice + (isGreen ? " GAS" : " STALL"));
        activeDiceLabel.setForeground(isGreen ? new Color(34, 139, 34) : Color.RED);

        int currentPos = p.getPosition();
        StringBuilder logMsg = new StringBuilder(p.getName() + " rolls " + dice);

        if (!isGreen) {
            int target = Math.max(1, currentPos - dice);
            logMsg.append(" | STALL: Moving BACK to ").append(target);
            animateMovement(p, target, false, logMsg);
        } else {
            if (isPrime(currentPos)) {
                logMsg.append(" | GPS ACTIVE: ");
                dijkstra.setSafeMode(safeModeCheck.isSelected());
                List<Integer> path = dijkstra.findShortestPath(currentPos, BOARD_SIZE * BOARD_SIZE);
                int target = path.get(Math.min(dice, path.size() - 1));
                logMsg.append("Smart Path to ").append(target);
                animateMovement(p, target, true, logMsg);
            } else {
                int target = currentPos + dice;
                if (target > BOARD_SIZE * BOARD_SIZE) {
                    logMsg.append(" | OVERSHOOT: Waiting...");
                    log(logMsg.toString());
                    nextTurn();
                } else animateMovement(p, target, false, logMsg);
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
            p.addScore(cell.getScore());
            soundManager.playSFX("resources/star.wav");
            logMsg.append(" | INCOME +$").append(cell.getScore());
            cell.setScore(0);
        }
        if (cell != null) {
            if (cell.isTrap()) {
                soundManager.playSFX("resources/trap.wav");
                logMsg.append(" | RAID! Balik ke HQ");
                p.setPosition(1);
            } else if (cell.getType() == Cell.CellType.SNAKE) {
                soundManager.playSFX("resources/snake.wav");
                logMsg.append(" | TRAFFIC! Detour ke ").append(cell.getTargetCell());
                p.setPosition(cell.getTargetCell());
            } else if (cell.getType() == Cell.CellType.LADDER) {
                soundManager.playSFX("resources/ladder.wav");
                logMsg.append(" | HIGHWAY! Fast to ").append(cell.getTargetCell());
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
        } else if (p.getPosition() % 5 == 0 && p.getPosition() != 1) {
            log("INFO: Gas Station (Bintang) - Bonus 1x Jalan untuk " + p.getName());
            rollDiceButton.setEnabled(true);
            updateTurnDisplay();
        } else nextTurn();
    }

    private void showLeaderboard() {
        StringBuilder sb = new StringBuilder("=== TOP COURIERS ===\n");
        PriorityQueue<PlayerStat> temp = new PriorityQueue<>(leaderboard);
        int rank = 1;
        while (!temp.isEmpty()) sb.append("#").append(rank++).append(" ").append(temp.poll().toString()).append("\n");
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(sb.toString())), "Shift Complete", JOptionPane.INFORMATION_MESSAGE);
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
                playerDiceLabels[i].setText("STATUS: DRIVE");
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
        rollDiceButton.setEnabled(false);
        playModeButton.setEnabled(true);
    }

    private void log(String s) {
        resultArea.append(" > " + s + "\n");
        if (s.contains("Dispatch") || s.contains("started")) resultArea.append(" --------------------------\n");
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
        for (int i = 0; i < 2; i++) {
            playerPanels[i] = new JPanel(new BorderLayout());
            playerPanels[i].setBackground(Color.WHITE);
            playerStatusLabels[i] = new JLabel();
            playerStatusLabels[i].setOpaque(true);
            playerStatusLabels[i].setBackground(players.get(i).getColor());
            playerStatusLabels[i].setForeground(Color.WHITE);
            playerStatusLabels[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
            playerStatusLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            playerStatusLabels[i].setPreferredSize(new Dimension(0, 35));
            playerDiceLabels[i] = new JLabel("READY");
            playerDiceLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            playerPanels[i].add(playerStatusLabels[i], BorderLayout.NORTH);
            playerPanels[i].add(playerDiceLabels[i], BorderLayout.CENTER);
            dashArea.add(playerPanels[i]);
        }

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBackground(Color.WHITE);
        resultArea.setForeground(new Color(44, 62, 80));
        resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "SATELLITE LOG"));

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
        resetButton = new JButton("MAIN MENU");
        resetButton.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));
        panel.add(playModeButton); panel.add(rollDiceButton); panel.add(safeModeCheck); panel.add(resetButton);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DijkstraBoardGame().setVisible(true));
    }

    class BoardPanel extends JPanel {
        GameBoard gb; List<Player> pl;
        public BoardPanel(GameBoard gb, List<Player> pl) {
            this.gb = gb; this.pl = pl;
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
                    g2.drawString(p.getName().substring(0, 1).toUpperCase(), center.x - 4 + offset, center.y + 5 + offset);
                }
            }
        }
    }
}