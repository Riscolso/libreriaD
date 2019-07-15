/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libreriad;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
* <h1>Servidor de tiempo</h1>
* Se encarga de sincronizar el reloj entre los nodos del sistema distribuido
* @author  Equipo 7 RULEZ
* @version 1.0
* @since   2019-04-05
*/
public class TimeServer extends javax.swing.JFrame {
    /**
     * Todo lo relacionado con el algoritmo
     */
    AlgoritmoBerkeley ab;
    
    ModRelojS mr;

    /**
     * Reloj corriendo en el botón
     */
    Reloj r;
    
    public TimeServer() {
        initComponents();
        this.setTitle("Servidor de Tiempo");
        //Establecer la hora actual
        Date date = new Date();
        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
        btnr.setText(hourFormat.format(date));
        
        //Instanciar el reloj
        r = new Reloj(btnr, 1000);
        //Instanciar el modificardor de reloj
        mr = new ModRelojS();
        //Vincular el modificador de reloj con el reloj; ya sé que es mucha vuelta, pero así permite la herencia múltiple xD
        mr.setReloj(r);
        
        //Iniciar reloj
        r.reloj();
        
        ab = new AlgoritmoBerkeley();
        ab.hiloEscuchaEquipos();
        ab.berkeley(btnr);
    }
    
    public static void setTime(String t){
        btnr.setText(t);
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        btnr = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jPanel2.setBackground(new java.awt.Color(51, 0, 102));
        jPanel2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        btnr.setBackground(new java.awt.Color(255, 255, 255));
        btnr.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        btnr.setForeground(new java.awt.Color(255, 255, 255));
        btnr.setText("00:00:00");
        btnr.setAutoscrolls(true);
        btnr.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        btnr.setBorderPainted(false);
        btnr.setContentAreaFilled(false);
        btnr.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnrActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(98, 98, 98)
                .addComponent(btnr, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(92, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(btnr, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnrActionPerformed
        mr.llenarCampos();
        //Pausar el reloj (ZA WARUDO!!!!!)
        r.on = false;
        mr.setVisible(true);
    }//GEN-LAST:event_btnrActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TimeServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TimeServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TimeServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TimeServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TimeServer().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JButton btnr;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables
}
