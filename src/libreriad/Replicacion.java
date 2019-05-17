package libreriad;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static libreriad.MuestraImage.*;
import static libreriad.AlgoritmoAnillo.namae;
import static libreriad.AlgoritmoAnillo.MAQ;
import static libreriad.AlgoritmoAnillo.stillAlive;

/**
 * Se hace uso de la replicación primaria
 * @author Ricardo RC
 */
public class Replicacion {
    /**
     * Indica si este equipo es el coordinador.
     */
    boolean primario = true;
    
    /**
     * Hilo encargado de esperar peticiones de DB, genera un SQL y lo envía
     */
    public void enviarBD(){
        Thread t = new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    ServerSocket s = new ServerSocket(2069);
                    while(true){
                        Socket cl = s.accept();
                        con.respaldarBD();
                        String ar = "respaldito.sql";
                        File f = new File(ar);
                        long tam = f.length(); //Tamaño
                        DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                        //Flujo de datos orientados a byte para la lectura de los archivos
                        DataInputStream dis = new DataInputStream(new FileInputStream(ar));
                        //Envio de datos henerales del archivo por el socket
                        dos.writeUTF(ar);
                        dos.flush();
                        dos.writeLong(tam);
                        dos.flush();
                        //Leer los datos contenidos en el archivo en paquetes de 1024  y los enviamos por el socket
                        byte[] b = new byte[1024];
                        long enviados =0;
                        //int porcentaje, 
                        int n;
                        while(enviados<tam){
                            n = dis.read(b);
                            //System.out.println("Mira b"+b);
                            dos.write(b,0,n);
                            dos.flush();
                            enviados += n;
                            //porcentaje = (int)(enviados*100/tam);
                            //System.out.println("Enviado: "+porcentaje+"%\r");
                        }
                        System.out.println("\n\nArchivo enviado");
                        dos.close();
                        dis.close();
                        cl.close();
                    }
                } catch (IOException ex) {
                    System.out.println("Error en enviar BD "+ex);
                } 
            }
        });
        t.start();
    }

    /**
     * Pide un sql a los siguientes de la lista.
     * Si el siguiente directo no está diponible lo salta
     */
    public void pedirBD(){
        try {
            for(int e=0;e<namae.size();e++){
                //comprobar que esta vivo,si no lo toma como muerto y envia al siguiente
                if(stillAlive(InetAddress.getByName(MAQ+namae.get(e)))){
                    //ConexiónBD con = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306/libreriad");
                    con.borrarBD();
                    Socket cl = new Socket(InetAddress.getByName(MAQ+namae.get(e)), 2069);
                    DataInputStream dis = new DataInputStream(cl.getInputStream());
                    //Leer losdatos principales del archivo y crear un flujo 
                    //Para escribir el archivo de salida
                    byte[] b = new byte[1024];
                    String nombre = dis.readUTF();
                    System.out.println("Recibimos el archivo"+nombre);
                    long tam = dis.readLong();
                    DataOutputStream dos = new DataOutputStream(new FileOutputStream(nombre));
                    long recibidos =0;
                    int n, porcentaje;
                    //Definimos el ciclo donde estaremos recibiendo los datos enviados por el cliente
                    while(recibidos<tam){
                        n = dis.read(b);
                        dos.write(b,0,n);
                        dos.flush();
                        recibidos += n;
                        porcentaje = (int)(recibidos*100/tam);
                    }
                    System.out.println("Archivo Recibido");
                    //cierre de  flujo
                    dos.close();
                    dis.close();
                    cl.close();
                    
                    con2.crearBD();
                    con.cargarBD();
                }
            }
        } catch (IOException ex) {
            System.out.println("Error en pedir la BD "+ex);
        }
    }
    
    
    
    /**
     * Con base a un objeto de tipo Petición realiza una replicación en todos los nodos secundarios
     * @param p Objeto a ser replicado en los nodos secundarios
     * @return true en caso de éxito, false en caso de falla
     */
    public static boolean replicacion(Peticion p){
        try{
            ObjectOutputStream oos=null;
            //Envíar a todos los nodos secuandarios 
            //La primera posición es del principal
            boolean b = true;
            for(int j=0;j<namae.size();j++){
                InetAddress dir = InetAddress.getByName(MAQ+namae.get(j));
                //Determinar si el nodo esta disponible
                if(stillAlive(dir)){
                    Socket cl = new Socket(dir, 2065);
                    oos = new ObjectOutputStream(cl.getOutputStream());
                    System.out.println("Enviando Objeto");
                    oos.writeObject(p);
                    oos.flush();
                    BufferedReader br2 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                    //Esperar el acuse de recibido
                    if (!br2.readLine().equals("Chido (Y)")) b=false;
                    /*Inserte código de manejo de errores cuando el nodo remoto te diga que salió mal xD*/
                }
                else System.out.println("Busca un ataud pequeño, por que el nodo "+j+" se murió :C");
            }
            //Regresa si se guardó todo correctamente
            return b;
        }catch(Exception ex){
            System.out.println("Error en el hilo de replicas "+ ex);
            return false;
        }
    }
    
    //Para los nodos secundarios
    /**
     * Hilo encargado de escuchar replicaciones y guardalas en la Base de datos
     */
    public void serverReplica(){
        Thread hilo = new Thread(new Runnable(){
            public void run() {
                ObjectInputStream ois=null;
                try{
                    ServerSocket s = new ServerSocket(2065);
                    System.out.println("Servidor de replicas iniciado...");
                    while(true){
                        Socket cl = s.accept();
                        System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
                        ois = new ObjectInputStream(cl.getInputStream());
                        Peticion p = (Peticion)ois.readObject();
                        if(p.getIp().equals("-1")){
                            System.out.println("Reinciando sesión...");
                            con.modifDispo();
                            noLibros=con.obtenerLibros();
                            lbLibros.setText("Libros disponibles: "+ noLibros);
                        }
                        else{
                            System.out.println("Objeto recibido, guardando en la BD");
                            System.out.println("ip: "+p.getIp());
                            System.out.println("Hora: "+p.getHora());
                            System.out.println("Libro: "+p.getLibro());
                            System.out.println("Fecha: "+p.getFecha());

                            //Guardar en la BD
                            con.almacenaUsuario(p);
                            con.almacenaPedido(p);
                            con.almanenaUsuarioSes(p);
                            con.modifDisp(p.getLibro());
                            noLibros=con.obtenerLibros();
                            lbLibros.setText("Libros disponibles: "+ noLibros);
                            jNomLibro.setText(p.getLibro());
                            muestraImagen();
                        }
                        //Enviando acuse
                        PrintWriter pw =new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                        pw.println("Chido (Y)");
                        pw.flush();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MuestraImage.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(MuestraImage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        hilo.start();
    }
}
