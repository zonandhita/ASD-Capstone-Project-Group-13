import java.util.*;

public class DijkstraAlgorithm {
    private GameBoard board;
    private SoundManager soundManager;
    private boolean safeMode = false;

    public DijkstraAlgorithm(GameBoard board, SoundManager soundManager) {
        this.board = board;
        this.soundManager = soundManager;
    }

    public void setSafeMode(boolean safe) {
        this.safeMode = safe;
    }

    /**
     * Implementasi Dijkstra untuk menentukan rute pengiriman paket tercepat.
     * Mengkalkulasi bobot berdasarkan kondisi jalan (Highway, Macet, atau Razia).
     */
    public List<Integer> findShortestPath(int start, int end) {
        int max = board.getBoardSize() * board.getBoardSize();
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        // Inisialisasi jarak awal ke semua titik alamat
        for (int i = 1; i <= max; i++) dist.put(i, Integer.MAX_VALUE);
        dist.put(start, 0);
        pq.add(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            int u = current.id;

            if (u == end) break;

            // Simulasi kemungkinan langkah berdasarkan kocokan dadu (1-6)
            for (int dice = 1; dice <= 6; dice++) {
                int v = u + dice;
                if (v > max) continue;

                Cell cellV = board.getCellByNumber(v);
                int finalDest = v;
                int weight = 1;

                if (cellV != null) {
                    // Penyesuaian bobot graph berdasarkan tipe lokasi
                    if (cellV.getType() == Cell.CellType.LADDER) {
                        finalDest = cellV.getTargetCell();
                        weight = 0; // Highway dianggap tanpa hambatan
                    }
                    else if (cellV.getType() == Cell.CellType.SNAKE) {
                        finalDest = cellV.getTargetCell();
                        // Mode aman akan memberikan pinalti tinggi pada area macet
                        weight = safeMode ? 100 : 1;
                    }
                    else if (cellV.isTrap()) {
                        finalDest = 1;
                        // Penalti maksimal untuk menghindari area razia polisi
                        weight = safeMode ? 500 : 5;
                    }
                }

                // Relaksasi edge untuk menemukan rute dengan cost terkecil
                if (dist.get(u) != Integer.MAX_VALUE && dist.get(u) + weight < dist.get(finalDest)) {
                    dist.put(finalDest, dist.get(u) + weight);
                    prev.put(finalDest, u);
                    pq.add(new Node(finalDest, dist.get(finalDest)));
                }
            }
        }

        // Rekonstruksi rute dari titik tujuan kembali ke titik awal
        List<Integer> path = new ArrayList<>();
        Integer curr = end;

        if (!prev.containsKey(end) && start != end) return path;

        while (curr != null) {
            path.add(0, curr);
            curr = prev.get(curr);
        }

        // Feedback audio saat rute navigasi berhasil dikalkulasi
        if (!path.isEmpty() && path.size() > 1) {
            if (soundManager != null) {
                soundManager.playSFX("resources/star.wav");
            }
        }

        return path;
    }

    private static class Node {
        int id, cost;
        Node(int id, int cost) { this.id = id; this.cost = cost; }
    }
}