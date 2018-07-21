package pool;

import media.support.*;
import phisic.*;

/**
 * La classe <code>PoolBall</code> modella una palla da biliardo gestendone comportamenti fisici e rappresentazione
 * visiva e sonora.
 * @author Oneiros
 */
public class PoolBall extends Sphere2D {

    /** Il numero identificativo della palla da biliardo */
    protected int number;
    /** L'immagine della palla da biliardo */
    protected Image image;
    private final Sound hardHitSound = new Sound("ball_hit_hard.wav");
    private final Sound mediumHitSound = new Sound("ball_hit_medium.wav");
    private final Sound weakHitSound = new Sound("ball_hit_weak.wav");

    /** Crea ed inizializza una palla da biliardo in posizione <code>pLocation</code>.
     * @param pLocation La posizione del centro della palla da biliardo.
     * @param pNumber Il numero identificativo della palla da biliardo
     */
    public PoolBall(Point pLocation, int pNumber) {
        super(pLocation);
        this.number = pNumber;
        this.image = new Image("ball" + this.number + ".png");
        this.image.setOpaque(false);
        this.image.setVisible(true);
        this.setPanelLocation();
    }

    /**
     * Crea ed inizializza una palla da biliardo in posizione <code>(pX,pY)</code>.
     * @param pX La coordinata <code>x</code> del centro della palla da biliardo
     * @param pY La coordinata <code>y</code> del centro della palla da biliardo
     * @param pNumber Il numero identificativo della palla da biliardo
     */
    public PoolBall(double pX, double pY, int pNumber) {
        this(new Point(pX, pY), pNumber);
    }

    private void setPanelLocation() {
        this.image.setLocation((int) this.location.x - (BALL_SIZE / 2), (int) this.location.y - (BALL_SIZE / 2));
    }

    @Override
    public void move(double pFriction) {
        super.move(pFriction);
        this.setPanelLocation();
    }

    /**
     * Imposta la posizione della palla da biliardo all'interno del suo sistema di riferimento.
     * @param pX La coordinata <code>x</code> del nuovo centro della palla da biliardo
     * @param pY La coordinata <code>y</code> del nuovo centro della palla da biliardo
     */
    public void setLocation(double pX, double pY) {
        this.location.setLocation(pX, pY);
        this.setPanelLocation();
    }

    /**
     * Imposta la posizione della palla da biliardo all'interno del suo sistema di riferimento.
     * @param pPoint Il nuovo centro della palla da biliardo
     */
    public void setLocation(Point pPoint) {
        this.setLocation(pPoint.x, pPoint.y);
    }

    /**
     * Gestisce l'urto tra la sfera d'istanza e la sfera <code>pBall</code>. Le nuove velocità e le nuove
     * direzioni vengono determinate scambiando le proiezioni dei vettori velocità sulla retta tangente
     * alle due sfere. Il metodo si occupa anche di distanziare opportunamente le due sfere per evitare casi di
     * autocompenetrazione.
     * Durante la collisione viene generato un suono d'impatto dipendente dall'entità dello stesso.
     * @param pBall La sfera con la quale è avvenuta la collisione
     */
    @Override
    public void collideWith(Sphere2D pBall) {
        double hitStrenght = pBall.speed.getMagnitude();
        super.collideWith(pBall);
        hitStrenght = Math.abs(hitStrenght - this.speed.getMagnitude());
        if (!this.hardHitSound.isRunning() && !this.mediumHitSound.isRunning() && !this.weakHitSound.isRunning()) {
            if (hitStrenght >= 0 && hitStrenght <= 1) {
                this.weakHitSound.setVolume(50 + 25 * (float) hitStrenght);
                this.weakHitSound.run();
            } else if (hitStrenght > 1 && hitStrenght <= 4) {
                this.mediumHitSound.run();
            } else {
                this.hardHitSound.run();
            }
        }
    }

    /**
     * Gestisce l'urto tra la sfera ed un muro calcolando la nuova direzione di spostamento e genrando un
     * suono d'impatto.
     * @param pWall Il muro sul quale la sfera è entrata in collisione
     */
    @Override
    public void collideWith(Wall pWall) {
        super.collideWith(pWall);
        Sound edgeImpact = new Sound("cushion.wav");
        edgeImpact.setVolume(60 + 15 * (float) (this.speed.getMagnitude()) / 10);
        edgeImpact.run();
    }

    /**
     * Restituisce la posizione del centro della palla da biliardo.
     * @return La posizione del centro della palla da biliardo
     */
    public Point getLocation() {
        return new Point((int) this.location.x, (int) this.location.y);
    }

    /**
     * Restituisce il numero identificato della palla da biliardo.
     * @return Il numero identificato della palla da biliardo
     */
    public int getNumber() {
        return this.number;
    }
}
