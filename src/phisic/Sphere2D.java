package phisic;

/**
 * La classe <code>Sphere2D</code> modella una sfera nello spazio bidimensionale capace di muoversi e urtare
 * contro altri oggetti.
 * @author Oneiros
 */
public class Sphere2D {

    /** Il diametro delle sfere modellate da questa classe.*/
    public static int BALL_SIZE = 30;
    /** Il vettore posizione della sfera.*/
    public Vector location;
    /** Il vettore velocità della sfera.*/
    public Vector speed;

    /**
     * Crea ed inizializza una sfera in quiete posizionandola nel punto <code>pCenter</code> in un sistema
     * di assi cartesiani.
     * @param pCenter Il centro della sfera
     */
    public Sphere2D(Point pCenter) {
        this.location = new Vector(pCenter);
        this.speed = new Vector(0, 0);
    }

    /**
     * Muove la sfera di uno "step". Per ottenere l'effetto del movimento, il metodo <code>move</code> va
     * richiamato più volte all'interno di un <code>Thread</code>.
     * <p>
     * La direzione e la velocità di movimento sono determinate rispettivamente dall'inclinazione e dal
     * modulo del vettore velocità. Il modulo di quest'ultimo viene inoltre diminuito per effetto dell'attrito
     * (rappresentato dal parametro <code>pFriction</code>) con la superficie sulla quale la sfera sta rotolando.
     * @param pFriction L'attrito generato dal rotolamento della sfera sulla superficie
     */
    public void move(double pFriction) {
        if (this.hasSpeed()) {
            if (this.speed.getMagnitude() < pFriction * 10) {
                this.stop();
            } else {
                this.location.x += this.speed.x;
                this.location.y -= this.speed.y;
                this.speed.x -= pFriction * this.speed.x;
                this.speed.y -= pFriction * this.speed.y;
            }
        }
    }

    /**
     * Determina se la sfera è dotata o meno di velocità
     * @return <code>true</code> se la sfera è dotata di velocità;<br>
     *         <code>false</code> altrimenti.
     */
    public boolean hasSpeed() {
        return this.speed.getMagnitude() > 0;
    }

    /**
     * Ferma il movimento della sfera
     */
    public void stop() {
        this.speed.setMagnitude(0);
    }

    /**
     * Restituisce la distanza tra i centri della sfera d'istanza e della sfera <code>pSphere</code>.
     * @param pSphere La seconda sfera
     * @return La distanza tra i centri delle sfere
     */
    private double distanceFrom(Sphere2D pSphere) {
        return location.distance(pSphere.location);
    }

    /**
     * Determina se la sfera d'istanza e la sfera <code>pSphere</code> si trovano ad una distanza tale da toccarsi.
     * @param pSphere La seconda sfera
     * @return <code>true</code> se le sfere si toccano;<br>
     *         <code>false</code> altrimenti.
     */
    public boolean isTouching(Sphere2D pSphere) {
        return this.distanceFrom(pSphere) < BALL_SIZE;
    }

    /**
     * Gestisce l'urto tra la sfera ed un muro calcolando la nuova direzione di spostamento.
     * @param pWall Il muro sul quale la sfera è entrata in collisione
     */
    public void collideWith(Wall pWall) {
        this.speed.setAngle(2 * pWall.getAngle() - this.speed.getAngularCoord());
    }

    /**
     * Gestisce l'urto tra la sfera d'istanza e la sfera <code>pSphere</code>. Le nuove velocità e le nuove
     * direzioni vengono determinate scambiando le proiezioni dei vettori velocità sulla retta tangente
     * alle due sfere. Il metodo si occupa anche di distanziare opportunamente le due sfere per evitare casi di
     * autocompenetrazione.
     * @param pSphere La sfera con la quale è avvenuta la collisione
     */
    public void collideWith(Sphere2D pSphere) {
        double α = Math.PI / 2 - this.location.getAngularCoordFromPoint(pSphere.location);

        this.speed.rotate(α);
        pSphere.speed.rotate(α);

        double aux = this.speed.y;
        this.speed.y = pSphere.speed.y;
        pSphere.speed.y = aux;

        this.speed.rotate(-α);
        pSphere.speed.rotate(-α);

        Point M = new Point((this.location.x + pSphere.location.x) / 2, (this.location.y + pSphere.location.y) / 2);

        this.location.setDistanceFrom(M, Sphere2D.BALL_SIZE / 2);
        pSphere.location.setDistanceFrom(M, Sphere2D.BALL_SIZE / 2);
    }
}
