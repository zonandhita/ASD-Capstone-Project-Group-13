import javax.swing.*;
import java.awt.*;

public class GraphVisualizer extends JFrame {
    private GraphPanel graphPanel;
    private JTextArea matrixInput;

    public GraphVisualizer() {
        // Inisialisasi jendela utama aplikasi visualisasi matriks
        setTitle("Graph Visualizer - Adjacency Matrix");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Setup awal menggunakan matriks tetangga contoh (4x4)
        int[][] exampleMatrix = {
                {0, 1, 1, 0},
                {0, 0, 1, 1},
                {0, 0, 0, 1},
                {1, 0, 0, 0}
        };

        Graph graph = new Graph(exampleMatrix);
        graphPanel = new GraphPanel(graph);

        // Membangun panel input untuk interaksi pengguna
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel instructionLabel = new JLabel("Enter adjacency matrix (space-separated, one row per line):");
        matrixInput = new JTextArea(5, 30);
        matrixInput.setText("0 1 1 0\n0 0 1 1\n0 0 0 1\n1 0 0 0");
        matrixInput.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JButton visualizeButton = new JButton("Visualize Graph");
        visualizeButton.addActionListener(e -> visualizeGraph());

        inputPanel.add(instructionLabel, BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(matrixInput), BorderLayout.CENTER);
        inputPanel.add(visualizeButton, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.NORTH);
        add(graphPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Memproses teks input dari JTextArea dan mengonversinya menjadi objek Graph.
     * Melakukan validasi untuk memastikan input berbentuk matriks persegi.
     */
    private void visualizeGraph() {
        try {
            // Parsing teks mentah berdasarkan baris dan spasi
            String[] lines = matrixInput.getText().trim().split("\n");
            int n = lines.length;
            int[][] matrix = new int[n][n];

            for (int i = 0; i < n; i++) {
                String[] values = lines[i].trim().split("\\s+");

                // Pastikan jumlah kolom sama dengan jumlah baris (Square Matrix)
                if (values.length != n) {
                    throw new IllegalArgumentException("Matrix must be square");
                }
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = Integer.parseInt(values[j]);
                }
            }

            // Memperbarui tampilan panel graf dengan data matriks baru
            Graph newGraph = new Graph(matrix);
            graphPanel.setGraph(newGraph);

        } catch (Exception ex) {
            // Penanganan error format input agar aplikasi tidak crash
            JOptionPane.showMessageDialog(this,
                    "Invalid matrix format. Please enter a valid adjacency matrix.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphVisualizer visualizer = new GraphVisualizer();
            visualizer.setVisible(true);
        });
    }
}