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

    public List<Integer> findShortestPath(int start, int end) {
        int max = board.getBoardSize() * board.getBoardSize();
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        for (int i = 1; i <= max; i++) dist.put(i, Integer.MAX_VALUE);
        dist.put(start, 0);
        pq.add(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            int u = current.id;

            if (u == end) break;

            for (int dice = 1; dice <= 6; dice++) {
                int v = u + dice;
                if (v > max) continue;

                Cell cellV = board.getCellByNumber(v);
                int finalDest = v;
                int weight = 1;

                if (cellV != null) {
                    // Logic Bobot Graph (GPS Logic)
                    if (cellV.getType() == Cell.CellType.LADDER) {
                        finalDest = cellV.getTargetCell();
                        weight = 0; // Jalan Tol = Cepat
                    }
                    else if (cellV.getType() == Cell.CellType.SNAKE) {
                        finalDest = cellV.getTargetCell();
                        weight = safeMode ? 100 : 1; // Macet = Hindari di Safe Mode
                    }
                    else if (cellV.isTrap()) {
                        finalDest = 1;
                        weight = safeMode ? 500 : 5; // Razia Polisi = Sangat Dihindari
                    }
                }

                if (dist.get(u) != Integer.MAX_VALUE && dist.get(u) + weight < dist.get(finalDest)) {
                    dist.put(finalDest, dist.get(u) + weight);
                    prev.put(finalDest, u);
                    pq.add(new Node(finalDest, dist.get(finalDest)));
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        Integer curr = end;

        if (!prev.containsKey(end) && start != end) return path;

        while (curr != null) {
            path.add(0, curr);
            curr = prev.get(curr);
        }

        // Bunyi notifikasi jika GPS menemukan jalan
        if (!path.isEmpty() && path.size() > 1) {
            if (soundManager != null) {
                soundManager.playSFX("resources/star.wav"); // Suara "GPS Ready"
            }
        }

        return path;
    }

    private static class Node {
        int id, cost;
        Node(int id, int cost) { this.id = id; this.cost = cost; }
    }
}