import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
// Import tambahan untuk audio
import javax.sound.sampled.*;
import java.io.File;

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
    private static final int CELL_SIZE = 75;

    private JTextArea resultArea;
    private JLabel[] playerStatusLabels = new JLabel[2];
    private JLabel[] playerDiceLabels = new JLabel[2];
    private JPanel[] playerPanels = new JPanel[2];
    private JButton rollDiceButton, playModeButton, resetButton, resetMapButton;
    private JCheckBox safeModeCheck;

    private boolean isGameStarted = false;
    private String p1NameInput = "Courier A";
    private String p2NameInput = "Courier B";

    // Variabel untuk mengontrol musik latar
    private Clip backgroundClip;

    public DijkstraBoardGame() {
        setTitle("Code City Courier - Final Project Group 13");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout(0, 0));

        mainContainer.add(createLoginPanel(), "LOGIN");
        add(mainContainer, BorderLayout.CENTER);

        cardLayout.show(mainContainer, "LOGIN");

        // Memanggil fungsi untuk menyalakan backsound
        playLoopingBacksound("resources/backsound1.wav");

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    /**
     * Logika untuk memutar musik latar secara terus-menerus (looping)
     */
    private void playLoopingBacksound(String filePath) {
        try {
            File musicPath = new File(filePath);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                backgroundClip = AudioSystem.getClip();
                backgroundClip.open(audioInput);

                // Mengatur agar musik berputar terus menerus
                backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
                backgroundClip.start();
            } else {
                System.out.println("Sistem: File backsound1.wav tidak ditemukan di folder resources/");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(850, 600));
        panel.setBackground(new Color(44, 62, 80));

        JLabel title = new JLabel("CITY COURIER: DISPATCH CENTER", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(236, 240, 241));
        form.setPreferredSize(new Dimension(450, 320));
        form.setBorder(BorderFactory.createTitledBorder(null, " COURIER REGISTRATION ", 0, 0, new Font("Segoe UI", Font.BOLD, 14), Color.BLACK));

        JTextField p1Field = new JTextField(p1NameInput, 15);
        JTextField p2Field = new JTextField(p2NameInput, 15);

        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.insets = new Insets(5, 10, 5, 10);
        fGbc.fill = GridBagConstraints.HORIZONTAL;
        fGbc.gridx = 0; fGbc.gridy = 0; form.add(new JLabel("Courier 1 (RED):"), fGbc);
        fGbc.gridy = 1; form.add(p1Field, fGbc);
        fGbc.gridy = 2; form.add(new JLabel("Courier 2 (BLUE):"), fGbc);
        fGbc.gridy = 3; form.add(p2Field, fGbc);

        JButton rulesBtn = new JButton("LIHAT KETENTUAN BERMAIN");
        rulesBtn.setBackground(new Color(52, 152, 219));
        rulesBtn.setForeground(Color.WHITE);
        rulesBtn.addActionListener(e -> showRulesPopup());
        fGbc.gridy = 4; fGbc.insets = new Insets(20, 10, 5, 10);
        form.add(rulesBtn, fGbc);

        JButton startBtn = new JButton("START SHIFT");
        startBtn.setBackground(new Color(46, 204, 113));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startBtn.addActionListener(e -> {
            p1NameInput = p1Field.getText().trim();
            p2NameInput = p2Field.getText().trim();
            setupGame();
            cardLayout.show(mainContainer, "GAME");
            pack();
            setLocationRelativeTo(null);
        });
        fGbc.gridy = 5; fGbc.insets = new Insets(10, 10, 10, 10);
        form.add(startBtn, fGbc);

        centerPanel.add(form);
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void showRulesPopup() {
        String rulesText = "--- KETENTUAN BERMAIN: CITY COURIER ---\n\n" +
                "1. MESIN & GERAK\n" +
                " - GAS: Maju (80%).\n" +
                " - STALL: Mundur (20%).\n" +
                " - BINTANG: Bonus 1x jalan.\n\n" +
                "2. FITUR JALUR\n" +
                " - HIGHWAY: Lompat ke atas.\n" +
                " - TRAFFIC: Turun ke bawah.\n" +
                " - RAID: Razia! Balik ke Start.\n\n" +
                "3. GPS DIJKSTRA\n" +
                " - PREMIUM: Hindari Polisi & Macet.\n\n" +
                "4. GOAL\n" +
                " - Capai Petak 49 dengan Earnings terbanyak!";

        JTextArea textArea = new JTextArea(rulesText);
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "PROTOKOL SHIFT", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupGame() {
        gameBoard = new GameBoard(BOARD_SIZE, CELL_SIZE);
        gameBoard.generateFeatures(5, 4);
        gameBoard.generateScores(10);
        dijkstra = new DijkstraAlgorithm(gameBoard, soundManager);

        players.clear();
        players.add(new Player(p1NameInput.isEmpty() ? "Courier 1" : p1NameInput, Color.RED));
        players.add(new Player(p2NameInput.isEmpty() ? "Courier 2" : p2NameInput, Color.BLUE));

        JPanel gamePanel = new JPanel(new BorderLayout(0, 0));
        boardPanel = new BoardPanel(gameBoard, players);

        gamePanel.add(boardPanel, BorderLayout.CENTER);
        gamePanel.add(createControlPanel(), BorderLayout.NORTH);
        gamePanel.add(createDualDashboardPanel(), BorderLayout.EAST);

        mainContainer.add(gamePanel, "GAME");
        updateTurnDisplay();
    }

    private void resetMap() {
        if (movementTimer != null && movementTimer.isRunning()) movementTimer.stop();
        gameBoard.generateFeatures(5, 4);
        gameBoard.generateScores(10);
        for (Player p : players) p.setPosition(1);
        isGameStarted = false;
        rollDiceButton.setEnabled(false);
        playModeButton.setEnabled(true);
        resultArea.setText("");
        log("SYSTEM: Map reset! Courier back to HQ.");
        boardPanel.repaint();
    }

    private void startGame() {
        isGameStarted = true;
        rollDiceButton.setEnabled(true);
        playModeButton.setEnabled(false);
        for(Player p : players) p.reset();
        boardPanel.repaint();
        updateTurnDisplay();
        log("SYSTEM: Dispatch shift started!");
    }

    private void rollDice() {
        if (!isGameStarted || (movementTimer != null && movementTimer.isRunning())) return;
        rollDiceButton.setEnabled(false);
        JLabel activeDiceLabel = playerDiceLabels[currentPlayerIndex];
        activeDiceLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 45));
        String[] diceFaces = {"⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};

        Timer rollingAnimation = new Timer(80, e -> {
            activeDiceLabel.setText(diceFaces[random.nextInt(6)]);
            activeDiceLabel.setForeground(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
        });

        Timer stopper = new Timer(1000, e -> {
            rollingAnimation.stop();
            finalizeRoll();
        });
        stopper.setRepeats(false);
        rollingAnimation.start();
        stopper.start();
    }

    private void finalizeRoll() {
        Player p = players.get(currentPlayerIndex);
        p.incrementSteps();
        int dice = random.nextInt(6) + 1;
        boolean isGreen = random.nextInt(100) < 80;

        JLabel activeDiceLabel = playerDiceLabels[currentPlayerIndex];
        activeDiceLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28));
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
                logMsg.append("Path to ").append(target);
                animateMovement(p, target, true, logMsg);
            } else {
                int target = currentPos + dice;
                if (target > BOARD_SIZE * BOARD_SIZE) {
                    logMsg.append(" | OVERSHOOT!");
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
                logMsg.append(" | RAID! Back to HQ");
                p.setPosition(1);
            } else if (cell.getType() == Cell.CellType.SNAKE) {
                soundManager.playSFX("resources/snake.wav");
                p.setPosition(cell.getTargetCell());
            } else if (cell.getType() == Cell.CellType.LADDER) {
                soundManager.playSFX("resources/ladder.wav");
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
            log("INFO: Gas Station - Bonus for " + p.getName());
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
            playerStatusLabels[i].setText(p.getName().toUpperCase() + " | $" + p.getScore());
            if (i == currentPlayerIndex) {
                playerPanels[i].setBorder(BorderFactory.createLineBorder(p.getColor(), 4));
                playerPanels[i].setBackground(Color.WHITE);
                playerDiceLabels[i].setText("DRIVE");
            } else {
                playerPanels[i].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                playerPanels[i].setBackground(new Color(245, 245, 245));
                playerDiceLabels[i].setText("STANDBY");
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
        JPanel container = new JPanel(new BorderLayout());
        int totalBoardHeight = BOARD_SIZE * CELL_SIZE;
        container.setPreferredSize(new Dimension(320, totalBoardHeight));
        container.setBackground(new Color(235, 235, 240));

        JPanel dashArea = new JPanel(new GridLayout(2, 1, 5, 5));
        dashArea.setOpaque(false);
        dashArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        for (int i = 0; i < 2; i++) {
            playerPanels[i] = new JPanel(new BorderLayout());
            playerPanels[i].setBackground(Color.WHITE);
            playerStatusLabels[i] = new JLabel();
            playerStatusLabels[i].setOpaque(true);
            playerStatusLabels[i].setBackground(players.get(i).getColor());
            playerStatusLabels[i].setForeground(Color.WHITE);
            playerStatusLabels[i].setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        resultArea.setMargin(new Insets(5, 5, 5, 5));

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "SATELLITE LOG"));

        container.add(dashArea, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
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

        resetMapButton = new JButton("🔄 RESET MAP");
        resetMapButton.setBackground(new Color(52, 152, 219));
        resetMapButton.setForeground(Color.WHITE);
        resetMapButton.addActionListener(e -> resetMap());

        resetButton = new JButton("MAIN MENU");
        resetButton.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));

        panel.add(playModeButton);
        panel.add(rollDiceButton);
        panel.add(safeModeCheck);
        panel.add(resetMapButton);
        panel.add(resetButton);

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
                    g2.fillOval(center.x - 15 + offset, center.y - 15 + offset, 28, 28);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(center.x - 15 + offset, center.y - 15 + offset, 28, 28);
                    g2.drawString(p.getName().substring(0, 1).toUpperCase(), center.x - 4 + offset, center.y + 5 + offset);
                }
            }
        }
    }
}