//Agregar segundero
package libreriad;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static libreriad.MuestraImage.cadenaDig;


public class RelojUsuario extends javax.swing.JFrame {

    InetAddress gpo = null;
    MulticastSocket cl;
    private String tiempo;
    public static int segundero = 1000;
    Thread  hilo, canal;
    //Número de reloj que tiene asignado
    private String nr;
    //IP del servidor
    private String ips;
    
    public RelojUsuario() {
        initComponents();
        btnPedir.setVisible(false);
    }
    
    //CÓDIGO
    public static void setTime(String nvoTime, int seg){
        //Nuevo valor de tiempo
        lbr.setText(nvoTime);
        
        //Nuevo valor del segundero
        //segundero = Integer.parseInt(seg);
    }
    //CÓDIGO


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbh = new javax.swing.JLabel();
        lbr = new javax.swing.JLabel();
        btnini = new javax.swing.JButton();
        spn = new javax.swing.JSpinner();
        lb = new javax.swing.JLabel();
        btnPedir = new javax.swing.JButton();
        lbLibro = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setIconImage(getIconImage());

        lbh.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        lbh.setText("Reloj");

        lbr.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        lbr.setText("00:00:00");

        btnini.setText("Iniciar");
        btnini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btniniActionPerformed(evt);
            }
        });

        spn.setModel(new javax.swing.SpinnerNumberModel(0, 0, 3, 1));

        lb.setText("Número de reloj");

        btnPedir.setText("Pedir Libro");
        btnPedir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPedirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lb)
                        .addGap(18, 18, 18)
                        .addComponent(spn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lbh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnini)
                    .addComponent(lbr))
                .addGap(24, 24, 24))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(121, 121, 121)
                        .addComponent(btnPedir))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(lbLibro)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbh)
                    .addComponent(lbr))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb)
                    .addComponent(spn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnini))
                .addGap(26, 26, 26)
                .addComponent(btnPedir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbLibro)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        lbr.setVisible(false);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btniniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btniniActionPerformed
        try{
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
        
            //Numero de cliente
            //String cli="c";
            nr = spn.getValue()+"";
            DatagramPacket envio = new DatagramPacket (("c"+nr).getBytes(), ("c"+nr).getBytes().length, gpo, 4000); 
            //Enviar una solicitud
            cl.send(envio);
            
            
            DatagramPacket p = new DatagramPacket(new byte[20], 20);
            //Se cacha dos veces el datagrama por que le llega lo que envió arriba
            cl.receive(p);
            cl.receive(p);
            //System.out.println("Ira lo que me llegó "+p.getSocketAddress());
            
            //Asignar IP del servidor
            ips = p.getAddress()+"";
            
            //Asignar tiempo y segundero
            String m = new String(p.getData()).substring(0, 8);
            String s = new String(p.getData()).substring(8, 12);
            setTime(m ,1000);
            //System.out.println("Segundero "+s);
            //lbr.setText(m,);
            
            lbr.setVisible(true);
            btnini.setVisible(false);
            spn.setVisible(false);
            lb.setVisible(false);
            lbh.setText("Reloj "+nr);
            
            //Cachar las actualizaciones del server
            canal = new Thread(new Runnable(){
                @Override
                public void run(){
                    while(true){
                        DatagramPacket rec = new DatagramPacket(new byte[20], 20);
                        try {
                            cl.receive(rec);
                            String aux = new String(rec.getData());
                            //Recibir mensajes solo de su numero de reloj
                            if(aux.charAt(0)!='c' && aux.charAt(8)==nr.charAt(0)){
                                setTime(aux.substring(0,8),1000);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(RelojUsuario.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            
            
            //Iniciar el reloj
            hilo = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int seg, min, hor;
                        while(true){
                                tiempo = lbr.getText();

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

                                lbr.setText(cadenaDig(hor)+":"+cadenaDig(min)+":"+cadenaDig(seg));
                                Thread.sleep(segundero);
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

    private void btnPedirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPedirActionPerformed
        try{
            //Creamos el socket
            //System.out.println("LLEGOOOOOOOOO");
            System.out.println("El ip "+ips);
            //OJO AQUÍ 
            Socket cl= new Socket("127.0.0.1",1234);
            
            //Hacemos una petición
            PrintWriter pw =new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
            pw.println("Dame libro >:v");
            pw.flush();     
            
            //Creamos un flujo de caracter ligado al socket para recibir el mensaje
            BufferedReader br2 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            
            //Leemos el mensaje recibido 
            String mensaje= br2.readLine();
            lbLibro.setText("Libro otorgado: "+ mensaje);
            //System.out.println("Recibimos un mensaje desde el servidor");
            //System.out.println("\n\nMensaje: "+mensaje);
            //Cerramos los flujos, el socket y terminamos el programa
            //br2.close();
            //cl.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnPedirActionPerformed

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
    private javax.swing.JLabel lb;
    private javax.swing.JLabel lbLibro;
    private javax.swing.JLabel lbh;
    private static javax.swing.JLabel lbr;
    private javax.swing.JSpinner spn;
    // End of variables declaration//GEN-END:variables
}