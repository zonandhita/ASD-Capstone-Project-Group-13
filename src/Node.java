import java.awt.*;

public class Node {
    private int id;
    private double x, y;
    private static final int RADIUS = 25;

    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getRadius() {
        return RADIUS;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }


    public void draw(Graphics2D g2) {
        // Render bodi utama titik dengan warna biru cerah
        g2.setColor(new Color(100, 150, 255));
        g2.fillOval((int)(x - RADIUS), (int)(y - RADIUS), RADIUS * 2, RADIUS * 2);

        // Menambahkan garis tepi agar bentuk node terlihat lebih tegas
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval((int)(x - RADIUS), (int)(y - RADIUS), RADIUS * 2, RADIUS * 2);

        // Menampilkan teks ID tepat di pusat koordinat lingkaran
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        String label = String.valueOf(id);
        int labelWidth = fm.stringWidth(label);
        int labelHeight = fm.getHeight();

        // Kalkulasi posisi teks agar benar-benar berada di tengah (centering)
        g2.drawString(label, (int)(x - labelWidth / 2), (int)(y + labelHeight / 4));
    }
}