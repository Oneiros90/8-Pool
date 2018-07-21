package pool;

/**
 * La classe <code>SplashScreen</code> gestisce l'apparizione dello splashscreen iniziale del gioco.
 * @author Oneiros
 */
public class SplashScreen extends javax.swing.JFrame {

    /**
     * Crea, inizializza e mostra lo splashscreen di gioco.
     */
    public SplashScreen() {
        this.setDefaultLookAndFeel();
        this.initComponents();
        media.support.Image splashScreenImage = new media.support.Image("splashScreen4.jpg");
        splashScreenImage.setVisible(true);
        this.add(splashScreenImage);
        this.setSize(splashScreenImage.getSize());
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int) (screenSize.getWidth() - this.getWidth()) / 2, (int) (screenSize.getHeight() - this.getHeight()) / 2);
        new Thread() {

            @Override
            public void run() {
                setVisible(true);
                if (com.sun.awt.AWTUtilities.isTranslucencySupported(com.sun.awt.AWTUtilities.Translucency.TRANSLUCENT)) {
                    try {
                        for (float f = 0; f < 1; f += 0.01) {
                            com.sun.awt.AWTUtilities.setWindowOpacity(getWindows()[0], f);
                            sleep(20);
                        }
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }.start();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        answerLabel = new javax.swing.JLabel();
        yesLabel = new javax.swing.JLabel();
        noLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(false);
        setUndecorated(true);
        getContentPane().setLayout(null);

        titleLabel.setFont(new java.awt.Font("Castellar", 0, 30)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(255, 255, 255));
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setText("<html><center>8-Pool<br>by Oneiros");
        titleLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        titleLabel.setName("titleLabel"); // NOI18N
        getContentPane().add(titleLabel);
        titleLabel.setBounds(10, 11, 260, 74);

        answerLabel.setFont(new java.awt.Font("Castellar", 0, 18)); // NOI18N
        answerLabel.setForeground(new java.awt.Color(255, 255, 255));
        answerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        answerLabel.setText("<html><center>Fullscreen?");
        answerLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        answerLabel.setName("answerLabel"); // NOI18N
        getContentPane().add(answerLabel);
        answerLabel.setBounds(10, 113, 260, 22);

        yesLabel.setFont(new java.awt.Font("Castellar", 0, 18)); // NOI18N
        yesLabel.setForeground(new java.awt.Color(255, 255, 255));
        yesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        yesLabel.setText("YES");
        yesLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        yesLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        yesLabel.setName("yesLabel"); // NOI18N
        yesLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                yesLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                yesLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                yesLabelMouseExited(evt);
            }
        });
        getContentPane().add(yesLabel);
        yesLabel.setBounds(69, 147, 57, 22);

        noLabel.setFont(new java.awt.Font("Castellar", 0, 18)); // NOI18N
        noLabel.setForeground(new java.awt.Color(255, 255, 255));
        noLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noLabel.setText("NO");
        noLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        noLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        noLabel.setName("noLabel"); // NOI18N
        noLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                noLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                noLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                noLabelMouseExited(evt);
            }
        });
        getContentPane().add(noLabel);
        noLabel.setBounds(144, 147, 57, 22);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void yesLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yesLabelMouseEntered
        yesLabel.setForeground(PoolGame.GOLD);
    }//GEN-LAST:event_yesLabelMouseEntered

    private void yesLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yesLabelMouseExited
        yesLabel.setForeground(java.awt.Color.white);
    }//GEN-LAST:event_yesLabelMouseExited

    private void noLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noLabelMouseEntered
        noLabel.setForeground(PoolGame.GOLD);
    }//GEN-LAST:event_noLabelMouseEntered

    private void noLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noLabelMouseExited
        noLabel.setForeground(java.awt.Color.white);
    }//GEN-LAST:event_noLabelMouseExited

    private void yesLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yesLabelMouseClicked
        this.dispose();
        new PoolFrame(true).setVisible(true);
    }//GEN-LAST:event_yesLabelMouseClicked

    private void noLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noLabelMouseClicked
        this.dispose();
        new PoolFrame(false).setVisible(true);
    }//GEN-LAST:event_noLabelMouseClicked

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new SplashScreen();
            }
        });
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel answerLabel;
    private javax.swing.JLabel noLabel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel yesLabel;
    // End of variables declaration//GEN-END:variables
}
