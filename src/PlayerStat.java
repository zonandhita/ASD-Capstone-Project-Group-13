public class PlayerStat implements Comparable<PlayerStat> {
    private String name;
    private int score;      // Earnings
    private int totalSteps; // Fuel

    public PlayerStat(String name, int score, int totalSteps) {
        this.name = name;
        this.score = score;
        this.totalSteps = totalSteps;
    }

    // Sorting Logic:
    // 1. Uang terbanyak menang
    // 2. Jika uang sama, Bensin (langkah) teredikit menang
    @Override
    public int compareTo(PlayerStat other) {
        if (this.score != other.score) {
            return other.score - this.score; // Descending (High Score first)
        }
        return this.totalSteps - other.totalSteps; // Ascending (Low Steps first)
    }

    // ✅ UPDATE: Format Text agar sesuai tema Kurir
    @Override
    public String toString() {
        return String.format("%-12s | Earnings: $% -4d | Fuel Used: %d L", name, score, totalSteps);
    }
    // test commit
}