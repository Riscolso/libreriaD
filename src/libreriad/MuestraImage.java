//Cerrar los sockets cuando se salga del programa
//Servidor enviar no disponibilidad - Cliente recibir trama
//Puerto para peticiones 1234
//Puerto para replicas 2065
//Puerto para elecciones 2066
//Puerto para latidos 2067
//Puerto para escuchar chismes 2068
//Puerto para BD 2069
//Mostrar portada en los nodos secundarios
//Sincronizar los relojes entre los coordinadores
//Apagar el servidor de libros si no es el coordinador
//Apagar el servidor de listas


//Poner atención en los try-catch de los hiles infinitos (tal vez por eso no funcione bien)
//Yeah

//Replicación
//Qué pasa si lo que responde el secundario no es Chido (Y)




/*
-Variables estaticas a la clase libreriaD
-Actualizar segunderos en los clientes
-Crear relojes independientes
*/



package libreriad;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

public class MuestraImage extends javax.swing.JFrame implements Serializable {
    
    //Número de libros disponibles para distribuir
    public static int noLibros;
    ModReloj mr;
    
    //Multicast - relojes
    public static InetAddress gpo = null;
    
    public static MulticastSocket s;
    public static MulticastSocket s1;
    static DatagramPacket p = null;
    
    //Para base de datos
    public static ConexiónBD con = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306/libreriad");
    public static ConexiónBD con2 = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306");
    
    public static boolean procesando = false;
    //Algoritmo de Berkeley
    AlgoritmoBerkeley ab = new AlgoritmoBerkeley();
    //Relojes
    Reloj r = new Reloj();
    //Algoritmo de anillo
    AlgoritmoAnillo aa = new AlgoritmoAnillo();
    //Replicación primaria
    Replicacion re = new Replicacion();
    
    
    /*----------------------------------Código vista libros--------------------------*/
    public void vistaInfoLibro(){
        try{
            DefaultTableModel t = new DefaultTableModel();
            jInfo.setModel(t);
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection c = con.getConnection();
            
            String SQL="SELECT ISBN, Nombre FROM Libro";
            ps = c.prepareStatement(SQL);
            rs = ps.executeQuery();
            
            ResultSetMetaData rsmd = rs.getMetaData();
            int cantidad = rsmd.getColumnCount();
            
            t.addColumn("ISBN");
            t.addColumn("Nombre");
            while(rs.next()){
                Object[] filas = new Object[cantidad];  
                for(int i=0;i<cantidad;i++){
                    filas[i]=rs.getObject(i+1);
                }
                t.addRow(filas);
            }            
        }catch(SQLException ex){
            System.err.println(ex.toString());
        }
    }
    
    public static void muestraImagen(){
        PreparedStatement s=null;
        ResultSet rs;
        try{
            Connection c = con.getConnection();
            s = c.prepareStatement("SELECT Portada FROM Libro WHERE Nombre=?");
            s.setString(1, jNomLibro.getText());
            rs = s.executeQuery();
            
            BufferedImage bi = null;
            byte[] b = null;
            while(rs.next()){
                b=rs.getBytes("Portada");
                InputStream img = rs.getBinaryStream(1);
                try{
                    bi = ImageIO.read(img);
                    ImageIcon foto = new ImageIcon(bi);
                    Icon icono = new ImageIcon(foto.getImage().getScaledInstance(jMuestraLibro.getWidth(),jMuestraLibro.getHeight(),Image.SCALE_DEFAULT));
                    jMuestraLibro.setIcon(icono);
                }catch(IOException ex){
                    System.err.println(ex);
                }
            }
        }catch(SQLException ex){
            System.err.println(ex.toString());
        }
    }
    
    /*----------------------------------Constructor-----------------------------*/
    public MuestraImage() {
        initComponents();
        setLocationRelativeTo(this);
        btnReIni.setVisible(true);
        mr = new ModReloj();
        
        //Vista para mostrar info del libro 
        vistaInfoLibro();
        //Código para los relojes
        r.iniciarRelojes();
        
        
        try {
            //Saber cuál es el nombre de la máquina
            //Suponiendo que todos tienen un número de nombre xD
            String aux = InetAddress.getLocalHost().getHostName()+"";
            aa.name = Integer.parseInt(aux.charAt(1)+"");
            System.out.println("Mi nombre "+aa.name);
        } catch (UnknownHostException ex) {
            System.out.println("Error al obtener mi nombre... Who am i?: "+ ex);
        }
        //Hilo que se encarga de todo el algoritmo del anillo
        aa.eleccion();
        
        //Hilo para que informe si esta vivo el nodo
        aa.vivo();
        
        //Servidor de las peticiones de libro
        re.serverReplica();
        
        //Cargar los siguientes de una archivo
        //Si esta vacío es la primera vez que aparece el nodo
        aa.namae = aa.cargarLista();
        
        //Iniciar servidor de relojes
        r.servidorRelojes();
        
        //Iniciar el canal de comunicación que envía cuando hay nuevo coordinador a los FE
        aa.iniciarMulti();
        
        //Hilo para enviar BD
        re.enviarBD();
        
        //Si no hay siguientes
        if(aa.namae.size() == 0){
            //Si es el primerísimo de todos 
            if(aa.name == 1){
                System.out.println("Estoy solo en el mundo\nHello darkness my old friend");
                aa.elsujeto = aa.name;

                //Código para las peticiones
                btnReIni.setVisible(true);
                
                //Generar sql de la bd
                con.respaldarBD();
                
                //Escuchar cada que hay una replica
                re.serverReplica();

                aa.namae.add(2);
            }
            //Si es un nodo nuevo y es secundario
            else{
                //El nodo 1 siempre debe estar encendido cuando se inicia un nodo nuevo
                aa.namae.add(1);
                aa.timeToDuel();
                aa.beat();
                //Escuchar cada que hay una replica
                re.serverReplica();
                //Escuchar a los FE cuando se caíga el principal
                aa.serverChismes();
                
                re.pedirBD();
            }
        }
        //Si esta reviviendo de sus cenizas cual ave fénix
        else if(aa.name!=1){
            aa.timeToDuel();
            aa.beat();
            //Escuchar cada que hay una replica, el primario nunca lo va a iniciar
            re.serverReplica();
            //Escuchar a los FE cuando se caíga el principal
            aa.serverChismes();
            re.pedirBD();
        }
        //Y ya si es el coordinador principal el que revive
        else{
            //Escuchar cada que hay una replica
            re.serverReplica();
            aa.timeToDuel();
            re.pedirBD();
        }
        noLibros=con.obtenerLibros();
        lbLibros.setText("Libros disponibles: "+ noLibros);
        //Activar el AlgoritmoBerkeley
        AlgoritmoBerkeley.presentarse();
        AlgoritmoBerkeley.hiloEscuchaHora();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jInfo = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jIdLibro = new javax.swing.JTextField();
        jMuestraLibro = new javax.swing.JLabel();
        jSalir = new javax.swing.JButton();
        jMuestra = new javax.swing.JButton();
        r1 = new javax.swing.JButton();
        r2 = new javax.swing.JButton();
        r3 = new javax.swing.JButton();
        r4 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnReIni = new javax.swing.JButton();
        lbLibros = new javax.swing.JLabel();
        jNomLibro = new javax.swing.JTextField();
        btncor = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 153, 204));

        jInfo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "ISBN", "Título"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jInfo);

        jLabel1.setText("Inf. Libro:");

        jSalir.setText("Salir");
        jSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSalirActionPerformed(evt);
            }
        });

        jMuestra.setText("Mostrar Portada");
        jMuestra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMuestraActionPerformed(evt);
            }
        });

        r1.setBackground(new java.awt.Color(255, 255, 255));
        r1.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        r1.setText("00:00:00");
        r1.setToolTipText("");
        r1.setAutoscrolls(true);
        r1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        r1.setBorderPainted(false);
        r1.setContentAreaFilled(false);
        r1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        r1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                r1ActionPerformed(evt);
            }
        });

        r2.setBackground(new java.awt.Color(255, 255, 255));
        r2.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        r2.setText("00:00:00");
        r2.setBorderPainted(false);
        r2.setContentAreaFilled(false);
        r2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        r2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                r2ActionPerformed(evt);
            }
        });

        r3.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        r3.setText("00:00:00");
        r3.setBorderPainted(false);
        r3.setContentAreaFilled(false);
        r3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        r3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                r3ActionPerformed(evt);
            }
        });

        r4.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        r4.setText("00:00:00");
        r4.setBorderPainted(false);
        r4.setContentAreaFilled(false);
        r4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        r4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                r4ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Dialog", 2, 14)); // NOI18N
        jLabel2.setText("Local");

        btnReIni.setText("Reiniciar sesión");
        btnReIni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReIniActionPerformed(evt);
            }
        });

        btncor.setText("Coordinador");
        btncor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btncorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(47, 47, 47)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel2)
                                                    .addComponent(r1))
                                                .addGap(46, 46, 46))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jIdLibro, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(90, 90, 90))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGap(61, 61, 61)
                                        .addComponent(r3)
                                        .addGap(28, 28, 28)))
                                .addComponent(jMuestraLibro, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(106, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(r4)
                            .addComponent(r2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jNomLibro, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jMuestra)
                                .addGap(18, 18, 18)
                                .addComponent(btncor)
                                .addGap(4, 4, 4))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(lbLibros, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnReIni)
                        .addGap(132, 132, 132)
                        .addComponent(jSalir)))
                .addGap(24, 24, 24))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jIdLibro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMuestra)
                    .addComponent(jNomLibro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btncor))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jMuestraLibro, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(r2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(r4)
                                .addGap(59, 59, 59)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnReIni)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jSalir)
                                .addComponent(lbLibros, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(27, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(r1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(r3)
                        .addGap(113, 113, 113))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSalirActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jSalirActionPerformed

    private void jMuestraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMuestraActionPerformed
        PreparedStatement s=null;
        ResultSet rs;
        
        try{
            Connection c = con.getConnection();
            s = c.prepareStatement("SELECT Portada FROM Libro WHERE ISBN=?");
            s.setInt(1, Integer.parseInt(jIdLibro.getText()));
            rs = s.executeQuery();
            
            BufferedImage bi = null;
            byte[] b = null;
            while(rs.next()){
                b=rs.getBytes("Portada");
                InputStream img = rs.getBinaryStream(1);
                try{
                    bi = ImageIO.read(img);
                    ImageIcon foto = new ImageIcon(bi);
                    Icon icono = new ImageIcon(foto.getImage().getScaledInstance(jMuestraLibro.getWidth(),jMuestraLibro.getHeight(),Image.SCALE_DEFAULT));
                    jMuestraLibro.setIcon(icono);
                }catch(IOException ex){
                    System.err.println(ex);
                }
            }
        }catch(SQLException ex){
            System.err.println(ex.toString());
        }
    }//GEN-LAST:event_jMuestraActionPerformed

    private void r1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r1ActionPerformed
        //Pausar el hilo
        //El método suspend esta depreciado xD
        r.on[0] = false;
        //Mandar tiempo y no de reloj al otr frame
        mr.setTimeM(r.tiempo[0], 0, r.segundero[0]);
        mr.setVisible(true);
    }//GEN-LAST:event_r1ActionPerformed

    private void r2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r2ActionPerformed
        r.on[1] = false;
        mr.setTimeM(r.tiempo[1], 1, r.segundero[1]);
        mr.setVisible(true);
    }//GEN-LAST:event_r2ActionPerformed

    private void r3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r3ActionPerformed
        r.on[2] = false;
        mr.setTimeM(r.tiempo[2], 2, r.segundero[2]);
        mr.setVisible(true);
    }//GEN-LAST:event_r3ActionPerformed

    private void r4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r4ActionPerformed
        r.on[3] = false;
        mr.setTimeM(r.tiempo[3], 3, r.segundero[3]);
        mr.setVisible(true);
    }//GEN-LAST:event_r4ActionPerformed
    
    
    
    private void btnReIniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReIniActionPerformed
        
        //Modificar la disponibilidad de manera local
        con.modifDispo();
        noLibros=con.obtenerLibros();
        lbLibros.setText("Libros disponibles: "+ noLibros);
        re.replicacion(new Peticion("-1"));
        /*try {
            //Modificar la disponibilidad de manera remota
            //if(!stub.reiniciarSesion()) System.out.println("Explotó algo EN el servidor remoto");
        } catch (RemoteException ex) {
            System.out.println("Explotó al enviar el servidor remoto D:");
        }*/
    }//GEN-LAST:event_btnReIniActionPerformed

    private void btncorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncorActionPerformed
        System.out.println("El coordinador es: "+aa.elsujeto);
        System.out.println("Mi tabla de siguientes es ");
        for(int a : aa.namae){
            System.out.print(a+" ");
        }
        System.out.println("");
    }//GEN-LAST:event_btncorActionPerformed
    
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
            java.util.logging.Logger.getLogger(MuestraImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MuestraImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MuestraImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MuestraImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MuestraImage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnReIni;
    private javax.swing.JButton btncor;
    private javax.swing.JTextField jIdLibro;
    private javax.swing.JTable jInfo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton jMuestra;
    public static javax.swing.JLabel jMuestraLibro;
    public static javax.swing.JTextField jNomLibro;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jSalir;
    private javax.swing.JScrollPane jScrollPane1;
    public static javax.swing.JLabel lbLibros;
    public static javax.swing.JButton r1;
    public static javax.swing.JButton r2;
    public static javax.swing.JButton r3;
    public static javax.swing.JButton r4;
    // End of variables declaration//GEN-END:variables
}
