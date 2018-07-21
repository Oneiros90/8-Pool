package phisic;

/**
 * La classe <code>Wall</code> modella un muro, un ostacolo sul quale una sfera può sbattere e rimbalzare
 * @author Oneiros
 */
public class Wall extends java.awt.geom.Line2D.Double {

    /**
     * Crea ed inizializza un muro a partire dai suoi punti estremali. L'ordine con il quale vengono
     * passati i due parametri <code>A</code> e <code>B</code> rappresenta una terza informazione
     * implicita che il costruttore interpreta per determinare se il muro vero e proprio si trova da una
     * parte o dall'altra del segmento che congiunge i due punti estremali. In generale, se si fa ruotare
     * il punto <code>B</code> attorno al punto <code>A</code> in senso antiorario lungo una circonferenza,
     * si avrà che la "parte fisica" del muro segue il segmento AB.
     *
     * <br><br>
     * Esempio:<br>
     * <code>
     * Point point1 = new Point(0, 10);<br>
     * Point point2 = new Point(10, 0);<br>
     * Wall wall1 = new Wall(point1, point2);<br>
     * Wall wall2 = new Wall(point2, point1);</code>
     * <br><br>
     * I due muri hanno gli stessi punti estremali ma il muro <code>wall1</code> si trova al di sotto del
     * segmento che congiunge i due punti mentre <code>wall2</code> si trova al di sopra.
     * @param pA Primo punto estremale del muro
     * @param pB Secondo punto estremale del muro
     */
    public Wall(Point pA, Point pB) {
        super(pA, pB);
    }

    /**
     * Restituisce la distanza minima tra il muro e la sfera <code>pBall</code>.
     * @param pBall La sfera dalla quale si vuole calcolare la distanza dal muro
     * @return La distanza minima tra il muro e la sfera <code>pBall</code>.
     */
    public double distanceFrom(Sphere2D pBall) {
        return super.ptSegDist(pBall.location);
    }

    /**
     * Determina se la sfera <code>pBall</code> si trova ad una distanza dal muro tale da toccarlo.
     * @param pBall La sfera che si vuole verificare
     * @return <code>true</code> se la sfera sta toccando il muro;<br>
     *         <code>false</code> altrimenti.
     */
    public boolean isTouching(Sphere2D pBall) {
        return this.distanceFrom(pBall) < Sphere2D.BALL_SIZE / 2 || this.getPenetration(pBall) > 0;
    }

    /**
     * Restituisce di quanto la sfera <code>pBall</code> è penetrata all'interno del muro.
     * @param pBall La sfera penetrata nel muro
     * @return La quantità di penetrazione
     */
    public double getPenetration(Sphere2D pBall) {
        double α = this.getAngle();
        Point a = new Point(x1, y1);
        Point b = new Point(x2, y2);
        Point c = new Point(pBall.location.x, pBall.location.y);

        a.changeAxisSystem(α);
        b.changeAxisSystem(α);
        c.changeAxisSystem(α);

        if (c.x > a.x && c.x < b.x && c.y >= (a.y - Sphere2D.BALL_SIZE / 2)) {
            return Math.abs(c.y - (a.y - Sphere2D.BALL_SIZE / 2));
        } else {
            return 0;
        }
    }

    /**
     * Distanzia opportunamente la sfera <code>pBall</code> in modo da eliminare la penetrazione
     * @param pBall La sfera penetrata nel muro
     */
    public void moveAway(Sphere2D pBall) {
        double α = this.getAngle();
        Point a = new Point(this.x1, this.y1);
        Point b = new Point(this.x2, this.y2);
        Point c = new Point(pBall.location.x, pBall.location.y);

        a.changeAxisSystem(α);
        b.changeAxisSystem(α);
        c.changeAxisSystem(α);

        double aux = Sphere2D.BALL_SIZE / 2;
        while (this.isTouching(pBall)) {
            c.y = a.y - aux;
            b.setLocation(c);
            b.changeAxisSystem(-α);
            pBall.location.setLocation(b);
            aux += 0.001;
        }
    }

    /**
     * Restituisce l'angolo espresso in radianti formato dall'asse delle ascisse del sistema di assi cartesiano
     * e dalla retta passante per i due punti estremali del muro, assumendo l'asse x come origine
     * e positivo il senso antiorario.
     * @return L'angolo che forma il muro con l'asse delle ascisse
     */
    public double getAngle() {
        return new Point(this.x1, this.y1).getAngularCoordFromPoint(new Point(this.x2, this.y2));
    }
}
