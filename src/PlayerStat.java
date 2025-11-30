public class PlayerStat implements Comparable<PlayerStat> {
    private String name;
    private int score;
    private int totalSteps;

    public PlayerStat(String name, int score, int totalSteps) {
        this.name = name;
        this.score = score;
        this.totalSteps = totalSteps;
    }

    // Override compareTo untuk sorting otomatis di PriorityQueue
    @Override
    public int compareTo(PlayerStat other) {
        // 1. Prioritas Utama: SKOR TERBESAR (Descending)
        if (this.score != other.score) {
            return other.score - this.score;
        }
        // 2. Prioritas Kedua: LANGKAH TERDIKIT (Ascending)
        // Jika skor sama, yang langkahnya lebih sedikit yang menang
        return this.totalSteps - other.totalSteps;
    }

    @Override
    public String toString() {
        return String.format("%-10s | Score: %-5d | Steps: %d", name, score, totalSteps);
    }
}