import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    private List<Node> nodes;
    private List<Edge> edges;
    private int[][] adjacencyMatrix;

    public Graph(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        initializeGraph();
    }


    private void initializeGraph() {
        int n = adjacencyMatrix.length;

        // Konfigurasi koordinat pusat dan radius untuk tata letak node
        int centerX = 400;
        int centerY = 300;
        int radius = 200;

        for (int i = 0; i < n; i++) {
            // Kalkulasi posisi X dan Y setiap node agar membentuk lingkaran sempurna
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            nodes.add(new Node(i, x, y));
        }

        // Iterasi matriks untuk membangun relasi antar titik (Edge)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // Jika nilai pada matriks bukan 0, maka terdapat rute penghubung (bobot)
                if (adjacencyMatrix[i][j] != 0) {
                    edges.add(new Edge(nodes.get(i), nodes.get(j), adjacencyMatrix[i][j]));
                }
            }
        }
    }


    public void draw(Graphics2D g2) {
        // Render seluruh garis penghubung terlebih dahulu
        for (Edge edge : edges) {
            edge.draw(g2);
        }

        // Render titik node di lapisan paling atas agar tidak tertutup garis
        for (Node node : nodes) {
            node.draw(g2);
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}