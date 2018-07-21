package pool;

import phisic.*;
import media.support.*;
import java.awt.event.*;

/**
 * <code>PoolGame</code> è la classe che gestisce gli input e gli output di <code>8-Pool</code>.
 * @author Oneiros
 */
public class PoolGame extends Image {

    /** Color oro, utilizzato in varie parti del gioco */
    protected static final java.awt.Color GOLD = new java.awt.Color(255, 204, 0);
    private static final Sound MENU_SOUND = new Sound("stick.wav");
    private static final Sound MUSIC = new Sound("syncopations.wav", 70);
    private Image tablePanel;
    private PoolFrame frame;

    /**
     * Crea ed inizializza un'istanza di <code>PoolGame</code> impostando il gioco per la visualizzazione
     * del menu principale.
     * @param pFrame Il frame nel quale andrà inserito il gioco
     */
    public PoolGame(PoolFrame pFrame) {
        super("parquet.jpg");
        if (!PoolGame.MUSIC.isRunning()) {
            PoolGame.MUSIC.loop(-1);
        }
        this.initComponents();
        this.frame = pFrame;
        this.setSize(pFrame.getContentPane().getSize());
        this.setTile(true);
        this.tablePanel = new Image("poolTable.png");
        int x = (this.getWidth() - this.tablePanel.getSize().width) / 2;
        int y = (this.getHeight() - this.tablePanel.getSize().height) / 2;
        this.tablePanel.setLocation(x, y);
        this.initializePanel(this.mainPanel);
        this.initializePanel(this.nameChooserPanel);
        this.initializePanel(this.aboutPanel);
        this.add(this.tablePanel);

        final Image volumeToggle = new Image("volume" + ((Sound.enabled) ? "On" : "Off") + ".png");
        volumeToggle.setLocation(getWidth() - 30, getHeight() - 30);
        volumeToggle.setVisible(true);
        volumeToggle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        volumeToggle.setToolTipText("Enable/disable sound effects");
        volumeToggle.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (volumeToggle.getImageName().equals("volumeOn.png")) {
                    volumeToggle.setImage("volumeOff.png");
                } else {
                    volumeToggle.setImage("volumeOn.png");
                }
                Sound.enabled = !Sound.enabled;
            }
        });
        this.add(volumeToggle);
    }

    private void initializePanel(javax.swing.JPanel pPanel) {
        pPanel.setSize(this.tablePanel.getSize());
        this.tablePanel.add(pPanel);
    }

    /**
     * La classe astratta <code>PoolManager</code> definisce un'astratta modalità di gioco.
     */
    protected abstract class PoolManager {

        /** La modalità di tiro.<br><br>
         * <code>1</code> - La stecca viene posizionata dalla parte opposta del cursore rispetto alla palla
         * bianca ed il tiro è direzionato verso il cursore;<br>
         * <code>2</code> - La stecca segue il cursore ed il tiro è direzionato verso la parte opposta del
         * cursore rispetto alla palla bianca.
         */
        protected short STRIKE_MODE = 2;
        /** Il tavolo da biliardo. */
        protected PoolTable poolTable;
        /** La stecca. */
        protected Cue cue;
        /** La parte interna del tavolo. */
        protected Chamber chamber;
        private boolean cueIsCharging;
        /** <code>Listener</code> che si occupa di ruotare la stecca a seconda della posizione del cursore. */
        protected final MouseMotionAdapter rotatingCueListener = new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                rotateCue();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                rotateCue();
            }
        };
        /** <code>Listener</code> che si occupa di eseguire il colpo di stecca al click del mouse. */
        protected final MouseAdapter strikingCueListener = new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    cueIsCharging = false;
                    removeMouseListener(strikingCueListener);
                    removeMouseMotionListener(rotatingCueListener);
                    hideMouse();
                    Point mouseLocation = getMouseLocationInFrame();
                    mouseLocation.changeAxisSystem(getBallLocationInFrame(0));
                    if (STRIKE_MODE == 1) {
                        mouseLocation.x = -mouseLocation.x;
                    } else {
                        mouseLocation.y = -mouseLocation.y;
                    }
                    final double power = cue.getDistanceFromBall() / 10 - 2;
                    final double angle = mouseLocation.getAngularCoord();
                    new Thread("Striking") {

                        @Override
                        public void run() {
                            while (cue.getDistanceFromBall() > Sphere2D.BALL_SIZE / 2) {
                                cue.setDistanceFromBall(cue.getDistanceFromBall() - (int) power);
                                cue.repaint();
                                try {
                                    sleep(10);
                                } catch (InterruptedException ex) {
                                }
                            }
                            cue.setVisible(false);
                            cue.hitSound();
                            poolTable.strike(power, angle);
                        }
                    }.start();
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    cueIsCharging = true;
                    new Thread("ChargingThread") {

                        @Override
                        public void run() {
                            while (cue.getDistanceFromBall() != Cue.MAX_DISTANCE_FROM_BALL && cueIsCharging) {
                                cue.setDistanceFromBall(cue.getDistanceFromBall() + 1);
                                cue.repaint();
                                rotateCue();
                                try {
                                    sleep(10);
                                } catch (InterruptedException ex) {
                                }
                            }
                        }
                    }.start();
                }
            }
        };
        /** <code>Listener</code> che si occupa di spostare la palla bianca sotto il cursore. */
        protected final MouseMotionAdapter whiteBallMovingListener = new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                whiteBallOnMouse();
            }
        };
        /** <code>Listener</code> che si occupa di riposizionare la palla bianca dopo essere andate in buca. */
        protected final MouseAdapter whiteBallPositioningListener = new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    boolean control = true;
                    for (PoolBall j : poolTable.balls) {
                        if (poolTable.balls[0] != j && poolTable.balls[0].isTouching(j)) {
                            control = false;
                            break;
                        }
                    }
                    if (control) {
                        removeMouseMotionListener(whiteBallMovingListener);
                        removeMouseListener(whiteBallPositioningListener);
                        setNextTurn();
                    }
                }
            }
        };

        /**
         * Definisce ed inizializza i componenti comuni a tutte le modalità di gioco.
         */
        public PoolManager() {
            this.poolTable = new PoolTable(this, tablePanel);
            this.chamber = new Chamber();
            this.chamber.setLocation(tablePanel.getX() + (PoolTable.TABLE_SIZE.width - this.chamber.background.getImageSize().width) / 2,
                    getHeight() - this.chamber.background.getImageSize().height);
            this.cue = new Cue(Cue.Color.BROWN, this.poolTable.balls[0], tablePanel.getX(), tablePanel.getY());
            add(this.cue);
            this.cue.repaint();
            this.cueIsCharging = false;
            add(this.poolTable.image);
            add(this.chamber.border);
            add(this.chamber.background);
            addMouseListener(this.strikingCueListener);
            addMouseMotionListener(this.rotatingCueListener);
            this.showCrossCursor();

            java.awt.Cursor handCursor = new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR);

            final Image mainMenuButton = new Image("menu.png");
            mainMenuButton.setLocation(15, 15);
            mainMenuButton.setVisible(true);
            mainMenuButton.setCursor(handCursor);
            mainMenuButton.setToolTipText("Go back to Main Menu");
            mainMenuButton.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    frame.startGame();
                }
            });
            add(mainMenuButton);

            final Image strikeModeToggle = new Image("mode1.png");
            strikeModeToggle.setLocation(getWidth() - 60, getHeight() - 30);
            strikeModeToggle.setVisible(true);
            strikeModeToggle.setCursor(handCursor);
            strikeModeToggle.setToolTipText("Changes striking mode");
            strikeModeToggle.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (strikeModeToggle.getImageName().equals("mode1.png")) {
                        strikeModeToggle.setImage("mode2.png");
                        STRIKE_MODE = 1;
                    } else {
                        strikeModeToggle.setImage("mode1.png");
                        STRIKE_MODE = 2;
                    }
                }
            });
            add(strikeModeToggle);
            repaint();
        }

        /**
         * Restituisce la posizione del cursore rispetto al frame.
         * @return La posizione del cursore rispetto al frame
         */
        protected Point getMouseLocationInFrame() {
            Point mouseLocation = new Point(java.awt.MouseInfo.getPointerInfo().getLocation());
            try {
                mouseLocation.x -= getLocationOnScreen().x;
                mouseLocation.y -= getLocationOnScreen().y;
            } catch (java.awt.IllegalComponentStateException ex) {
            }
            return mouseLocation;
        }

        /**
         * Restituisce la posizione del cursore rispetto al tavolo da biliardo.
         * @return La posizione del cursore rispetto al tavolo da biliardo
         */
        protected Point getMouseLocationInTable() {
            Point mouseLocation = this.getMouseLocationInFrame();
            mouseLocation.x -= tablePanel.getX();
            mouseLocation.y -= tablePanel.getY();
            return mouseLocation;
        }

        /**
         * Sposta la biglia bianca sotto il cursore del mouse.
         */
        protected void whiteBallOnMouse() {
            Point position = this.getMouseLocationInTable();
            int ray = Sphere2D.BALL_SIZE / 2;
            if (position.x < PoolTable.Cushion.W.x1 + ray) {
                position.x = PoolTable.Cushion.W.x1 + ray;
            } else if (position.x > PoolTable.Cushion.E.x1 - ray) {
                position.x = PoolTable.Cushion.E.x1 - ray;
            }
            if (position.y < PoolTable.Cushion.N_A.y1 + ray) {
                position.y = PoolTable.Cushion.N_A.y1 + ray;
            } else if (position.y > PoolTable.Cushion.S_A.y1 - ray) {
                position.y = PoolTable.Cushion.S_A.y1 - ray;
            }
            this.poolTable.balls[0].setLocation(position);
        }

        /**
         * Ruota la stecca a seconda della posizione del cursore
         */
        protected void rotateCue() {
            Point mouseLocation = getMouseLocationInFrame();
            mouseLocation.changeAxisSystem(this.getBallLocationInFrame(0));
            if (STRIKE_MODE == 2) {
                mouseLocation.x = -mouseLocation.x;
                mouseLocation.y = -mouseLocation.y;
            }
            cue.rotate(mouseLocation.getAngularCoord());
        }

        /**
         * Restituisce la posizione della biglia contrassegnata dal numero <code>pBall</code> nel frame.
         * @param pBall Il numero identificativo della biglia
         * @return La posizione della biglia contrassegnata dal numero <code>pBall</code> nel frame
         */
        protected Point getBallLocationInFrame(int pBall) {
            Point ballLocation = poolTable.balls[0].getLocation();
            ballLocation.x += poolTable.image.getLocation().x;
            ballLocation.y += poolTable.image.getLocation().y;
            return ballLocation;
        }

        /**
         * Prepara il prossimo turno di gioco.
         */
        protected abstract void setNextTurn();

        /**
         * Gestisce la caduta in buca di una biglia.
         * @param pBall Il numero identificativo della biglia caduta in buca
         * @param pWith Il numero identificativo della biglia toccata per prima dalla palla bianca
         */
        protected abstract void pocket(int pBall, int pWith, int pPocket);

        /**
         * Gestisce la caduta in buca della biglia bianca.
         */
        protected void whiteBallInPocket() {
            poolTable.balls[0].image.setVisible(true);
            this.whiteBallOnMouse();
            addMouseMotionListener(this.whiteBallMovingListener);
            addMouseListener(this.whiteBallPositioningListener);
        }

        /**
         * Gestisce il termine del turno di gioco.
         */
        protected void turnEnded() {
            addMouseMotionListener(this.rotatingCueListener);
            addMouseListener(this.strikingCueListener);
            this.cue.prepare();
            this.rotateCue();
            this.cue.setVisible(true);
        }
        private final java.awt.Cursor defaultCursor = new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR);
        private final java.awt.Cursor crossCursor = new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR);

        /**
         * Mostra il cursore a forma di croce.
         */
        public final void showCrossCursor() {
            setCursor(this.crossCursor);
            tablePanel.setCursor(this.crossCursor);
        }

        /**
         * Mostra il cursore di default.
         */
        public final void showDefaultCursor() {
            setCursor(this.defaultCursor);
            tablePanel.setCursor(this.defaultCursor);
        }
    }

    /**
     * La classe <code>PracticePoolManager</code> definisce una modalità di gioco a giocatore singolo, senza alcuna
     * regola (modalità di pratica).
     */
    protected class PracticePoolManager extends PoolManager {

        public PracticePoolManager() {
            super();
        }

        @Override
        protected void pocket(int pBall, int pWith, int pPocket) {
        }

        @Override
        protected void setNextTurn() {
            this.showCrossCursor();
            if (!poolTable.balls[0].image.isVisible()) {
                this.whiteBallInPocket();
            } else {
                this.turnEnded();
            }
        }
    }

    /**
     * La classe <code>EightBallPoolManager</code> definisce una modalità di gioco multigiocatore con le regole
     * della Palla 8 americana.
     */
    protected class EightBallPoolManager extends PoolManager {

        /** Le regole di gioco della Palla 8. */
        protected GameManager gameManager;
        
        /** <code>Listener</code> che si occupa di eseguire il colpo di stecca al click del mouse. */
        protected final MouseAdapter choosePocketListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    for (PoolTable.Pocket p : PoolTable.Pocket.values()) {
                        if (getMouseLocationInTable().distance(p.getLocation()) < 20) {
                            gameManager.setDesignatedPocket(p.ordinal());
                            tablePanel.removeMouseListener(this);
                            tablePanel.removeMouseMotionListener(showCircleListener);
                            circle.setVisible(false);
                            turnEnded();
                            break;
                        }
                    }
                }
            }
        };
        protected final MouseMotionAdapter showCircleListener = new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                boolean isOnPocket = false;
                for (PoolTable.Pocket p : PoolTable.Pocket.values()) {
                    if (getMouseLocationInTable().distance(p.getLocation()) < circle.getWidth() / 2) {
                        circle.setLocation(p);
                        circle.setVisible(true);
                        isOnPocket = true;
                        break;
                    }
                }
                if (!isOnPocket) {
                    circle.setVisible(false);
                }
            }
        };

        private class Circle extends Image {

            protected Circle() {
                super("circle.png");
                this.setVisible(false);
            }

            protected void setLocation(PoolTable.Pocket pPocket) {
                int x = (int) pPocket.getLocation().x - circle.getWidth() / 2;
                int y = (int) pPocket.getLocation().y - circle.getWidth() / 2;
                circle.setLocation(x, y);
            }
        };
        private Circle circle;

        /**
         * La classe <code>Stats</code> modella il pannello segnapunti del gioco. In esso sono visualizzate
         * alcune informazioni sui giocatori come il loro nome, i loro punti e il tipo di biglie che devono
         * colpire. C'è inoltre una freccia che indica il giocatore di turno.
         */
        protected class Stats extends Image {

            private boolean firstPocket;
            private final javax.swing.ImageIcon arrowLeft = new Image("arrowLeft.png").getImageIcon();
            private final javax.swing.ImageIcon arrowRight = new Image("arrowRight.png").getImageIcon();

            /**
             * Crea ed inizializza un pannello segnapugni con i valori di default.
             */
            public Stats() {
                super("statsPanel.png");
                statsPanel.setBounds(25, 5, statsPanel.getPreferredSize().width, statsPanel.getPreferredSize().height);
                statsPanel.setVisible(true);
                this.initializeLabel(nameLabel1, gameManager.shooter1.getName(), gameManager.shooter1.getCueColor().getColor());
                this.initializeLabel(nameLabel2, gameManager.shooter2.getName(), gameManager.shooter2.getCueColor().getColor());
                this.initializeLabel(pointsLabel1, "0", java.awt.Color.white);
                this.initializeLabel(pointsLabel2, "0", java.awt.Color.white);
                this.updatePoints();
                this.indicate(gameManager.currentShooter);
                this.add(statsPanel);
                this.firstPocket = true;
            }

            private void initializeLabel(javax.swing.JLabel pLabel, String pText, java.awt.Color pColor) {
                pLabel.setText(pText);
                pLabel.setForeground(pColor);
            }

            private void initializeBallImage(int pBallIndex, int pPlayerIndex) {
                class PoolBallImage extends Image {

                    public PoolBallImage(int pBallIndex) {
                        super("ball" + pBallIndex + ".png");
                        this.setVisible(true);
                        this.setOpaque(false);
                    }

                    @Override
                    protected void paintComponent(java.awt.Graphics g) {
                        super.paintComponent(g);
                        g.setColor(java.awt.Color.white);
                        g.fillOval(9, 9, 11, 11);
                    }
                }
                PoolBallImage ball = new PoolBallImage(pBallIndex);
                ball.setLocation((pPlayerIndex == 1) ? 30 : 485, 16);
                this.add(ball);
            }

            /**
             * Aggiorna il pannello con i nuovi punti.
             */
            protected final void updatePoints() {
                class GetPointsAnimation extends Thread {

                    private javax.swing.JLabel textField;
                    private static final int WAIT = 300;

                    public GetPointsAnimation(javax.swing.JLabel textField) {
                        this.textField = textField;
                    }

                    @Override
                    public void run() {
                        animation();
                        animation();
                        animation();
                    }

                    private void animation() {
                        try {
                            this.textField.setVisible(false);
                            sleep(WAIT);
                            this.textField.setVisible(true);
                            sleep(WAIT);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
                int points1 = gameManager.shooter1.getPoints();
                int points2 = gameManager.shooter2.getPoints();
                if (!pointsLabel1.getText().equals(String.valueOf(points1))) {
                    pointsLabel1.setText(String.valueOf(points1));
                    new GetPointsAnimation(pointsLabel1).start();
                }
                if (!pointsLabel2.getText().equals(String.valueOf(points2))) {
                    pointsLabel2.setText(String.valueOf(points2));
                    new GetPointsAnimation(pointsLabel2).start();
                }
                if (this.firstPocket && (points1 > 0 || points2 > 0)) {
                    this.setBallType();
                    this.firstPocket = false;
                }
            }

            /**
             * Aggiunge due biglie affianco ai nomi dei giocatori per identificare il tipo di biglie che essi
             * devono imbucare.
             */
            protected void setBallType() {
                int ball1Index = 1 + gameManager.shooter1.getCueColor().ordinal();
                int ball2Index = 1 + gameManager.shooter2.getCueColor().ordinal();
                if (gameManager.shooter1.getType() == GameManager.BallType.STRIPED) {
                    ball1Index += 8;
                } else {
                    ball2Index += 8;
                }
                this.initializeBallImage(ball1Index, 1);
                this.initializeBallImage(ball2Index, 2);
            }

            /**
             * Volge la freccia centrale verso il giocatore <code>pShooter</code>.
             * @param pShooter Il giocatore di turno
             */
            protected final void indicate(GameManager.Shooter pShooter) {
                arrowLabel.setIcon((pShooter == gameManager.shooter1) ? this.arrowLeft : this.arrowRight);
            }

            /**
             * Elimina tutti i componenti presenti nel pannello segnapunti per visualizzare il nome del vincitore.
             * @param pWinner Il vincitore della partita.
             */
            protected void showWinner(GameManager.Shooter pWinner) {
                this.removeAll();
                statsPanel.removeAll();
                final javax.swing.JLabel winnerLabel;
                if (pWinner.getName().length() >= 20) {
                    winnerLabel = new javax.swing.JLabel(pWinner.getName().substring(0, 20) + "... WON");
                } else {
                    winnerLabel = new javax.swing.JLabel(pWinner.getName() + " WON");
                }
                winnerLabel.setVisible(true);
                winnerLabel.setOpaque(false);
                winnerLabel.setFont(new java.awt.Font("Consolas", 0, 30));
                winnerLabel.setForeground(pWinner.getCueColor().getColor());
                winnerLabel.setBounds(statsPanel.getBounds());
                winnerLabel.setMaximumSize(statsPanel.getSize());
                winnerLabel.setHorizontalAlignment(javax.swing.JLabel.CENTER);
                winnerLabel.setVerticalAlignment(javax.swing.JLabel.CENTER);
                this.add(winnerLabel);
                this.repaint();
                new Thread("Winner Animation") {

                    private static final int WAIT = 300;

                    @Override
                    public void run() {
                        animation();
                        animation();
                        animation();
                        animation();
                        animation();
                    }

                    private void animation() {
                        try {
                            sleep(WAIT);
                            winnerLabel.setVisible(false);
                            sleep(WAIT);
                            winnerLabel.setVisible(true);
                        } catch (InterruptedException ex) {
                        }
                    }
                }.start();
            }
        }
        /** Il pannello segnapunti. */
        protected Stats stats;

        /**
         * Crea ed inizializza un <code>PVPPoolManager</code> con le regole fornite da <code>pGameManager</code>
         * @param pGameManager <code>GameManager</code> contenente le regole di gioco.
         */
        protected EightBallPoolManager(GameManager pGameManager) {
            super();
            this.gameManager = pGameManager;
            this.gameManager.setNewTurn();
            this.stats = new Stats();
            this.stats.setLocation((getWidth() - this.stats.getWidth()) / 2, 0);
            add(this.stats);
            this.cue.setColor(this.gameManager.currentShooter.getCueColor());
            this.circle = new Circle();
            this.circle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            tablePanel.add(this.circle);
        }

        @Override
        protected void pocket(int pBall, int pWith, int pPocket) {
            this.gameManager.pocket(pBall, pWith, pPocket);
        }

        @Override
        protected void setNextTurn() {
            this.stats.updatePoints();
            switch (this.gameManager.getTurnResult()) {
                case WON:
                    this.stats.showWinner(gameManager.currentShooter);
                    this.showDefaultCursor();
                    new Sound("win.wav").run();
                    break;
                case LOST:
                    this.stats.showWinner(gameManager.waitingShooter);
                    this.showDefaultCursor();
                    new Sound("lose.wav").run();
                    break;
                case WHITE_IN_POCKET:
                    this.gameManager.whiteBallRepositionated();
                    this.stats.indicate(this.gameManager.waitingShooter);
                    super.whiteBallInPocket();
                    break;
                default:
                    this.gameManager.turnOver();
                    this.stats.indicate(this.gameManager.currentShooter);
                    this.turnEnded();
            }
        }

        @Override
        protected void turnEnded() {
            if (gameManager.playerHaveToChoosePocket()) {
                this.showDefaultCursor();
                new Thread("chooseAPocket!") {

                    @Override
                    public void run() {
                        circle.setVisible(true);
                        for (PoolTable.Pocket p : PoolTable.Pocket.values()) {
                            circle.setLocation(p);
                            try {
                                sleep(250);
                            } catch (InterruptedException ex) {
                            }
                        }
                        circle.setVisible(false);
                        tablePanel.addMouseMotionListener(showCircleListener);
                        tablePanel.addMouseListener(choosePocketListener);
                    }
                }.start();
            } else {
                this.showCrossCursor();
                this.gameManager.setNewTurn();
                this.cue.setColor(this.gameManager.currentShooter.getCueColor());
                super.turnEnded();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        practiceButton = new javax.swing.JButton();
        aboutButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        statsPanel = new javax.swing.JPanel();
        nameLabel1 = new javax.swing.JLabel();
        nameLabel2 = new javax.swing.JLabel();
        pointsLabel1 = new javax.swing.JLabel();
        pointsLabel2 = new javax.swing.JLabel();
        arrowLabel = new javax.swing.JLabel();
        nameChooserPanel = new javax.swing.JPanel();
        shooter1TextField = new javax.swing.JTextField();
        shooter2TextField = new javax.swing.JTextField();
        breaker1RadioButton = new javax.swing.JRadioButton();
        javax.swing.JRadioButton breaker2RadioButton = new javax.swing.JRadioButton();
        javax.swing.JButton startMultiplayerButton = new javax.swing.JButton();
        comboBox1 = new javax.swing.JComboBox();
        comboBox2 = new javax.swing.JComboBox();
        cuePreview1 = new javax.swing.JLabel();
        cuePreview2 = new javax.swing.JLabel();
        javax.swing.JLabel shooter1Label = new javax.swing.JLabel();
        javax.swing.JLabel shooter2Label = new javax.swing.JLabel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        javax.swing.JButton backButton = new javax.swing.JButton();
        javax.swing.ButtonGroup breakerButtonGroup = new javax.swing.ButtonGroup();
        aboutPanel = new javax.swing.JPanel();
        javax.swing.JLabel aboutImage = new javax.swing.JLabel();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        javax.swing.JPanel infoPanel = new javax.swing.JPanel();
        javax.swing.JLabel emailLabel = new javax.swing.JLabel();
        emailURL = new javax.swing.JLabel();
        javax.swing.JLabel profileLabel = new javax.swing.JLabel();
        profileURL = new javax.swing.JLabel();
        javax.swing.JLabel javaProjectsLabel = new javax.swing.JLabel();
        javaProjectsURL = new javax.swing.JLabel();
        javax.swing.JLabel cProjectsLabel = new javax.swing.JLabel();
        cProjectsURL = new javax.swing.JLabel();
        javax.swing.JButton aboutBackButton = new javax.swing.JButton();

        mainPanel.setBackground(new java.awt.Color(0, 0, 0));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setOpaque(false);
        mainPanel.setPreferredSize(new java.awt.Dimension(800, 540));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        startButton.setFont(new java.awt.Font("Trebuchet MS", 0, 24)); // NOI18N
        startButton.setForeground(GOLD);
        startButton.setText("START 8-BALL");
        startButton.setBorder(null);
        startButton.setBorderPainted(false);
        startButton.setContentAreaFilled(false);
        startButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        startButton.setFocusable(false);
        startButton.setName("startButton"); // NOI18N
        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                startButtonMouseExited(evt);
            }
        });
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        mainPanel.add(startButton, gridBagConstraints);

        practiceButton.setFont(new java.awt.Font("Trebuchet MS", 0, 24));
        practiceButton.setForeground(GOLD);
        practiceButton.setText("PRACTICE");
        practiceButton.setBorder(null);
        practiceButton.setBorderPainted(false);
        practiceButton.setContentAreaFilled(false);
        practiceButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        practiceButton.setFocusable(false);
        practiceButton.setName("practiceButton"); // NOI18N
        practiceButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                practiceButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                practiceButtonMouseExited(evt);
            }
        });
        practiceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                practiceButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        mainPanel.add(practiceButton, gridBagConstraints);

        aboutButton.setFont(new java.awt.Font("Trebuchet MS", 0, 24));
        aboutButton.setForeground(GOLD);
        aboutButton.setText("ABOUT");
        aboutButton.setBorder(null);
        aboutButton.setBorderPainted(false);
        aboutButton.setContentAreaFilled(false);
        aboutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        aboutButton.setFocusable(false);
        aboutButton.setName("aboutButton"); // NOI18N
        aboutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                aboutButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                aboutButtonMouseExited(evt);
            }
        });
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        mainPanel.add(aboutButton, gridBagConstraints);

        exitButton.setFont(new java.awt.Font("Trebuchet MS", 0, 24));
        exitButton.setForeground(GOLD);
        exitButton.setText("EXIT");
        exitButton.setBorder(null);
        exitButton.setBorderPainted(false);
        exitButton.setContentAreaFilled(false);
        exitButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        exitButton.setFocusable(false);
        exitButton.setName("exitButton"); // NOI18N
        exitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exitButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exitButtonMouseExited(evt);
            }
        });
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        mainPanel.add(exitButton, gridBagConstraints);

        statsPanel.setBackground(new java.awt.Color(0, 0, 0));
        statsPanel.setName("statsPanel"); // NOI18N
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new java.awt.Dimension(500, 50));
        statsPanel.setLayout(new java.awt.GridBagLayout());

        nameLabel1.setFont(new java.awt.Font("Consolas", 0, 18));
        nameLabel1.setForeground(new java.awt.Color(255, 204, 0));
        nameLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        nameLabel1.setMaximumSize(new java.awt.Dimension(170, 25));
        nameLabel1.setMinimumSize(new java.awt.Dimension(170, 25));
        nameLabel1.setName("nameLabel1"); // NOI18N
        nameLabel1.setPreferredSize(new java.awt.Dimension(170, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        statsPanel.add(nameLabel1, gridBagConstraints);

        nameLabel2.setFont(new java.awt.Font("Consolas", 0, 18));
        nameLabel2.setForeground(new java.awt.Color(255, 204, 0));
        nameLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        nameLabel2.setMaximumSize(new java.awt.Dimension(170, 25));
        nameLabel2.setMinimumSize(new java.awt.Dimension(170, 25));
        nameLabel2.setName("nameLabel2"); // NOI18N
        nameLabel2.setPreferredSize(new java.awt.Dimension(170, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 30);
        statsPanel.add(nameLabel2, gridBagConstraints);

        pointsLabel1.setFont(new java.awt.Font("Consolas", 0, 18));
        pointsLabel1.setForeground(new java.awt.Color(255, 204, 0));
        pointsLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        pointsLabel1.setMaximumSize(new java.awt.Dimension(170, 25));
        pointsLabel1.setMinimumSize(new java.awt.Dimension(170, 25));
        pointsLabel1.setName("pointsLabel1"); // NOI18N
        pointsLabel1.setPreferredSize(new java.awt.Dimension(170, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        statsPanel.add(pointsLabel1, gridBagConstraints);

        pointsLabel2.setFont(new java.awt.Font("Consolas", 0, 18));
        pointsLabel2.setForeground(new java.awt.Color(255, 204, 0));
        pointsLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pointsLabel2.setMaximumSize(new java.awt.Dimension(170, 25));
        pointsLabel2.setMinimumSize(new java.awt.Dimension(170, 25));
        pointsLabel2.setName("pointsLabel2"); // NOI18N
        pointsLabel2.setPreferredSize(new java.awt.Dimension(170, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 30);
        statsPanel.add(pointsLabel2, gridBagConstraints);

        arrowLabel.setVisible(true);
        arrowLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/images/arrowLeft.png"))); // NOI18N
        arrowLabel.setName("arrowLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        statsPanel.add(arrowLabel, gridBagConstraints);

        nameChooserPanel.setVisible(false);
        nameChooserPanel.setName("nameChooserPanel"); // NOI18N
        nameChooserPanel.setOpaque(false);
        nameChooserPanel.setLayout(new java.awt.GridBagLayout());

        shooter1TextField.setColumns(1);
        shooter1TextField.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        shooter1TextField.setMaximumSize(new java.awt.Dimension(6, 20));
        shooter1TextField.setName("shooter1TextField"); // NOI18N
        shooter1TextField.setNextFocusableComponent(shooter2TextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 55);
        nameChooserPanel.add(shooter1TextField, gridBagConstraints);

        shooter2TextField.setColumns(1);
        shooter2TextField.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        shooter2TextField.setMaximumSize(new java.awt.Dimension(6, 20));
        shooter2TextField.setName("shooter2TextField"); // NOI18N
        shooter2TextField.setNextFocusableComponent(startMultiplayerButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 55);
        nameChooserPanel.add(shooter2TextField, gridBagConstraints);

        breakerButtonGroup.add(breaker1RadioButton);
        breaker1RadioButton.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        breaker1RadioButton.setText("Breaker");
        breaker1RadioButton.setName("breaker1RadioButton"); // NOI18N
        breaker1RadioButton.setOpaque(false);
        breaker1RadioButton.setSelected(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        nameChooserPanel.add(breaker1RadioButton, gridBagConstraints);

        breakerButtonGroup.add(breaker2RadioButton);
        breaker2RadioButton.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        breaker2RadioButton.setText("Breaker");
        breaker2RadioButton.setName("breaker2RadioButton"); // NOI18N
        breaker2RadioButton.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        nameChooserPanel.add(breaker2RadioButton, gridBagConstraints);

        startMultiplayerButton.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        startMultiplayerButton.setText("Start!");
        startMultiplayerButton.setName("startMultiplayerButton"); // NOI18N
        startMultiplayerButton.setNextFocusableComponent(shooter1TextField);
        startMultiplayerButton.setOpaque(false);
        startMultiplayerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startMultiplayerButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        nameChooserPanel.add(startMultiplayerButton, gridBagConstraints);

        comboBox1.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        comboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yellow", "Blue", "Red", "Purple", "Orange", "Green", "Brown" }));
        comboBox1.setSelectedIndex(2);
        comboBox1.setName("comboBox1"); // NOI18N
        comboBox1.setOpaque(false);
        comboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBox1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 0);
        nameChooserPanel.add(comboBox1, gridBagConstraints);

        comboBox2.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        comboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yellow", "Blue", "Red", "Purple", "Orange", "Green", "Brown" }));
        comboBox2.setSelectedIndex(1);
        comboBox2.setName("comboBox2"); // NOI18N
        comboBox2.setOpaque(false);
        comboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBox2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 0);
        nameChooserPanel.add(comboBox2, gridBagConstraints);

        cuePreview1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/images/cueRed.png"))); // NOI18N
        cuePreview1.setName("cuePreview1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 50, 5);
        nameChooserPanel.add(cuePreview1, gridBagConstraints);

        cuePreview2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/images/cueBlue.png"))); // NOI18N
        cuePreview2.setName("cuePreview2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 50, 5);
        nameChooserPanel.add(cuePreview2, gridBagConstraints);

        shooter1Label.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        shooter1Label.setText("FIRST SHOOTER");
        shooter1Label.setName("shooter1Label"); // NOI18N
        shooter1Label.setForeground(GOLD);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        nameChooserPanel.add(shooter1Label, gridBagConstraints);

        shooter2Label.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        shooter2Label.setText("SECOND SHOOTER");
        shooter2Label.setName("shooter2Label"); // NOI18N
        shooter2Label.setForeground(GOLD);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        nameChooserPanel.add(shooter2Label, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jLabel1.setText("Name:");
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 50, 5, 5);
        nameChooserPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jLabel2.setText("Cue color:");
        jLabel2.setName("jLabel2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 0);
        nameChooserPanel.add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jLabel3.setText("Name:");
        jLabel3.setName("jLabel3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 50, 5, 5);
        nameChooserPanel.add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jLabel4.setText("Cue color:");
        jLabel4.setName("jLabel4"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        nameChooserPanel.add(jLabel4, gridBagConstraints);

        backButton.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        backButton.setText("Back");
        backButton.setName("backButton"); // NOI18N
        backButton.setOpaque(false);
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        nameChooserPanel.add(backButton, gridBagConstraints);

        aboutPanel.setName("aboutPanel"); // NOI18N
        aboutPanel.setOpaque(false);
        aboutPanel.setVisible(false);
        aboutPanel.setLayout(new java.awt.GridBagLayout());

        aboutImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/images/aboutPhoto.jpg"))); // NOI18N
        aboutImage.setName("aboutImage"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 40);
        aboutPanel.add(aboutImage, gridBagConstraints);

        jLabel5.setText("<html><div style=\"text-align: center;\"><span style=\"font-size: 24pt; font-family: 'Trebuchet MS'; \"><span style=\"font-size: 36pt; \"><span style=\"color: rgb(255, 165, 0);\">8-Pool</span></span></span></div>\n<div style=\"text-align: center;\"><span style=\"font-size: 18pt; font-style: italic; \">Simulazione in Java del gioco Palla 8</span></div>\n<div style=\"text-align: center;\"><br />\n  </div>\n<div style=\"text-align: center;\"><br />\n  </div>\n<div style=\"text-align: center;\"><span style=\"font-size: 12pt; \">Progetto realizzato da <span style=\"color: rgb(255, 0, 0); font-weight: bold; \">Lorenzo Valente</span> (aka Oneiros)</span></div>\n<div style=\"text-align: center;\"><span style=\"font-size: 12pt; \">Versione <span style=\"font-weight: bold; \">1.0</span> (7-7-2010)</span></div></html>");
        jLabel5.setName("jLabel5"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        aboutPanel.add(jLabel5, gridBagConstraints);

        infoPanel.setName("infoPanel"); // NOI18N
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new java.awt.GridBagLayout());

        emailLabel.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        emailLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        emailLabel.setText("E-mail");
        emailLabel.setName("emailLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        infoPanel.add(emailLabel, gridBagConstraints);

        emailURL.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        emailURL.setForeground(new java.awt.Color(0, 0, 255));
        emailURL.setText("oneiros.valente@gmail.com");
        emailURL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        emailURL.setName("emailURL"); // NOI18N
        emailURL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                emailURLMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        infoPanel.add(emailURL, gridBagConstraints);

        profileLabel.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        profileLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        profileLabel.setText("Profilo");
        profileLabel.setName("profileLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        infoPanel.add(profileLabel, gridBagConstraints);

        profileURL.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        profileURL.setForeground(new java.awt.Color(0, 0, 255));
        profileURL.setText("http://www.facebook.com/lorenzo.valente");
        profileURL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        profileURL.setName("profileURL"); // NOI18N
        profileURL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                profileURLMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        infoPanel.add(profileURL, gridBagConstraints);

        javaProjectsLabel.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        javaProjectsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        javaProjectsLabel.setText("Progetti in Java");
        javaProjectsLabel.setName("javaProjectsLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        infoPanel.add(javaProjectsLabel, gridBagConstraints);

        javaProjectsURL.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        javaProjectsURL.setForeground(new java.awt.Color(0, 0, 255));
        javaProjectsURL.setText("http://code.google.com/p/oneirosjava/");
        javaProjectsURL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        javaProjectsURL.setName("javaProjectsURL"); // NOI18N
        javaProjectsURL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                javaProjectsURLMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        infoPanel.add(javaProjectsURL, gridBagConstraints);

        cProjectsLabel.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        cProjectsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cProjectsLabel.setText("Progetti in C/C++");
        cProjectsLabel.setName("cProjectsLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        infoPanel.add(cProjectsLabel, gridBagConstraints);

        cProjectsURL.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        cProjectsURL.setForeground(new java.awt.Color(0, 0, 255));
        cProjectsURL.setText("http://code.google.com/p/oneirosc/");
        cProjectsURL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cProjectsURL.setName("cProjectsURL"); // NOI18N
        cProjectsURL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cProjectsURLMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        infoPanel.add(cProjectsURL, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        aboutPanel.add(infoPanel, gridBagConstraints);

        aboutBackButton.setText("Back");
        aboutBackButton.setName("aboutBackButton"); // NOI18N
        aboutBackButton.setOpaque(false);
        aboutBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutBackButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        aboutPanel.add(aboutBackButton, gridBagConstraints);

        setLayout(null);
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startButtonMouseEntered
        this.startButton.setForeground(java.awt.Color.white);
}//GEN-LAST:event_startButtonMouseEntered

    private void startButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startButtonMouseExited
        this.startButton.setForeground(GOLD);
}//GEN-LAST:event_startButtonMouseExited
    private KeyAdapter enterPressingListener = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                startMultiplayerButtonActionPerformed(null);
            }
        }
    };
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        PoolGame.MENU_SOUND.run();
        this.startButton.setForeground(GOLD);
        this.mainPanel.setVisible(false);
        this.nameChooserPanel.setVisible(true);
        this.shooter1TextField.addKeyListener(enterPressingListener);
        this.shooter2TextField.addKeyListener(enterPressingListener);
        this.addKeyListener(enterPressingListener);
}//GEN-LAST:event_startButtonActionPerformed

    private void practiceButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_practiceButtonMouseEntered
        this.practiceButton.setForeground(java.awt.Color.white);
}//GEN-LAST:event_practiceButtonMouseEntered

    private void practiceButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_practiceButtonMouseExited
        this.practiceButton.setForeground(GOLD);
}//GEN-LAST:event_practiceButtonMouseExited

    private void practiceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_practiceButtonActionPerformed
        PoolGame.MENU_SOUND.run();
        this.practiceButton.setForeground(GOLD);
        this.mainPanel.setVisible(false);
        tablePanel.remove(this.mainPanel);
        new PracticePoolManager();
}//GEN-LAST:event_practiceButtonActionPerformed

    private void aboutButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutButtonMouseEntered
        this.aboutButton.setForeground(java.awt.Color.white);
}//GEN-LAST:event_aboutButtonMouseEntered

    private void aboutButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutButtonMouseExited
        this.aboutButton.setForeground(GOLD);
}//GEN-LAST:event_aboutButtonMouseExited

    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        PoolGame.MENU_SOUND.run();
        this.aboutButton.setForeground(GOLD);
        this.mainPanel.setVisible(false);
        this.aboutPanel.setVisible(true);
}//GEN-LAST:event_aboutButtonActionPerformed

    private void exitButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitButtonMouseEntered
        this.exitButton.setForeground(java.awt.Color.white);
}//GEN-LAST:event_exitButtonMouseEntered

    private void exitButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitButtonMouseExited
        this.exitButton.setForeground(GOLD);
}//GEN-LAST:event_exitButtonMouseExited

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        PoolGame.MENU_SOUND.run();
        System.exit(0);
}//GEN-LAST:event_exitButtonActionPerformed

    private void startMultiplayerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startMultiplayerButtonActionPerformed
        PoolGame.MENU_SOUND.run();
        this.removeKeyListener(enterPressingListener);
        class EmptyNameError extends Thread {

            private javax.swing.JTextField textField;
            private static final int WAIT = 100;

            public EmptyNameError(javax.swing.JTextField textField) {
                this.textField = textField;
            }

            @Override
            public void run() {
                animation();
                animation();
                animation();
            }

            private void animation() {
                try {
                    this.textField.setBackground(java.awt.Color.red);
                    sleep(WAIT);
                    this.textField.setBackground(java.awt.Color.white);
                    sleep(WAIT);
                } catch (InterruptedException ex) {
                }
            }
        }
        class SameColorError extends Thread {

            private static final int WAIT = 100;

            @Override
            public void run() {
                animation();
                animation();
                animation();
            }

            private void animation() {
                try {
                    comboBox1.setForeground(java.awt.Color.red);
                    comboBox2.setForeground(java.awt.Color.red);
                    sleep(WAIT);
                    comboBox1.setForeground(java.awt.Color.black);
                    comboBox2.setForeground(java.awt.Color.black);
                    sleep(WAIT);
                } catch (InterruptedException ex) {
                }
            }
        }
        boolean control = true;
        if (this.shooter1TextField.getText().isEmpty()) {
            new EmptyNameError(this.shooter1TextField).start();
            control = false;
        }
        if (this.shooter2TextField.getText().isEmpty()) {
            new EmptyNameError(this.shooter2TextField).start();
            control = false;
        }
        if (this.comboBox1.getSelectedIndex() == this.comboBox2.getSelectedIndex()) {
            new SameColorError().start();
            control = false;
        }
        if (control) {
            this.nameChooserPanel.setVisible(false);
            this.statsPanel.setVisible(true);
            int breaker = (this.breaker1RadioButton.isSelected()) ? 1 : 2;
            Cue.Color color1 = Cue.Color.values()[this.comboBox1.getSelectedIndex()];
            Cue.Color color2 = Cue.Color.values()[this.comboBox2.getSelectedIndex()];
            GameManager gm = new GameManager(this.shooter1TextField.getText(), this.shooter2TextField.getText(), breaker, color1, color2);
            new EightBallPoolManager(gm);
        }
}//GEN-LAST:event_startMultiplayerButtonActionPerformed

    private javax.swing.ImageIcon getCueImageIcon(String pFileName) {
        return new javax.swing.ImageIcon(getClass().getResource("/media/images/" + pFileName));
    }

    private void updateCuePreview(int pComboBox) {
        if (pComboBox == 1) {
            String cueFileName = Cue.Color.values()[this.comboBox1.getSelectedIndex()].getFileName();
            this.cuePreview1.setIcon(getCueImageIcon(cueFileName));
        } else {
            String cueFileName = Cue.Color.values()[this.comboBox2.getSelectedIndex()].getFileName();
            this.cuePreview2.setIcon(getCueImageIcon(cueFileName));
        }
    }

    private void comboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBox1ActionPerformed
        this.updateCuePreview(1);
    }//GEN-LAST:event_comboBox1ActionPerformed

    private void comboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBox2ActionPerformed
        this.updateCuePreview(2);
}//GEN-LAST:event_comboBox2ActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        PoolGame.MENU_SOUND.run();
        this.nameChooserPanel.setVisible(false);
        this.mainPanel.setVisible(true);
        this.shooter1TextField.removeKeyListener(enterPressingListener);
        this.shooter2TextField.removeKeyListener(enterPressingListener);
        this.removeKeyListener(enterPressingListener);
}//GEN-LAST:event_backButtonActionPerformed

    private void emailURLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_emailURLMouseClicked
        if (!this.emailURL.getText().equals("COPIED IN CLIPBOARD")) {
            java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new java.awt.datatransfer.StringSelection(this.emailURL.getText()), null);
            this.emailURL.setForeground(GOLD);
            this.emailURL.setText("COPIED IN CLIPBOARD");
        }
}//GEN-LAST:event_emailURLMouseClicked

    private void profileURLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_profileURLMouseClicked
        this.openURL(this.profileURL.getText());
}//GEN-LAST:event_profileURLMouseClicked

    private void javaProjectsURLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_javaProjectsURLMouseClicked
        this.openURL(this.javaProjectsURL.getText());
}//GEN-LAST:event_javaProjectsURLMouseClicked

    private void cProjectsURLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cProjectsURLMouseClicked
        this.openURL(this.cProjectsURL.getText());
}//GEN-LAST:event_cProjectsURLMouseClicked

    private void aboutBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutBackButtonActionPerformed
        PoolGame.MENU_SOUND.run();
        this.mainPanel.setVisible(true);
        this.aboutPanel.setVisible(false);
}//GEN-LAST:event_aboutBackButtonActionPerformed

    private void hideMouse() {
        java.awt.Image image = java.awt.Toolkit.getDefaultToolkit().createImage(new java.awt.image.MemoryImageSource(16, 16, new int[16 * 16], 0, 16));
        this.tablePanel.setCursor(java.awt.Toolkit.getDefaultToolkit().createCustomCursor(image, new java.awt.Point(0, 0), "invisibleCursor"));
    }

    private void openURL(String pUrl) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(pUrl));
        } catch (java.io.IOException ex) {
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutButton;
    private javax.swing.JPanel aboutPanel;
    private javax.swing.JLabel arrowLabel;
    private javax.swing.JRadioButton breaker1RadioButton;
    private javax.swing.JLabel cProjectsURL;
    private javax.swing.JComboBox comboBox1;
    private javax.swing.JComboBox comboBox2;
    private javax.swing.JLabel cuePreview1;
    private javax.swing.JLabel cuePreview2;
    private javax.swing.JLabel emailURL;
    private javax.swing.JButton exitButton;
    private javax.swing.JLabel javaProjectsURL;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel nameChooserPanel;
    private javax.swing.JLabel nameLabel1;
    private javax.swing.JLabel nameLabel2;
    private javax.swing.JLabel pointsLabel1;
    private javax.swing.JLabel pointsLabel2;
    private javax.swing.JButton practiceButton;
    private javax.swing.JLabel profileURL;
    private javax.swing.JTextField shooter1TextField;
    private javax.swing.JTextField shooter2TextField;
    private javax.swing.JButton startButton;
    private javax.swing.JPanel statsPanel;
    // End of variables declaration//GEN-END:variables
}
