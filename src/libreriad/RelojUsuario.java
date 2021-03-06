//Agregar segundero
package libreriad;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static libreriad.Reloj.cadenaDig;


public class RelojUsuario extends javax.swing.JFrame {
    FrontEnd fe;
    InetAddress gpo = null;
    MulticastSocket cl;
    private String tiempo;
    public static int segundero = 1000;
    Thread  hilo, canal;
    //Número de reloj que tiene asignado
    private String nr;
    //IP del servidor
    
    
    /*--------------------------Constructor--------------------*/
    public RelojUsuario() {
        initComponents();
        this.setTitle("Usuario");
        setLocationRelativeTo(this);
        btnPedir.setVisible(false);
        fe = new FrontEnd();
        //Obtener el número del coordiandor
        try {
            if(fe.buscarElSujeto() == -1) System.out.println("No hay coordinadores activos ");
            else System.out.println("El coordinador principal es " +fe.elsujeto);
        } catch (UnknownHostException ex) {
            System.out.println("Error al buscar al coordinador "+ex);
        }
        try {
            //Iniciar el servidor que escucha el nuevo coordinador
            fe.servidorCoordinador();
        } catch (IOException ex) {
            Logger.getLogger(RelojUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            cl = new MulticastSocket(4000);
        
            System.out.println("Cliente escuchando puerto "+cl.getLocalPort());
            cl.setReuseAddress(true);
            try{
                gpo=InetAddress.getByName("228.1.1.1"); //Puede entrar dentro de un rango no válido
            }catch(UnknownHostException u){
                System.err.println("Dirección no válida");
            }
            cl.joinGroup(gpo);
            System.out.println("Unido al grupo");
         } catch (IOException ex) {
            Logger.getLogger(RelojUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Cachar las actualizaciones del server
            canal = new Thread(new Runnable(){
                @Override
                public void run(){
                    DatagramPacket rec = new DatagramPacket(new byte[20], 20);
                    while(true){
                        try {
                            cl.receive(rec);
                            System.out.println("Llega "+new String(rec.getData()));
                            String aux = new String(rec.getData());
                            //Recibir mensajes solo de su numero de reloj
                            if(aux.charAt(0)!='c' && aux.charAt(8)==nr.charAt(0)){
                                lbr.setText(aux.substring(0,8));
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(RelojUsuario.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
    }
    
    
    public static void setTime(String nvoTime, int seg){
        //Nuevo valor de tiempo
        lbr.setText(nvoTime);
        
        //Nuevo valor del segundero
        //segundero = Integer.parseInt(seg);
    }
    


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lbh = new javax.swing.JLabel();
        lbr = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnini = new javax.swing.JButton();
        lbLibro = new javax.swing.JLabel();
        btnPedir = new javax.swing.JButton();
        lb = new javax.swing.JLabel();
        spn = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setIconImage(getIconImage());

        jPanel1.setBackground(new java.awt.Color(102, 102, 255));

        lbh.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        lbh.setForeground(new java.awt.Color(0, 0, 0));
        lbh.setText("Reloj");

        lbr.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        lbr.setForeground(new java.awt.Color(0, 0, 0));
        lbr.setText("00:00:00");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(lbh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbr)
                .addGap(21, 21, 21))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(lbh))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addComponent(lbr)
                .addContainerGap())
        );

        lbr.setVisible(false);

        jPanel2.setBackground(new java.awt.Color(51, 0, 102));

        btnini.setBackground(new java.awt.Color(255, 255, 255));
        btnini.setForeground(new java.awt.Color(0, 0, 0));
        btnini.setText("Iniciar");
        btnini.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btniniActionPerformed(evt);
            }
        });

        lbLibro.setForeground(new java.awt.Color(255, 255, 255));

        btnPedir.setBackground(new java.awt.Color(255, 255, 255));
        btnPedir.setForeground(new java.awt.Color(0, 0, 0));
        btnPedir.setText("Pedir Libro");
        btnPedir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPedir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPedirActionPerformed(evt);
            }
        });

        lb.setForeground(new java.awt.Color(255, 255, 255));
        lb.setText("Número de reloj");

        spn.setModel(new javax.swing.SpinnerNumberModel(0, 0, 3, 1));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbLibro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnPedir)
                .addGap(103, 103, 103))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(lb)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(spn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 116, Short.MAX_VALUE)
                .addComponent(btnini)
                .addGap(35, 35, 35))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(35, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb)
                    .addComponent(spn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnini))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbLibro, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPedir)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPedirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPedirActionPerformed
        try {
            lbLibro.setText("Libro otorgado: "+ fe.peticion());
        } catch (IOException ex) {
            System.out.println("Error en la petición "+ex);
        }
    }//GEN-LAST:event_btnPedirActionPerformed

    private void btniniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btniniActionPerformed
        try{

            //Numero de cliente
            //String cli="c";
            nr = spn.getValue()+"";
            DatagramPacket envio = new DatagramPacket (("c"+nr).getBytes(), ("c"+nr).getBytes().length, gpo, 9875);
            //Enviar una solicitud
            cl.send(envio);

            lbr.setVisible(true);
            btnini.setVisible(false);
            spn.setVisible(false);
            lb.setVisible(false);
            lbh.setText("Reloj "+nr);

            //Iniciar el reloj
            hilo = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int seg, min, hor;
                        while(true){
                            Thread.sleep(segundero);
                            tiempo = lbr.getText();
                            String aux = tiempo;

                            seg = Integer.parseInt(tiempo.substring(tiempo.lastIndexOf(":")+1));
                            min = Integer.parseInt(tiempo.substring(tiempo.indexOf(":")+1,tiempo.lastIndexOf(":")));
                            hor = Integer.parseInt(tiempo.substring(0,2));
                            if(seg <59) seg++;
                            else{
                                seg=0;
                                if(min<59) min++;
                                else{
                                    min=0;
                                    if(hor<23) hor++;
                                    else hor=0;
                                }
                            }
                            if(lbr.getText().equals(aux)){
                                lbr.setText(cadenaDig(hor)+":"+cadenaDig(min)+":"+cadenaDig(seg));
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("Error en hilo: "+ex);
                    }
                }
            });
            hilo.start();
            canal.start();

            btnPedir.setVisible(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_btniniActionPerformed

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
            java.util.logging.Logger.getLogger(RelojUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RelojUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RelojUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RelojUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RelojUsuario().setVisible(true);
            }
        });
    }
    @Override
    public Image getIconImage() {
        Image retValue = Toolkit.getDefaultToolkit().
                getImage(ClassLoader.getSystemResource("resources/rel.png"));


        return retValue;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPedir;
    private javax.swing.JButton btnini;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lb;
    public static javax.swing.JLabel lbLibro;
    private javax.swing.JLabel lbh;
    private static javax.swing.JLabel lbr;
    private javax.swing.JSpinner spn;
    // End of variables declaration//GEN-END:variables
}
