package pool;

import phisic.*;
import media.support.Image;

/**
 * La classe <code>PoolTable</code> modella un tavolo da biliardo. <br>
 * La classe si occupa di posizionare le palle da biliardo sul tavolo, di coordinare i loro movimenti e
 * di monitorare le collisioni tra di esse e con i bordi.
 * @author Oneiros
 */
public class PoolTable {

    /** L'attrito generato dalla superficie del tavolo */
    public static final double FRICTION = 0.007;
    /** Le dimensioni del tavolo */
    public static final java.awt.Dimension TABLE_SIZE = new java.awt.Dimension(800, 440);
    /** Le 16 palle da biliardo */
    protected PoolBall[] balls;
    /** L'immagine del tavolo */
    protected Image image;
    private PoolGame.PoolManager poolManager;
    private final Thread ballsMovesThread = new Thread("BallsMovesThread") {

        private int firstTouchedBall;

        @Override
        public void run() {
            boolean ballsAreMoving = true;
            this.firstTouchedBall = 0;
            while (ballsAreMoving) {
                ballsAreMoving = false;
                for (PoolBall ball : balls) {
                    if (ball.hasSpeed()) {
                        ballsAreMoving = true;
                        if (!poolManager.chamber.contains(ball)) {
                            ball.move(PoolTable.FRICTION);
                            if (!checkPocketEntering(ball)) {
                                checkBallsCollisions(ball);
                                checkEdgesCollisions(ball);
                            }
                        }
                    }
                }
                try {
                    sleep(10);
                } catch (InterruptedException ex) {
                }
            }
            poolManager.setNextTurn();
        }

        private void checkBallsCollisions(PoolBall pBall) {
            for (PoolBall secondBall : balls) {
                if (pBall != secondBall && pBall.isTouching(secondBall) && !poolManager.chamber.contains(secondBall)) {
                    if (pBall.getNumber() == 0 && this.firstTouchedBall == 0) {
                        this.firstTouchedBall = secondBall.getNumber();
                    }
                    pBall.collideWith(secondBall);
                }
            }
        }

        private void checkEdgesCollisions(PoolBall pBall) {
            for (int j = 0; j < Cushion.values().length; j++) {
                if (Cushion.values()[j].getWall().isTouching(pBall)) {
                    Wall closestWall = Cushion.values()[j].getWall();
                    for (int k = j + 1; k < Cushion.values().length; k++) {
                        Wall secondWall = Cushion.values()[k].getWall();
                        if (secondWall.getPenetration(pBall) > closestWall.getPenetration(pBall)) {
                            closestWall = secondWall;
                        }
                    }
                    pBall.collideWith(closestWall);
                    for (int e = 0; e < Cushion.values().length; e++) {
                        Cushion.values()[e].getWall().moveAway(pBall);
                    }
                    break;
                }
            }
        }

        private boolean checkPocketEntering(PoolBall pBall) {
            for (Pocket p : Pocket.values()) {
                if (pBall.getLocation().distance(p.getLocation()) <= Sphere2D.BALL_SIZE / 2) {
                    pBall.image.setVisible(false);
                    pBall.stop();
                    poolManager.pocket(pBall.getNumber(), this.firstTouchedBall, p.ordinal());
                    if (poolManager.chamber.add(pBall)) {
                        image.remove(pBall.image);
                    }
                    return true;
                }
            }
            return false;
        }
    };

    /**
     * L'enum <code>Cushion</code> elenca le sponde del tavolo definendone posizione e dimensioni.
     */
    protected enum Cushion {

        /** Il bordo in alto a sinistra. */
        N_A(369, 40, 71, 40),
        /** Il bordo in alto a destra. */
        N_B(729, 40, 431, 40),
        /** Il bordo a destra. */
        E(760, 369, 760, 71),
        /** Il bordo in basso a destra. */
        S_A(431, 400, 729, 400),
        /** Il bordo in basso a sinistra. */
        S_B(71, 400, 369, 400),
        /** Il bordo a sinistra. */
        W(40, 71, 40, 369),
        /** Il primo bordo della buca in alto a sinistra. */
        NW_1(20, 50, 40, 70),
        /** Il secondo bordo della buca in alto a sinistra. */
        NW_2(70, 40, 50, 20),
        /** Il primo bordo della buca in alto. */
        N_1(380, 20, 370, 40),
        /** Il secondo bordo della buca in alto. */
        N_2(430, 40, 420, 20),
        /** Il primo bordo della buca in alto a destra. */
        NE_1(750, 20, 730, 40),
        /** Il secondo bordo della buca in alto a destra. */
        NE_2(760, 70, 780, 50),
        /** Il primo bordo della buca in basso a destra. */
        SE_1(780, 390, 760, 370),
        /** Il secondo bordo della buca in basso a destra. */
        SE_2(730, 400, 750, 420),
        /** Il primo bordo della buca in basso. */
        S_1(420, 420, 430, 400),
        /** Il secondo bordo della buca in basso. */
        S_2(370, 400, 380, 420),
        /** Il primo bordo della buca in basso a sinistra. */
        SW_1(50, 420, 70, 400),
        /** Il secondo bordo della buca in basso a sinistra. */
        SW_2(40, 370, 20, 390);
        /** Le coordinata <code>x</code> del primo punto estremale. */
        protected int x1;
        /** Le coordinata <code>y</code> del primo punto estremale. */
        protected int y1;
        /** Le coordinata <code>x</code> del secondo punto estremale. */
        protected int x2;
        /** Le coordinata <code>y</code> del secondo punto estremale. */
        protected int y2;

        private Cushion(int pX1, int pY1, int pX2, int pY2) {
            this.x1 = pX1;
            this.y1 = pY1;
            this.x2 = pX2;
            this.y2 = pY2;
        }

        /**
         * Restituisce il <code>Wall</code> corrispondente al <code>Cushion</code>.
         * @return Il <code>Wall</code> corrispondente al <code>Cushion</code>
         */
        protected Wall getWall() {
            return new Wall(new Point(x1, y1), new Point(x2, y2));
        }
    }

    /**
     * L'enum <code>Pocket</code> elenca le buche presenti nel tavolo definendone la posizione.
     */
    protected enum Pocket {

        /** La buca in alto a sinistra */
        NW(35, 35),
        /** La buca in alto */
        N(400, 25),
        /** La buca in alto a destra */
        NE(765, 35),
        /** La buca in basso a destra */
        SE(765, 405),
        /** La buca in basso */
        S(400, 415),
        /** La buca in basso a sinistra */
        SW(35, 405);
        private int x, y;

        private Pocket(int pX, int pY) {
            this.x = pX;
            this.y = pY;
        }

        protected Point getLocation() {
            return new Point(this.x, this.y);
        }
    }

    /**
     * L'enum <code>Triangle</code> elenca le posizioni che le palle da biliardo devono assumere
     * all'inizio della partita
     */
    protected enum Triangle {

        /** La posizione della biglia numero 1 ad inizio partita. */
        BALL_1(1, 1),
        /** La posizione della biglia numero 2 ad inizio partita. */
        BALL_2(3, 1),
        /** La posizione della biglia numero 3 ad inizio partita. */
        BALL_3(3, 3),
        /** La posizione della biglia numero 4 ad inizio partita. */
        BALL_4(5, 1),
        /** La posizione della biglia numero 5 ad inizio partita. */
        BALL_5(5, 5),
        /** La posizione della biglia numero 6 ad inizio partita. */
        BALL_6(4, 2),
        /** La posizione della biglia numero 7 ad inizio partita. */
        BALL_7(4, 3),
        /** La posizione della biglia numero 8 ad inizio partita. */
        BALL_8(3, 2),
        /** La posizione della biglia numero 9 ad inizio partita. */
        BALL_9(5, 3),
        /** La posizione della biglia numero 10 ad inizio partita. */
        BALL_10(2, 2),
        /** La posizione della biglia numero 11 ad inizio partita. */
        BALL_11(4, 1),
        /** La posizione della biglia numero 12 ad inizio partita. */
        BALL_12(4, 4),
        /** La posizione della biglia numero 13 ad inizio partita. */
        BALL_13(5, 2),
        /** La posizione della biglia numero 14 ad inizio partita. */
        BALL_14(5, 4),
        /** La posizione della biglia numero 15 ad inizio partita. */
        BALL_15(2, 1);
        private int column;
        private int row;
        private final java.awt.Point trianglePosition;

        private Triangle(int pColumn, int pRow) {
            this.column = pColumn - 1;
            this.row = pRow - 1;
            this.trianglePosition = new java.awt.Point(TABLE_SIZE.width - (TABLE_SIZE.height / 2), TABLE_SIZE.height / 2);
        }

        /**
         * Restituisce la posizione della biglia.
         * @return La posizione della biglia.
         */
        public java.awt.Point getLocation() {
            int x = trianglePosition.x + this.column * (Sphere2D.BALL_SIZE - 4);
            int y = trianglePosition.y - this.column * (Sphere2D.BALL_SIZE / 2) + this.row * (Sphere2D.BALL_SIZE);
            return new java.awt.Point(x, y);
        }
    }

    /**
     * Crea ed inizializza un tavolo da biliardo.
     * @param pPoolManager Un <code>PoolManager</code> che gestisce le regole di gioco
     * @param pImage Un'immagine da utilizzare per il tavolo
     */
    public PoolTable(PoolGame.PoolManager pPoolManager, Image pImage) {
        this.image = pImage;
        this.balls = new PoolBall[16];
        this.balls[0] = new PoolBall(TABLE_SIZE.height / 2, TABLE_SIZE.height / 2, 0);
        this.image.add(this.balls[0].image);
        for (int i = 1; i < this.balls.length; i++) {
            this.balls[i] = new PoolBall(new Point(Triangle.values()[i - 1].getLocation()), i);
            this.image.add(this.balls[i].image);
        }
        for (PoolBall j : this.balls) {
            j.image.repaint();
        }
        this.poolManager = pPoolManager;
    }

    /**
     * Definisce velocità e angolazione della palla bianca dando avvio al movimento della palla
     * e di tutte quelle che verranno colpite.
     * @param pStrenght La forza con la quale è stata colpita la palla bianca
     * @param pAngle L'angolazione con la quale è stata colpita la palla bianca
     */
    public void strike(double pStrenght, double pAngle) {
        this.balls[0].speed.setPolarChoords(pStrenght, pAngle);
        this.ballsMovesThread.run();
    }
}
