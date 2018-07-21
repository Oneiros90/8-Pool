package pool;

/**
 * La classe <code>GameManager</code> gestisce le regole della versione americana di Palla 8:
 * <br><br>
 * Le biglie vengono divise attraverso la loro colorazione: le biglie "piene" (da 1 a 7) e le "rigate"
 * (da 9 a 15). L'attrezzatura di gioco è completata dalla presenza della biglia battente, bianca,
 * colpita dal giocatore attraverso la stecca da biliardo.<br>
 * Per cominciare, si decide chi sarà a "spaccare" (ovvero colpire per primo le palline) dopodichè
 * i due giocatori si alterneranno sino al momento in cui uno dei due riuscirà ad imbucare una palla,
 * rigata o piena. Da quel momento il giocatore stesso dovrà continuare ad indirizzare in buca solo quelle
 * dello stesso tipo per tutta la partita, lasciando le altre al suo avversario. Il turno di gioco passa
 * all'avversario (indipendentemente dal risultato del tiro) ogni qual volta viene fatto fallo ("foul"),
 * evento che avviene nei seguenti casi:
 * <br><br>
 * - La biglia battente non tocca nessuna delle altre biglie;<br>
 * - La prima palla toccata dalla biglia battente non appartiene al tipo del giocatore di turno;<br>
 * - Il giocatore di turno imbuca la biglia battente (in questo caso, prima di procedere, l'avversario deve
 * rimettere in gioco la palla bianca in una posizione a piacere);<br>
 * - Il giocatore di turno imbuca almeno una biglia che non appartiene al suo tipo.
 * <br><br>
 * La biglia n° 8 appartiene ad entrambi i giocatori e la sua caduta in buca determina la fine del gioco.
 * A seconda di come è stata imbucata la biglia n° 8 viene stabilito il vincitore:
 * <br><br>
 * - Se il "breaker" (il giocatore che spacca) imbuca la palla 8 al primo colpo, egli vince automaticamente;<br>
 * - Se il giocatore di turno imbuca la palla 8 ma ci sono altre biglie del proprio tipo in gioco, il
 * giocatore perde automaticamente;<br>
 * - Se la prima palla toccata dalla biglia battente non appartiene al tipo del giocatore di turno
 * e la palla 8 cade in buca, il giocatore perde automaticamente;<br>
 * - Se il giocatore di turno imbuca la palla 8 dopo aver imbucato tutte le altre biglie del proprio tipo
 * e non si verifica la condizione n°3, il giocatore vince la partita.
 * @author Oneiros
 */
public class GameManager {

    /** Il primo giocatore inserito nel gioco.*/
    protected Shooter shooter1;
    /** Il secondo giocatore inserito nel gioco.*/
    protected Shooter shooter2;
    /** Puntatore al giocatore di turno.*/
    protected Shooter currentShooter;
    /** Puntatore al giocatore in attesa.*/
    protected Shooter waitingShooter;
    private boolean inTurn;
    private boolean strike;
    private Result turnResult;
    private int numberOfPockets;
    private int designatedPocket;

    /**
     * L'enum <code>BallType</code> assegna ad ogni palla da biliardo il tipo <code>SOLID</code> ("piena")
     * oppure <code>STRIPED</code> ("rigata") a seconda del loro tipo di colorazione. Alla palla bianca
     * e alla nera viene assegnato il tipo <code>NONE</code>.
     */
    protected static enum BallType {

        /** Nessun tipo */
        NONE,
        /** Tipo "pieno" */
        SOLID,
        /** Tipo "rigato" */
        STRIPED;

        /**
         * Stabilisce il tipo di una palla a partire dal suo numero identificativo.
         * @param pBall Il numero identificativo della palla
         * @return Il tipo della palla
         */
        protected static BallType getBallType(int pBall) {
            if (pBall >= 1 && pBall <= 7) {
                return SOLID;
            } else if (pBall >= 9 && pBall <= 15) {
                return STRIPED;
            } else {
                return NONE;
            }
        }

        /**
         * Restituisce il tipo opposto.
         * @return <code>SOLID</code> se il tipo del <code>BallType</code> chiamante è <code>STRIPED</code>;<br>
         *         <code>STRIPED</code> se il tipo del <code>BallType</code> chiamante è <code>SOLID</code>;<br>
         *         <code>NONE</code> se il tipo del <code>BallType</code> chiamante è <code>NONE</code>;<br>
         */
        protected BallType getOpposite() {
            switch (this) {
                case SOLID:
                    return STRIPED;
                case STRIPED:
                    return SOLID;
                default:
                    return NONE;
            }
        }
    };

    /**
     * La classe <code>Shooter</code> modella un giocatore di biliardo tramite il suo nome, i punti
     * ottenuti durante la partita, il tipo di palle da biliardo che deve imbucare ed il colore della sua
     * stecca.
     */
    protected class Shooter {

        private int points;
        private String name;
        private BallType type;
        private Cue.Color color;

        /**
         * Crea ed inizializza un giocatore tramite il suo nome ed il colore della sua stecca.
         * Ogni giocatore inizia con <code>0</code> punti e senza un tipo di palla da colpire.
         * @param pName Il nome del giocatore
         * @param pColor Il colore della stecca
         */
        protected Shooter(String pName, Cue.Color pColor) {
            this.name = pName;
            this.points = 0;
            this.type = BallType.NONE;
            this.color = pColor;
        }

        /**
         * Stabilisce se la palla <code>pBall</code> deve essere imbucata da questo giocatore
         * @param pBall Il numero identificativo della biglia imbucata
         * @return <code>true</code> se la palla <code>pBall</code> deve essere imbucata da questo giocatore;
         *         <code>true</code> altrimenti.
         */
        public boolean own(int pBall) {
            return this.type == BallType.getBallType(pBall);
        }

        /**
         * Restituisce il nome del giocatore.
         * @return Il nome del giocatore
         */
        public String getName() {
            return this.name;
        }

        /**
         * Restituisce i punti attuali del giocatore.
         * @return I punti del giocatore
         */
        public int getPoints() {
            return this.points;
        }

        /**
         * Restituisce il tipo di biglie che il giocatore deve mandare in buca.
         * @return Il tipo di biglie che il giocatore deve mandare in buca
         */
        public BallType getType() {
            return this.type;
        }

        /**
         * Restituisce il colore della stecca del giocatore.
         * @return Il colore della stecca del giocatore
         */
        public Cue.Color getCueColor() {
            return this.color;
        }
    }

    /**
     * L'enum <code>Result</code> elenca i possibili risultati di un turno.
     * <br><br>
     * <code>NEW_STRIKE</code> - Il giocatore ha giocato bene: ha diritto ad un nuovo tiro;<br>
     * <code>TURN_OVER</code> - Il giocatore ha fatto fallo: il turno passa all'avversario;<br>
     * <code>WHITE_IN_POCKET</code> - La palla bianca è andata in buca: il turno passa all'avversario,il quale
     * deve scegliere dove posizionare la palla bianca;<br>
     * <code>WON</code> - Il giocatore ha vinto;<br>
     * <code>LOST</code> - Il giocatore ha perso;<br>
     */
    protected enum Result {

        /** Il giocatore ha giocato bene: ha diritto ad un nuovo tiro. */
        NEW_STRIKE,
        /** Il giocatore ha fatto fallo: il turno passa all'avversario. */
        TURN_OVER,
        /** La palla bianca è andata in buca: il turno passa all'avversario, il quale deve scegliere dove
         * posizionare la palla bianca. */
        WHITE_IN_POCKET,
        /** Il giocatore ha vinto. */
        WON,
        /** Il giocatore ha perso. */
        LOST
    }

    /**
     * Crea ed inizializza un <code>GameManager</code> con i seguenti parametri.
     * @param pName1 Il nome del primo giocatore;
     * @param pName2 Il nome del secondo giocatore;
     * @param pBreaker <code>1</code> se sarà il primo giocatore a spaccare;
     *                 <code>2</code> se sarà il secondo giocatore a spaccare;
     * @param pColor1 Il colore della stecca del primo giocatore;
     * @param pColor2 Il colore della stecca del secondo giocatore;
     */
    public GameManager(String pName1, String pName2, int pBreaker, Cue.Color pColor1, Cue.Color pColor2) {
        this.shooter1 = new Shooter(String.valueOf(pName1.charAt(0)).toUpperCase() + pName1.substring(1), pColor1);
        this.shooter2 = new Shooter(String.valueOf(pName2.charAt(0)).toUpperCase() + pName2.substring(1), pColor2);
        if (pBreaker == 1) {
            this.currentShooter = this.shooter1;
            this.waitingShooter = this.shooter2;
        } else {
            this.currentShooter = this.shooter2;
            this.waitingShooter = this.shooter1;
        }
        this.inTurn = false;
        this.strike = true;
    }

    /**
     * Prepara un nuovo turno di gioco. Questo metodo può generare eccezioni se chiamato in momenti di gioco
     * sbagliati.
     */
    public void setNewTurn() {
        if (!this.inTurn) {
            this.inTurn = true;
            this.turnResult = Result.NEW_STRIKE;
            this.numberOfPockets = 0;
        } else {
            throw new RuntimeException("Last turn did not ended");
        }
    }

    /**
     * Gestisce la caduta in buca di una biglia stabilendo e se essa è stata proficua o meno per il giocatore.
     * @param pBall Il numero identificativo della biglia caduta in buca
     * @param pWith Il numero identificativo della prima biglia toccata dalla biglia battente
     */
    public void pocket(int pBall, int pWith, int pPocket) {
        if (this.inTurn) {

            Result currentResult = this.getCurrentResult(pBall, pWith, pPocket);
            this.numberOfPockets++;

            // Casi in cui il risultato di questa buca non influisce sul risultato del turno:

            if (this.turnResult == Result.LOST) {
                // Il giocatore ha imbucato la nera prima del tempo. Il giocatore ha perso
                if (currentResult == Result.NEW_STRIKE || currentResult == Result.TURN_OVER) {
                    // Il giocatore ha imbucato un'altra palla ma ha perso comunque la partita
                } else if (currentResult == Result.WHITE_IN_POCKET) {
                    // Il giocatore ha imbucato anche la bianca ma ha perso comunque la partita
                }
            } else if (this.turnResult == Result.TURN_OVER) {
                // Il giocatore ha toccato o imbucato una palla dell'avversario
                if (currentResult == Result.NEW_STRIKE) {
                    // Il giocatore ha imbucato una sua palla ma ha ormai perso il turno
                } else if (currentResult == Result.WON) {
                    // Il giocatore ha imbucato scorrettamente la nera. Il giocatore ha perso nonostante avesse 7 punti
                    this.turnResult = Result.LOST;
                } else {
                    this.turnResult = currentResult;
                }
            } else if (this.turnResult == Result.WHITE_IN_POCKET) {
                // Il giocatore ha imbucato la bianca
                if (currentResult == Result.NEW_STRIKE || currentResult == Result.TURN_OVER) {
                    // Il giocatore ha imbucato un'altra palla ma ha perso comunque il turno
                } else if (currentResult == Result.WON) {
                    // Il giocatore ha imbucato scorrettamente la nera. Il giocatore ha perso nonostante avesse 7 punti
                    this.turnResult = Result.LOST;
                } else {
                    this.turnResult = currentResult;
                }
            } else if (this.turnResult == Result.WON) {
                // Il giocatore ha imbucato la nera correttamente
                if (currentResult == Result.NEW_STRIKE || currentResult == Result.TURN_OVER) {
                    // Il giocatore ha imbucato un'altra palla ma ha comunque vinto
                } else if (currentResult == Result.WHITE_IN_POCKET) {
                    // Il giocatore ha imbucato anche la bianca. Il giocatore ha perso
                    this.turnResult = Result.LOST;
                }
            } else {
                // Il giocatore ha imbucato correttamente una sua palla
                this.turnResult = currentResult;
            }
        } else {
            throw new RuntimeException("Turn did not started");
        }
    }

    private Result getCurrentResult(int pBall, int pWith, int pPocket) {
        if (pBall == 0) {
            return Result.WHITE_IN_POCKET;
        } else if (pBall == 8) {
            if (this.strike && this.currentShooter.own(pWith)) {
                return Result.WON;
            } else if (this.currentShooter.points == 7 &&
                    (this.currentShooter.own(pWith) || pWith == 8) &&
                    pPocket == this.designatedPocket) {
                return Result.WON;
            } else {
                return Result.LOST;
            }
        }
        if (this.currentShooter.type == BallType.NONE) {
            this.currentShooter.type = BallType.getBallType(pBall);
            this.waitingShooter.type = this.currentShooter.type.getOpposite();
            this.currentShooter.points++;
            return Result.NEW_STRIKE;
        }
        if (this.currentShooter.own(pBall)) {
            this.currentShooter.points++;
            if (this.currentShooter.own(pWith)) {
                return Result.NEW_STRIKE;
            } else if (this.turnResult == Result.NEW_STRIKE && this.numberOfPockets >= 1) {
                return Result.NEW_STRIKE;
            } else {
                return Result.TURN_OVER;
            }
        } else {
            this.waitingShooter.points++;
            return Result.TURN_OVER;
        }
    }

    public void setDesignatedPocket(int pDesignated) {
        this.designatedPocket = pDesignated;
    }

    public boolean playerHaveToChoosePocket() {
        return this.currentShooter.getPoints() == 7 && this.designatedPocket == -1;
    }

    /**
     * Restituisce il risultato del turno attuale.
     * @return Il risultato del turno attuale
     */
    public Result getTurnResult() {
        return this.turnResult;
    }

    /**
     * Segnala al <code>GameManager</code> che la palla bianca è stata rimessa sul tavolo dopo essere
     * stata imbucata.
     */
    public void whiteBallRepositionated() {
        if (this.turnResult == Result.WHITE_IN_POCKET) {
            this.turnResult = Result.TURN_OVER;
        }
    }

    /**
     * Termina il turno attuale e stabilisce chi sarà il prossimo a colpire.
     */
    public void turnOver() {
        if (this.inTurn) {
            this.strike = false;
            this.inTurn = false;
            this.designatedPocket = -1;
            if (this.numberOfPockets == 0) {
                this.turnResult = Result.TURN_OVER;
            }
            if (this.turnResult == Result.TURN_OVER || this.turnResult == Result.WHITE_IN_POCKET) {
                Shooter aux = currentShooter;
                currentShooter = waitingShooter;
                waitingShooter = aux;
            }
        } else {
            throw new RuntimeException("Turn did not started");
        }
    }
}
