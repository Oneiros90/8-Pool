package pool;

/**
 * La classe <code>PoolFrame</code> prepara un frame adatto per il gioco.
 * @author Oneiros
 */
public class PoolFrame extends javax.swing.JFrame {

    /** Le dimensioni del frame (senza bordi) in modalità finestra*/
    protected static final java.awt.Dimension FRAME_DIMENSION = new java.awt.Dimension(1000, 640);
    private PoolGame pool;
    private boolean fullScreen = false;

    /**
     * Crea ed inizializza il frame di gioco
     * @param pFullScreen <code>true</code> per la modalità a schermo intero;
     *                    <code>false</code> per la modalità a finestra.
     */
    public PoolFrame(boolean pFullScreen) {
        this.getContentPane().setBackground(java.awt.Color.BLACK);
        this.setIconImage(new media.support.Image("ball8.png").getImage());
        this.setTitle("8-Pool by Oneiros");
        this.setDefaultLookAndFeel();
        this.initComponents();
        this.fullScreen = false;
        if (pFullScreen) {
            this.setFullscreen();
        } else {
            this.smartSetSize(FRAME_DIMENSION);
            this.centerFrame();
        }
        this.startGame();
    }

    /**
     * Avvia il gioco vero e proprio. Ogni volta che viene chiamato questo metodo viene automaticamente
     * eliminata la partita precedente
     */
    protected final void startGame() {
        if (this.pool != null) {
            (this.pool).setVisible(false);
            System.gc();
        }
        this.pool = new PoolGame(this);
        this.add(this.pool);
        this.repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));
        setResizable(false);
        getContentPane().setLayout(null);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void centerFrame() {
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int) (screenSize.getWidth() - this.getWidth()) / 2, (int) (screenSize.getHeight() - this.getHeight()) / 2);
    }

    private void smartSetSize(int pWidth, int pHeight) {
        this.setSize(this.getWidth() - this.getContentPane().getWidth() + pWidth, this.getHeight() - this.getContentPane().getHeight() + pHeight);
        this.getContentPane().setSize(pWidth, pHeight);
    }

    private final void smartSetSize(java.awt.Dimension pSize) {
        this.smartSetSize(pSize.width, pSize.height);
    }

    private void setDefaultLookAndFeel() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            System.out.println(ex);
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        } catch (InstantiationException ex) {
            System.out.println(ex);
        } catch (IllegalAccessException ex) {
            System.out.println(ex);
        }
    }

    private void setFullscreen() {
        this.dispose();
        if (!this.fullScreen) {
            this.fullScreen = true;
            this.setUndecorated(true);
            java.awt.GraphicsDevice myDevice = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            myDevice.setFullScreenWindow(PoolFrame.getWindows()[1]);
            java.awt.DisplayMode originalDM = myDevice.getDisplayMode();
            this.setSize(originalDM.getWidth(), originalDM.getHeight());
        } else {
            this.setUndecorated(false);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
