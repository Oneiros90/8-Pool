package media.support;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

/**
 * La classe <code>Sound</code> permette di gestire in modo facile e veloce dei suoni in formato Wav. I suoni
 * devono essere posizionati nella sottocartella <code>src</code>del progetto.
 * @author Oneiros
 */
public class Sound implements Runnable {

    /** La sottocartella interna ad <code>src</code> dove sono posizionati tutti i suoni */
    public static String imagesDirectory = "/media/sounds/";
    /** Toggle che permette di abilitare/disabilitare la riproduzione di tutti i suoni gestiti da questa classe */
    public static boolean enabled = true;
    private Clip sound;
    private String soundName;

    /**
     * Crea ed inizializza un Sound a partire dal nome di un file .wav contenuto nella sottocartella
     * <code>src</code>del progetto.
     * @param pName Il nome del file audio (estensione compresa)
     */
    public Sound(String pName) {
        this.soundName = pName;
        if (Sound.imagesDirectory.charAt(0) == '/') {
            Sound.imagesDirectory = Sound.imagesDirectory.substring(1);
        }
        try {
            URL soundURL = getClass().getClassLoader().getResource(Sound.imagesDirectory + pName);
            Line.Info lInfo = new Line.Info(Clip.class);
            Line line = AudioSystem.getLine(lInfo);
            this.sound = (Clip) line;
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL);
            this.sound.open(ais);
        } catch (LineUnavailableException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        } catch (UnsupportedAudioFileException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Crea ed inizializza un Sound a partire dal nome di un file .wav contenuto nella sottocartella
     * <code>src</code>del progetto.
     * @param pName Il nome del file audio (estensione compresa)
     * @param pVolume Il volume iniziale del file audio (default: <code>80%</code>
     */
    public Sound(String pName, float pVolume) {
        this(pName);
        this.setVolume(pVolume);
    }

    /**
     * Avvia la riproduzione del suono.
     */
    public void run() {
        if (Sound.enabled) {
            this.sound.setFramePosition(0);
            this.sound.loop(0);
            this.newMusicStopper();
        }
    }

    /**
     * Esegue la riproduzione del suono <code>n</code> volte.
     * @param n Il numero di riproduzioni del suono (Inserire un valore negativo per ottenere una
     * riproduzione continua)
     */
    public void loop(int n) {
        if (Sound.enabled) {
            this.sound.setFramePosition(0);
            this.sound.loop(n);
            this.newMusicStopper();
        }
    }

    private void newMusicStopper() {
        new Thread("Music Stopper") {

            @Override
            public void run() {
                while (sound.isActive()) {
                    while (enabled && sound.isActive()) {
                        this.sleep();
                    }
                    float volume = getVolume();
                    setVolume(0);
                    while (!enabled && sound.isActive()) {
                        this.sleep();
                    }
                    setVolume(volume);
                }
            }

            private void sleep() {
                try {
                    sleep(1);
                } catch (InterruptedException ex) {
                }
            }
        }.start();
    }

    /**
     * Ferma la riproduzione del suono.
     */
    public void stop() {
        this.sound.stop();
    }

    /**
     * Regola il volume del suono.
     * @param pVolume Percentuale di volume del suono (default: <code>80%</code>)
     */
    public final void setVolume(float pVolume) {
        if (pVolume > 100) {
            throw new IllegalArgumentException(
                    "Requested value " + pVolume + " exceeds allowable maximum value " + 100 + ".");
        } else if (pVolume < 0) {
            throw new IllegalArgumentException(
                    "Requested value " + pVolume + " smaller than allowable minimum value " + 0 + ".");
        }
        if (pVolume >= 80) {
            pVolume = (float) (pVolume * (6.0206 / 80) - 6.0206);
        } else {
            pVolume = (float) (pVolume - 80.0);
        }
        FloatControl gainControl = (FloatControl) this.sound.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(pVolume);
    }

    /**
     * Restituisce il volume attuale in percentuale.
     * @return Il volume attuale in percentuale (default: <code>80%</code>)
     */
    public float getVolume() {
        FloatControl gainControl = (FloatControl) this.sound.getControl(FloatControl.Type.MASTER_GAIN);
        double volume = gainControl.getValue();
        if (volume >= 80) {
            return (float) ((volume + 6.0206) * (80 / 6.0206));
        } else {
            return (float) (volume + 80.0);
        }
    }

    /**
     * Restituisce la lunghezza in millisecondi del suono.
     * @return La lunghezza in millisecondi del suono
     */
    public long length() {
        return this.sound.getMicrosecondLength();
    }

    /**
     * Determina se il suono è attualmente in riproduzione o meno
     * @return <code>true</code> se il suono è attualmente in riproduzione;<br>
     *         <code>false</code> altrimenti.
     */
    public boolean isRunning() {
        return this.sound.isRunning();
    }

    /**
     * Restituisce il nome del file audio
     * @return Il nome del file audio (estensione compresa).
     */
    public String getSoundName() {
        return this.soundName;
    }

    /**
     * Restituisce il path completo del file audio.
     * @return Il path completo del file audio
     */
    public String getSoundFullPath() {
        return Sound.imagesDirectory + this.soundName;
    }

    /**
     * Restituisce una stringa contenente il path completo del file audio.
     * @return Una stringa contenente il path completo del file audio
     */
    @Override
    public String toString() {
        return "Sound[" + this.getSoundFullPath() + "]";
    }
}
