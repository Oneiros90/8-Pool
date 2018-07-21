package pool;

import media.support.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * La classe <code>Cue</code> modella una stecca da biliardo gestendone movimenti e rappresentazione visiva e sonora.
 * @author Oneiros
 */
public class Cue extends Image {

    /** La massima distanza consentita tra la punta della stecca e la palla */
    public static final int MAX_DISTANCE_FROM_BALL = 180;
    private int distanceFromBall;
    private PoolBall whiteBall;
    private int offsetX, offsetY;
    private double angle;
    private AffineTransform affineTransform;
    private final Sound hitSound = new Sound("cue_hit.wav");

    /**
     * L'enum Color elenca i possibili colori delle stecche,
     */
    protected enum Color {

        /** Stecca gialla */
        YELLOW("Yellow"),
        /** Stecca blu */
        BLUE("Blue"),
        /** Stecca rossa */
        RED("Red"),
        /** Stecca viola */
        PURPLE("Purple"),
        /** Stecca arancione */
        ORANGE("Orange"),
        /** Stecca verde */
        GREEN("Green"),
        /** Stecca marrone */
        BROWN("Brown");
        private String color;

        private Color(String pColor) {
            this.color = pColor;
        }

        /**
         * Restituisce il nome del file immagine della stecca corrispondente al colore.
         * @return Il nome del file immagine della stecca corrispondente al colore
         */
        protected String getFileName() {
            return "cue" + this.color + ".png";
        }

        /**
         * Restituisce il colore (<code>java.awt.Color</code>) della stecca.
         * @return Il colore della stecca
         */
        protected java.awt.Color getColor() {
            switch (this) {
                case YELLOW:
                    return java.awt.Color.YELLOW;
                case BLUE:
                    return java.awt.Color.BLUE;
                case RED:
                    return java.awt.Color.RED;
                case PURPLE:
                    return java.awt.Color.MAGENTA;
                case ORANGE:
                    return java.awt.Color.ORANGE;
                case GREEN:
                    return java.awt.Color.GREEN;
                default:
                    return java.awt.Color.getHSBColor((float) 0.08, 1, (float) 0.6);
            }
        }
    }

    /**
     * Crea ed inizializza la stecca da biliardo
     * @param pColor Il colore della stecca
     * @param pBall La palla da biliardo intorno alla quale deve ruotare la stecca
     * @param pOffsetX Un offset sulla coordinata <code>x</code> della posizione del pannello della stecca
     * @param pOffsetY Un offset sulla coordinata <code>y</code> della posizione del pannello della stecca
     */
    public Cue(Color pColor, PoolBall pBall, int pOffsetX, int pOffsetY) {
        super(pColor.getFileName());
        this.setSize(2 * (this.getWidth() + MAX_DISTANCE_FROM_BALL), 2 * (this.getWidth() + MAX_DISTANCE_FROM_BALL));
        this.setVisible(true);
        this.setOpaque(false);
        this.angle = Math.PI;
        this.whiteBall = pBall;
        this.offsetX = pOffsetX;
        this.offsetY = pOffsetY;
        this.prepare();
        this.affineTransform = new AffineTransform();
    }

    /**
     * Imposta il colore della stecca
     * @param pColor Il colore della stecca
     */
    public void setColor(Color pColor) {
        super.setImage(pColor.getFileName());
    }

    /**
     * Prepara la stecca per un lancio posizionandola affianco alla palla da colpire
     */
    public final void prepare() {
        int x = (int) this.whiteBall.getLocation().x + this.offsetX - this.getWidth() / 2;
        int y = (int) this.whiteBall.getLocation().y + this.offsetY - this.getWidth() / 2;
        this.setLocation(x, y);
        this.distanceFromBall = 30;
    }

    /**
     * Ruota la stecca di un angolo d'ampiezza <code>pAngle</code>.
     * @param pAngle L'angolo di rotazione della stecca
     */
    public void rotate(double pAngle) {
        this.angle = pAngle;
        this.repaint();
    }

    /**
     * Imposta la distanza tra la punta della stecca ed il centro della palla da colpire.
     * @param pDist La distanza tra la punta della stecca e la palla da colpire
     */
    public void setDistanceFromBall(int pDist) {
        if (pDist > MAX_DISTANCE_FROM_BALL) {
            throw new IllegalArgumentException("Requested value " + pDist
                    + " exceeds allowable maximum value " + MAX_DISTANCE_FROM_BALL + ".");
        }
        this.distanceFromBall = pDist;
    }

    /**
     * Restituisce la distanza attuale tra la punta della stecca ed il centro della palla da colpire.
     * @return La distanza tra la punta della stecca e la palla da colpire
     */
    public int getDistanceFromBall() {
        return this.distanceFromBall;
    }

    /**
     * Genera il suono di collisione tra stecca e palla
     */
    public void hitSound() {
        if (!this.hitSound.isRunning()) {
            this.hitSound.run();
        }
    }

    /**
     * Disegna la stecca.
     * @param g L'oggetto <code>Graphics</code> da disegnare
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int aux1 = this.getWidth() / 2;
        int aux2 = this.getImageSize().height / 2;
        this.affineTransform.setToTranslation(aux1 + this.distanceFromBall, aux1 - aux2);
        this.affineTransform.rotate(this.angle, -this.distanceFromBall, aux2);
        g2d.drawImage(this.getImage(), this.affineTransform, this);
    }
}
