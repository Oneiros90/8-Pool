package phisic;

/**
 * La classe <code>Point</code> modella un punto nel piano mediante coordinate {@code (x,y)} a doppia precisione.
 * <br><br>
 * Questa classe inoltre fornisce metodi per eseguire operazioni avanzate come la rotazione
 * e la traslazione del sistema di assi cartesiani.
 *
 * @author Oneiros
 */
public class Point extends java.awt.geom.Point2D.Double {

    /**
     * Crea ed inizializza un punto in coordinate <code>(0,0)</code>.
     */
    public Point() {
    }

    /**
     * Crea ed inizializza un punto in coordinate <code>(pX,pY)</code>.
     * @param pX La coordinata x del punto creato
     * @param pY La coordinata y del punto creato
     */
    public Point(double pX, double pY) {
        super(pX, pY);
    }

    /**
     * Crea ed inizializza un punto a partire delle coordinate intere di un <code>java.awt.Point</code>.
     * @param pPoint Il punto a coordinate intere dal quale costruire un Point
     */
    public Point(java.awt.Point pPoint) {
        super(pPoint.x, pPoint.y);
    }

    /**
     * Effettua una rotazione di ampiezza <code>pAngle</code> del sistema di assi cartesiani nel quale è
     * inserita l'istanza del punto.
     * Questo metodo sostituisce le coordinate del punto ruotato alle coordinate attuali dell'istanza.
     * @param pAngle L'angolo di rotazione degli assi espresso in radianti
     */
    public void changeAxisSystem(double pAngle) {
        double cosα = Math.cos(pAngle);
        double sinα = Math.sin(pAngle);
        double aux = this.x;
        this.x = this.x * cosα - this.y * sinα;
        this.y = aux * sinα + this.y * cosα;
    }

    /**
     * Sposta in <code>pCenter</code> il centro del sistema di assi cartesiani nel quale è inserita
     * l'istanza del punto.
     * Questo metodo sostituisce le coordinate del punto traslato alle coordinate attuali dell'istanza.
     * @param pCenter Il nuovo centro del sistema di assi cartesiani
     */
    public void changeAxisSystem(Point pCenter) {
        this.x -= pCenter.x;
        this.y -= pCenter.y;
    }

    /**
     * Effettua una rototraslazione del sistema di assi cartesiani nel quale è inserita l'istanza del punto.
     * Questo metodo sostituisce le coordinate del punto rototraslato alle coordinate attuali dell'istanza.
     * @param pCenter Il nuovo centro del sistema di assi cartesiani
     * @param pAngle L'angolo di rotazione degli assi espresso in radianti
     */
    public void changeAxisSystem(Point pCenter, double pAngle) {
        this.changeAxisSystem(pCenter);
        this.changeAxisSystem(pAngle);
    }

    /**
     * Restituisce l'angolo ("anomalia" o "ascissa angolare") espresso in radianti formato dall'asse
     * polare e dal raggio vettore, assumendo l'asse polare come origine e positivo il senso antiorario.
     * @return L'ascissa angolare del punto
     */
    public double getAngularCoord() {
        return Math.atan2(this.y, this.x);
    }

    /**
     * Restituisce l'angolo ("anomalia" o "ascissa angolare") espresso in radianti formato dall'asse
     * polare e dal raggio vettore del sistema di assi cartesiani di centro <code>pPoint</code>,
     * assumendo l'asse polare come origine e positivo il senso antiorario.
     * @param pCenter Il centro del sistema di assi cartesiani nel quale calcolare l'ascissa angolare
     * @return L'ascissa angolare del punto nel sistema traslato
     */
    public double getAngularCoordFromPoint(Point pCenter) {
        if (this.equals(pCenter)) {
            throw new IllegalArgumentException("Argument can't be equal to this instance of Point");
        }
        return Math.atan2(this.y - pCenter.y, pCenter.x - this.x);
    }

    /**
     * Sposta l'istanza del punto lungo la retta passante per essa ed il punto <code>pPoint</code>
     * in modo che la distanza tra i due punti sia uguale a <code>pDistance</code>
     * @param pPoint Il punto dal quale distanziare l'istanza del punto
     * @param pDistance La distanza finale tra l'istanza del punto e <code>pPoint</code>
     */
    public void setDistanceFrom(Point pPoint, double pDistance) {
        double α = Math.atan2(this.y - pPoint.y, this.x - pPoint.x) % Math.PI;
        double aux = pDistance;
        while (this.distance(pPoint) < pDistance) {
            this.x = aux * Math.cos(α) + pPoint.x;
            this.y = aux * Math.sin(α) + pPoint.y;
            aux += 0.001;
        }
    }

    /**
     * Restituisce una stringa contenente le coordinate del punto.
     * @return Una stringa del tipo "Point[x, y]"
     */
    @Override
    public String toString() {
        return "Point[" + this.x + ", " + this.y + "]";
    }

    /**
     * Determina se l'istanza del punto ed <code>obj</code> possiedono le stesse coordinate x e y.
     * @param obj un oggetto da comparare con questo punto
     * @return <code>true</code> se obj è una istanza di <code>Point</code> e ha le stesse coordinate;<br>
     *         <code>false</code> altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point secondPoint = (Point) obj;
            return this.x == secondPoint.x && this.y == secondPoint.y;
        }
        return false;
    }
}
