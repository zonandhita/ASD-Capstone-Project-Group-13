public class PlayerStat implements Comparable<PlayerStat> {
    private String name;
    private int score;
    private int totalSteps;

    public PlayerStat(String name, int score, int totalSteps) {
        this.name = name;
        this.score = score;
        this.totalSteps = totalSteps;
    }

    /**
     * Menentukan kriteria pemenang dalam leaderboard City Courier.
     * Prioritas utama adalah total pendapatan, kemudian efisiensi penggunaan bahan bakar.
     */
    @Override
    public int compareTo(PlayerStat other) {
        // Urutan pertama: Kurir dengan total pendapatan (Earnings) tertinggi
        if (this.score != other.score) {
            return other.score - this.score;
        }
        // Urutan kedua: Jika pendapatan sama, kurir dengan bensin (langkah) paling sedikit yang unggul
        return this.totalSteps - other.totalSteps;
    }

    /**
     * Representasi teks untuk ditampilkan pada tabel skor akhir.
     */
    @Override
    public String toString() {
        return String.format("%-12s | Earnings: $% -4d | Fuel Used: %d L", name, score, totalSteps);
    }
}