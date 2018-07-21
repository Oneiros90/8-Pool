package pool;

import phisic.Sphere2D;
import media.support.*;

/**
 * La classe <code>PocketedBalls</code> gestisce l'entrata in buca di una palla da biliardo e la rappresentazione
 * dell'interno del tavolo da biliardo.
 * @author Oneiros
 */
public class Chamber extends java.util.ArrayList<PoolBall> {

    /** L'immagine del bordo */
    protected Image border;
    /** L'immagine dello sfondo */
    protected Image background;
    private static final int BORDER = 18;
    private static final int END = BORDER + 15 * (Sphere2D.BALL_SIZE) + 240;
    private static final double direction = Math.PI;
    private int priority;

    private class InPocketMovingThread extends Thread {

        private int ticket;
        private PoolBall ball;

        public InPocketMovingThread(PoolBall pBall) {
            this.ball = pBall;
        }

        @Override
        public void run() {
            this.ticket = size();
            try {
                int spawn = END + (ticket - 1) * Sphere2D.BALL_SIZE;
                ball.setLocation(spawn, background.getHeight() / 2);
                while (this.ticket != priority) {
                    sleep(1);
                }
                ball.image.setVisible(true);
                ball.speed.setPolarChoords(2, Chamber.direction);
                while (ball.location.x >= spawn - Sphere2D.BALL_SIZE) {
                    ball.move(0);
                    sleep(9);
                }
                priority++;
                while (ball.location.x > BORDER - 15 + Sphere2D.BALL_SIZE * ticket) {
                    ball.move(0);
                    sleep(9);
                }
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
            ball.speed.setMagnitude(0);
        }
    };

    /**
     * Crea ed inizializza i pannelli per la visualizzazione dell'interno del tavolo.
     */
    public Chamber() {
        this.border = new Image("insideTableBorder.png");
        this.border.setVisible(true);
        this.background = new Image("insideTableBackground.png");
        this.background.setSize(this.border.getWidth(), this.border.getHeight() - BORDER);
        this.priority = 1;
    }

    /**
     * Sposta i pannelli per la visualizzazione dell'interno del tavolo.
     * @param pX La coordinata <code>x</code> dell'angolo in alto a sinistra del pannello
     * @param pY La coordinata <code>y</code> dell'angolo in alto a sinistra del pannello
     */
    public void setLocation(int pX, int pY) {
        this.background.setLocation(pX, pY);
        this.border.setLocation(pX, pY - BORDER);
    }

    /**
     * Aggiunge la palla da biliardo <code>pBall</code> all'elenco delle palle imbucate gestendone l'animazione
     * ed il suono.
     * @param pBall La palla entrata in buca
     * @return <code>true</code> se la sfera è stata aggiunta correttamente alla lista;<br>
     *         <code>false</code> altrimenti.
     */
    @Override
    public boolean add(final PoolBall pBall) {
        new Sound("ball_roll.wav").run();
        if (pBall.number == 0) {
            return false;
        }
        this.background.add(pBall.image);
        super.add(pBall);
        new InPocketMovingThread(pBall).start();
        return true;
    }

    /**
     * Staibilisce se la palla da biliardo <code>pBall</code> è stata imbucata.
     * @param pBall La palla da biliardo
     * @return <code>true</code> se la sfera è stata imbucata;<br>
     *         <code>false</code> altrimenti.
     */
    public boolean contains(PoolBall pBall) {
        return super.contains(pBall);
    }
}
