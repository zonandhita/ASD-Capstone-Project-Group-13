import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    private Clip backgroundClip;
    private FloatControl volumeControl;

    // --- MUSIC BACKGROUND (LOOPING) ---
    public void playBackgroundMusic(String filePath) {
        try {
            if (backgroundClip != null && backgroundClip.isRunning()) return;

            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.out.println("❌ File Music tidak ketemu: " + filePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);

            // Set Volume Background agak kecil (misal -10 decibel) biar ga berisik
            setVolume(0.6f);

            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }

    public void setVolume(float volume) {
        if (backgroundClip != null && backgroundClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = gainControl.getMaximum() - gainControl.getMinimum();
            float gain = (range * volume) + gainControl.getMinimum();
            gainControl.setValue(gain);
        }
    }

    // --- SOUND EFFECT (SEKALI MAIN) ---
    // Method baru ini untuk SFX (Snake, Ladder, Win, dll)
    public void playSFX(String filePath) {
        new Thread(() -> { // Pakai Thread biar game tidak lag saat load suara
            try {
                File audioFile = new File(filePath);
                if (!audioFile.exists()) {
                    System.out.println("❌ File SFX tidak ketemu: " + filePath);
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);

                // Volume SFX Full (1.0f) biar terdengar jelas di atas lagu
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue(1.0f);
                }

                clip.start();

                // Hapus memori clip setelah selesai main
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

            } catch (Exception e) {
                System.err.println("Error playing SFX: " + e.getMessage());
            }
        }).start();
    }
}