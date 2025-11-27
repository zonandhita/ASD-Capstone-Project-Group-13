import java.util.*;

public class DijkstraAlgorithm {
    private GameBoard board;
    private Map<Integer, Integer> distance;
    private Map<Integer, Integer> previous;
    private Set<Integer> visited;
    private List<Integer> shortestPath;

    public DijkstraAlgorithm(GameBoard board) {
        this.board = board;
        this.distance = new HashMap<>();
        this.previous = new HashMap<>();
        this.visited = new HashSet<>();
        this.shortestPath = new ArrayList<>();
    }

    public List<Integer> findShortestPath(int start, int end) {
        // Initialize
        distance.clear();
        previous.clear();
        visited.clear();
        shortestPath.clear();

        int maxCell = board.getBoardSize() * board.getBoardSize();

        // Set semua jarak ke infinity, kecuali start
        for (int i = 1; i <= maxCell; i++) {
            distance.put(i, Integer.MAX_VALUE);
        }
        distance.put(start, 0);

        // Priority Queue untuk memilih node dengan jarak terkecil
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>();
        pq.offer(new NodeDistance(start, 0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            int currentCell = current.node;

            if (visited.contains(currentCell)) continue;
            visited.add(currentCell);

            // Mark cell as visited untuk visualisasi
            Cell cell = board.getCellByNumber(currentCell);
            if (cell != null) {
                cell.setVisited(true);
            }

            // Jika sudah sampai tujuan
            if (currentCell == end) break;

            // Eksplorasi tetangga
            List<Integer> neighbors = board.getNeighbors(currentCell);
            for (int neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    int newDist = distance.get(currentCell) + 1; // Setiap langkah cost = 1

                    if (newDist < distance.get(neighbor)) {
                        distance.put(neighbor, newDist);
                        previous.put(neighbor, currentCell);
                        pq.offer(new NodeDistance(neighbor, newDist));
                    }
                }
            }
        }

        // Reconstruct path
        if (previous.containsKey(end)) {
            int current = end;
            while (current != start) {
                shortestPath.add(0, current);
                current = previous.get(current);
            }
            shortestPath.add(0, start);

            // Mark path cells
            for (int cellNum : shortestPath) {
                Cell cell = board.getCellByNumber(cellNum);
                if (cell != null) {
                    cell.setPath(true);
                }
            }
        }

        return shortestPath;
    }

    public int getDistance(int cell) {
        return distance.getOrDefault(cell, Integer.MAX_VALUE);
    }

    public List<Integer> getShortestPath() {
        return shortestPath;
    }

    public Set<Integer> getVisitedCells() {
        return visited;
    }

    // Helper class untuk Priority Queue
    private static class NodeDistance implements Comparable<NodeDistance> {
        int node;
        int distance;

        NodeDistance(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Integer.compare(this.distance, other.distance);
        }
    }
}