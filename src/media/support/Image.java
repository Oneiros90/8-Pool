package media.support;

import java.awt.Dimension;
import java.awt.Point;

/**
 * La classe <code>Image</code> consente di gestire un'immagine in maniera facile e veloce a partire da
 * un file in formato <code>JPG</code>, <code>GIF</code> o <code>PNG.</code>. Le immagini devono essere
 * posizionate nella sottocartella <code>src</code>del progetto.
 * L'immagine verrà inserita all'interno di un pannello trasparente (utilizzabile in contesti grafici
 * <code>Swing</code>) modificabile a proprio piacimento.
 * @author Oneiros
 */
public class Image extends javax.swing.JComponent {

    /** La sottocartella interna ad <code>src</code> dove sono posizionate tutte le immagini */
    public static String imagesDirectory = "/media/images/";
    private java.awt.Image image;
    private Dimension imageSize;
    private Point imageLocation;
    private boolean tile;
    private String imageName;

    /**
     * Crea ed inizializza una Image a partire dal nome di un file immagine contenuto nella cartella
     * <code>src</code> del progetto. Il pannello contenente l'immagine verrà posizionato nel punto
     * <code>(0,0)</code> e ridimensionato con le stesse dimensioni dell'immagine.<br>
     * Sono supportati i seguenti formati: <code>JPG, GIF, PNG.</code>
     * @param pName Il nome del file immagine (estensione compresa)
     */
    public Image(String pName) {
        this.setImage(pName);
        this.setSize(this.imageSize);
        this.imageLocation = new Point(0, 0);
        this.tile = false;
    }

    /**
     * Carica un'immagine dalla cartella <code>src</code>del progetto e la posiziona nel pannello nelle
     * sue dimensioni originali ed in posizione <code>(0,0)</code>.<br>
     * Sono supportati i seguenti formati: <code>JPG, GIF, PNG.</code>
     * @param pName Il nome del file immagine (estensione compresa)
     */
    public final void setImage(String pName) {
        this.imageName = pName;
        this.image = java.awt.Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(Image.imagesDirectory + pName));
        this.imageSize = this.getOriginalImageSize();
        this.repaint();
    }

    /**
     * Imposta le dimensioni dell'immagine. Per impostare le dimensioni del pannello, utilizzare
     * <code>setSize()</code> oppure <code>setBounds()</code>.
     * @param pSize Le dimensioni finali dell'immagine
     */
    public void setImageSize(Dimension pSize) {
        this.imageSize.setSize(pSize);
    }

    /**
     * Imposta le dimensioni dell'immagine. Per impostare le dimensioni del pannello, utilizzare
     * <code>setSize()</code> oppure <code>setBounds()</code>.
     * @param pWidth La larghezza finale dell'immagine
     * @param pHeight L'altezza finale dell'immagine
     */
    public void setImageSize(int pWidth, int pHeight) {
        this.imageSize.setSize(pWidth, pHeight);
    }

    /**
     * Sposta l'immagine all'interno del suo pannello. Per spostare il pannello, utilizzare
     * <code>setLocation()</code>.
     * @param pPoint La posizione finale dell'angolo in alto a sinistra dell'immagine
     */
    public void setImageLocation(Point pPoint) {
        this.imageLocation.setLocation(pPoint);
    }

    /**
     * Sposta l'immagine all'interno del suo pannello. Per spostare il pannello, utilizzare
     * <code>setLocation()</code>.
     * @param pX La coordinata <code>x</code> dell'angolo in alto a sinistra dell'immagine
     * @param pY La coordinata <code>y</code> dell'angolo in alto a sinistra dell'immagine
     */
    public void setImageLocation(int pX, int pY) {
        this.imageLocation.setLocation(pX, pY);
    }

    /**
     * Imposta la modalità "Affianca". La modalità "Affianca" duplica l'immagine in modo da ricoprire
     * completamente il pannello che la contiene.
     * @param pFlag <code>true</code> se si vuole attivare la modalità "Affianca";<br>
     *              <code>false</code> se si vuole disattivare la modalità "Affianca".
     */
    public void setTile(boolean pFlag) {
        this.tile = pFlag;
        this.repaint();
    }

    /**
     * Restituisce l'immagine come istanza di <code>java.awt.Image</code>.
     * @return L'immagine come istanza di java.awt.Image
     */
    public java.awt.Image getImage() {
        return this.image;
    }

    /**
     * Restituisce l'immagine come istanza di <code>javax.swing.ImageIcon</code>.
     * @return L'immagine come istanza di javax.swing.ImageIcon
     */
    public javax.swing.ImageIcon getImageIcon() {
        return new javax.swing.ImageIcon(getClass().getResource(this.getImageFullPath()));
    }

    /**
     * Restituisce le dimensioni attuali dell'immagine. Per le dimensioni del pannello, utilizzare
     * <code>getSize()</code>.
     * @return Le dimensioni dell'immagine
     */
    public Dimension getImageSize() {
        return this.imageSize;
    }

    /**
     * Restituisce la posizione dell'immagine. Per la posizione del pannello, utilizzare
     * <code>getLocation()</code>.
     * @return La posizione dell'angolo in alto a sinistra dell'immagine
     */
    public java.awt.Point getImageLocation(){
        return this.imageLocation;
    }

    /**
     * Restituisce le dimensioni originali dell'immagine rileggendole direttamente dal file.
     * @return Le dimensioni originali dell'immagine
     */
    public Dimension getOriginalImageSize() {
        java.awt.MediaTracker mTracker = new java.awt.MediaTracker(this);
        mTracker.addImage(this.image, 1);
        try {
            mTracker.waitForID(1);
        } catch (InterruptedException ex) {
        }
        return new Dimension(this.image.getWidth(null), this.image.getHeight(null));
    }

    /**
     * Restituisce il nome dell'immagine
     * @return Il nome dell'immagine (estensione compresa).
     */
    public String getImageName() {
        return this.imageName;
    }

    /**
     * Restituisce il path completo dell'immagine.
     * @return Il path completo dell'immagine
     */
    public String getImageFullPath() {
        return Image.imagesDirectory + this.imageName;
    }

    /**
     * Disegna l'immagine sul pannello nella posizione specificata da <code>imageLocation</code>. Se il flag
     * <code>tile</code> è attivo, l'immagine verrà duplicata fino a coprire l'intero pannello.
     * @param g L'oggetto <code>Graphics</code> da disegnare
     */
    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (this.image != null) {
            if (this.tile) {
                int y = 0;
                do {
                    int x = 0;
                    do {
                        g.drawImage(this.image, x, y, this.imageSize.width, this.imageSize.height, this);
                        x += this.imageSize.width;
                    } while (x < this.getWidth());
                    y += this.imageSize.height;
                } while (y < this.getHeight());
            } else {
                g.drawImage(this.image, this.imageLocation.x, this.imageLocation.y, this.imageSize.width, this.imageSize.height, this);
            }
        }
    }

    /**
     * Restituisce una stringa contenente il path completo dell'immagine.
     * @return Una stringa contenente il path completo dell'immagine
     */
    @Override
    public String toString() {
        return "Image[" + this.getImageFullPath() + "]";
    }
}
