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
        setTitle("Code City Courier - Group 13");
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

    /**
     * Memperbaiki animasi dadu agar simbol muncul (tidak kotak putih)
     * dengan memaksa penggunaan font Segoe UI Symbol.
     */
    private void rollDice() {
        if (!isGameStarted || (movementTimer != null && movementTimer.isRunning())) return;

        rollDiceButton.setEnabled(false);

        // Memastikan font mendukung simbol dadu Unicode
        diceLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 45));
        String[] diceFaces = {"⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};

        Timer rollingAnimation = new Timer(80, new ActionListener() {
            int ticks = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                int randomIndex = random.nextInt(6);
                diceLabel.setText(diceFaces[randomIndex]);
                diceLabel.setForeground(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));

                ticks++;
                if (ticks > 15) {
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

        // Tampilkan hasil akhir dengan teks status GAS/STALL
        diceLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 30));
        diceLabel.setText("🎲 " + dice + (isGreen ? " GAS" : " STALL"));
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

        if (cell != null && cell.hasScore()) {
            int bonus = cell.getScore();
            p.addScore(bonus);
            soundManager.playSFX("resources/star.wav");
            logMsg.append(" -> 📦 PICKUP (+$").append(bonus).append(")");
            cell.setScore(0);
        }

        if (cell != null) {
            if (cell.isTrap()) {
                soundManager.playSFX("resources/trap.wav");
                logMsg.append(" -> 👮 RAID! Back to HQ");
                p.setPosition(1);
            }
            else if (cell.getType() == Cell.CellType.SNAKE) {
                soundManager.playSFX("resources/snake.wav");
                logMsg.append(" -> 🚧 TRAFFIC! Detour to ").append(cell.getTargetCell());
                p.setPosition(cell.getTargetCell());
            }
            else if (cell.getType() == Cell.CellType.LADDER) {
                soundManager.playSFX("resources/ladder.wav");
                logMsg.append(" -> 🛣️ HIGHWAY! Fast to ").append(cell.getTargetCell());
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
            log("⛽ Gas Station! Free Turn.");
            rollDiceButton.setEnabled(true);
            updateTurnLabel();
        }
        else {
            nextTurn();
        }
    }

    private void showLeaderboard(Player winner) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎉 SHIFT COMPLETE: ").append(winner.getName()).append(" 🎉\n");
        sb.append("Earnings: $").append(winner.getScore()).append("\n\n");
        sb.append("=== 🏆 LEADERBOARD 🏆 ===\n");

        PriorityQueue<PlayerStat> tempQueue = new PriorityQueue<>(leaderboard);
        int rank = 1;
        while (!tempQueue.isEmpty()) {
            PlayerStat stat = tempQueue.poll();
            sb.append("#").append(rank).append(" ").append(stat.toString()).append("\n");
            rank++;
        }
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(sb.toString())), "Shift Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        updateTurnLabel();
        rollDiceButton.setEnabled(true);
    }

    /**
     * Perbaikan: Menghilangkan teks "TURN" yang menabrak garis merah UI.
     */
    private void updateTurnLabel() {
        Player p = players.get(currentPlayerIndex);
        statusLabel.setText("CURRENT DRIVER: " + p.getName().toUpperCase());
        statusLabel.setBackground(p.getColor());
        statusLabel.setForeground(Color.WHITE);

        // Menggunakan LineBorder polos tanpa teks judul agar UI bersih
        diceLabel.setBorder(BorderFactory.createLineBorder(p.getColor(), 3));

        log(">>> Shift: " + p.getName());
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

    private void showDemoPath() {
        dijkstra.setSafeMode(safeModeCheck.isSelected());
        List<Integer> path = dijkstra.findShortestPath(1, BOARD_SIZE * BOARD_SIZE);
        log("GPS Route... " + path);
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

        playModeButton = new JButton("🚀 Start Shift");
        playModeButton.setBackground(new Color(46, 204, 113));
        playModeButton.setForeground(Color.WHITE);
        playModeButton.addActionListener(e -> startGame());

        rollDiceButton = new JButton("🎲 Drive");
        rollDiceButton.setBackground(new Color(241, 196, 15));
        rollDiceButton.addActionListener(e -> rollDice());

        safeModeCheck = new JCheckBox("Premium GPS");
        safeModeCheck.setBackground(new Color(50,50,60));
        safeModeCheck.setForeground(Color.WHITE);

        findPathButton = new JButton("📡 GPS Test");
        findPathButton.addActionListener(e -> showDemoPath());

        resetButton = new JButton("🔄 Reset");
        resetButton.addActionListener(e -> resetGame());

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

        statusLabel = new JLabel("Waiting...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        diceLabel = new JLabel("⛽ Engine: OFF");
        diceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        diceLabel.setPreferredSize(new Dimension(280, 100));
        diceLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        resultArea = new JTextArea(15, 20);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        JPanel topInfo = new JPanel(new BorderLayout(5,5));
        topInfo.add(statusLabel, BorderLayout.NORTH);
        topInfo.add(diceLabel, BorderLayout.CENTER);

        panel.add(topInfo, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

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
            int size = BOARD_SIZE * CELL_SIZE;
            setPreferredSize(new Dimension(size, size));
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
                        g2.setColor(new Color(p.getColor().getRed(), p.getColor().getGreen(), p.getColor().getBlue(), 100));
                        g2.fillOval(center.x - 22 + offset, center.y - 22 + offset, 44, 44);
                    }

                    g2.setColor(p.getColor());
                    g2.fillOval(center.x - 15 + offset, center.y - 15 + offset, 30, 30);
                    g2.setColor(Color.WHITE);
                    g2.drawOval(center.x - 15 + offset, center.y - 15 + offset, 30, 30);

                    g2.drawString(p.getName().substring(0, 1), center.x - 3 + offset, center.y + 4 + offset);
                }
            }
        }
    }
}