package phisic;

/**
 * La classe <code>Vector</code> modella un vettore nello spazio vettoriale.
 * @author Oneiros
 */
public class Vector extends Point {

    /**
     * Crea ed inizializza un vettore in coordinate <code>(0,0)</code>.
     */
    public Vector() {
    }

    /**
     * Crea ed inizializza un vettore in coordinate <code>(pX,pY)</code>.
     * @param pPoint La posizione del vettore
     */
    public Vector(Point pPoint) {
        super(pPoint.x, pPoint.y);
    }

    /**
     * Crea ed inizializza un vettore di modulo <code>pMagnitude</code> ed inclinazione <code>pAngle</code>
     * @param pMagnitude Il modulo del vettore creato
     * @param pAngle L'inclinazione del vettore creato
     */
    public Vector(double pMagnitude, double pAngle) {
        this.setPolarChoords(pMagnitude, pAngle);
    }

    /**
     * Modifica il modulo del vettore senza modificarne l'inclinazione
     * @param pMagnitude Il nuovo modulo del vettore
     */
    public void setMagnitude(double pMagnitude) {
        double α = this.getAngularCoord();
        this.x = pMagnitude * Math.cos(α);
        this.y = pMagnitude * Math.sin(α);
    }

    /**
     * Modifica l'inclinazione del vettore senza modificarne il modulo
     * @param pAngle La nuova inclinazione del vettore
     */
    public void setAngle(double pAngle) {
        this.rotate(pAngle - this.getAngularCoord());
    }

    /**
     * Imposta il vettore in modo che il suo modulo sia <code>pMagnitude</code> e
     * la sua inclinazione sia <code>pAngle</code>
     * @param pMagnitude Il nuovo modulo del vettore
     * @param pAngle La nuova inclinazione del vettore
     */
    public final void setPolarChoords(double pMagnitude, double pAngle) {
        super.setLocation(pMagnitude * Math.cos(pAngle), pMagnitude * Math.sin(pAngle));
    }

    /**
     * Effettua una rotazione di ampiezza <code>pAngle</code> del vettore intorno al suo punto di applicazione.
     * @param pAngle L'angolo di rotazione del vettore
     */
    public void rotate(double pAngle) {
        super.changeAxisSystem(pAngle);
    }

    /**
     * Restituisce il modulo del vettore
     * @return Il modulo del vettore
     */
    public double getMagnitude() {
        return super.distance(0, 0);
    }

    /**
     * Effettua la somma vettoriale tra il vettore d'istanza ed il vettore <code>pVector</code> e restituisce
     * il vettore risultante
     * @param pVector Vettore addendo
     * @return Vettore somma
     */
    public Vector sum(Vector pVector) {
        return new Vector(new Point(this.x + pVector.x, this.y + pVector.y));
    }
}
