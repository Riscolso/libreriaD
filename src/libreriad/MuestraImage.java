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
package libreriad;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class MuestraImage extends javax.swing.JFrame implements Serializable {
    //Nombre de las máquinas
    public final static String maq = "E";
    //Relojes
    private String tiempo[];
    public static int segundero[];
    //Número de libros disponibles para distribuir
    private int noLibros;
    private static boolean on[];
    ModReloj mr;
    Thread  hilo[];
    int i;
    //Multicast - relojes
    public static InetAddress gpo = null;
    InetAddress gru = null;
    public static MulticastSocket s;
    public static MulticastSocket s1;
    static DatagramPacket p = null;
    
    //Para base de datos
    ConexiónBD con = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306/libreriad");
    ConexiónBD con2 = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306");
    /*Arreglo con Todos los botones que muentran la hora
    Pa' modificarlos por bonche, mas fácil :)*/
    public static JButton BRelojes[];
   
    //Nombres de las máquinas
    ArrayList<Integer> namae = new ArrayList<Integer>();
    
    //Nombre de la máquina actual
    Integer name;
    
    //Nodo siguiente directo - El nuevo nodo va a empezar una votación a este nodo
    //siempre que se incie un nuevo nodo, este nodo debe estar prendido si o sí
    int sig = 2;
    
    //Variables para manejar replicación
    boolean primario = true;
    //Nodo coordinador
    int elsujeto = -1;
    
    public static boolean procesando = false;
    
    /*----------------------------------Código algoritmo anillo--------------------------------*/
    public Runnable beat(){
        return new Runnable(){
            @Override
            public void run(){
                try{
                    while(true){
                        //Si es el coordinador principal, detiene el hilo, checa esto cada 2 minutos
                        while(elsujeto != name && elsujeto != -1){
                            //System.out.println("Enviando latido <3");
                            //Nodo principal
                            InetAddress coordinador = InetAddress.getByName(maq+elsujeto);
                            //Comprobar que esta muerto
                            if(!stillAlive(coordinador)){
                                System.out.println("El coordinado..... AH MUERTO!");
                                elsujeto = -1;
                                procesando = true;
                                timeToDuel();
                            }
                            //El latido se hace cada 5 min
                            Thread.sleep(5000);
                        }
                        //Para que no gaste muchos recursos
                        System.out.println("Hilo de beat apagado");
                        Thread.sleep(60*1000);
                    }
                }catch(Exception ex){
                    System.out.println("Error en el hilo de latido "+ex);
                }
            }
        };
    }
    
    //Determina si un nodo especifico esta vivo Y tiene la aplicación corriendo            
    public static boolean stillAlive(InetAddress ia){
        try {
            Socket cl = new Socket();
            //Le damos 3 segundos para aceptar la conexión, si no la app en la PC no esta iniciada
            cl.connect( new InetSocketAddress(ia, 2067), 3000);
            PrintWriter pw =new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
            pw.println("sigues vivo?");
            pw.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            br.readLine();
            //System.out.println("Me llegó "+ br.readLine());
            return true;
        } catch (SocketTimeoutException ste){
            System.out.println("La aplicación al nodo que se quiere conectar no está disponible");
            return false;
        } catch (IOException ex) {
            //Cuando no se puede hacer conexión con nodo especificado
            //System.out.println("Error en Still Alive");
            return false;
        }
    }
    
    //Es solo para que se pueda saber si el nodo esta vivo o muerto
    public Runnable vivo(){
        return new Runnable(){
            @Override
            public void run(){
                try {
                    ServerSocket s = new ServerSocket(2067);
                    while(true){
                        Socket cl = s.accept();
                        BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                        br.readLine();
                        //System.out.println("Me llegó "+ br.readLine());
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                        pw.println("StillAlive");
                        pw.flush();
                    }
                } catch (IOException ex) {
                    System.out.println("Error en hilo \"Vivo\" "+ ex);
                }
            }
        };
    }
    
    //Inicia una elección a sus siguientes
    //Envía trama al siguiente (Si esta vivo, si no lo brinca)
    //Retorna cuando ya se escogió un nuevo nodo
    public boolean timeToDuel(){
        //Enviar al siguiente
        //El ciclo enpieza en su nombre + 1, ósea el siguiente directo
        try{
            boolean b=false;
            ArrayList<Integer> n = new ArrayList<Integer>();
            //Decir que es una elección
            n.add(-1);
            //Encolarse
            n.add(name);
            for(int e=0;e<namae.size();e++){
                //comprobar que esta vivo,si no lo toma como muerto y envia al siguiente
                if(stillAlive(InetAddress.getByName(maq+namae.get(e)))){
                    Socket nodo = new Socket(InetAddress.getByName(maq+namae.get(e)), 2066);
                    ObjectOutputStream ods = new ObjectOutputStream(nodo.getOutputStream());
                    System.out.println("DuelEnviando trama de elección");
                    System.out.println(n.subList(0,n.size())+"\n");
                    ods.writeObject(n);
                    ods.flush();
                    while(procesando){
                        System.out.println("Procesando nuevo coordinador...");
                        //Esperara a que se termine de escoger un nuevo coordinador
                        //El sleep es para que no se gasten taaaaaantos recursos
                        Thread.sleep(1000);
                    }
                    System.out.println("Nuevo coordinador");
                    b = true;
                    nodo.close();
                    return true;
                }
            }
            //Si no había nadie disponible, este nodo pasa a ser el nuevo coordinador
            if(!b){
                System.out.println("Soy el nuevo coordinador por que no hay nadie mas\nHello darkness my old friend");
                elsujeto = name;
                //Enviar a todos los FE
                multiCoordinador(name);
            }
            return true;
        }catch(Exception ex){
            System.out.println("Error en el inicio de elección "+ex);
            return false;
        }
    }
    
    //Inicia una elección a un nodo en especifico - Siempre debe estar vivo dicho nodo
    public void timeToDuel(int inicial){
        //Enviar al siguiente
        //El ciclo enpieza en su nombre + 1, ósea el siguiente directo
        try{
            ArrayList<Integer> n = new ArrayList<Integer>();
            //Decir que es una elección
            n.add(-1);
            //Encolarse
            n.add(name);
            if(stillAlive(InetAddress.getByName(maq+inicial))){
                Socket nodo = new Socket(InetAddress.getByName(maq+inicial), 2066);
                ObjectOutputStream ods = new ObjectOutputStream(nodo.getOutputStream());
                System.out.println("Enviando trama de elección");
                System.out.println(n.subList(0,n.size())+"\n");
                ods.writeObject(n);
                ods.flush();
                nodo.close();
            }
        }catch(Exception ex){
            System.out.println("No se pudo conectar con el nodo establecido "+ex);
        }
    }
    
    //Prepara la tabla de los siguientes
    public ArrayList<Integer> prepaSig(ArrayList<Integer> n){
        //Quitar el primer elemento
        n.remove(0);
        //Agregar los elementos de la tabla de los siguientes sin repetir
        for(int c : namae) if(!n.contains(c)) n.add(c);
        //Obtener el mayor de la lista
        int aux = 0;
        for(int i =0;i<n.size();i++){
            if(n.get(i)>aux) aux = n.get(i);
        }
        //Si el nodo es el mayor de la lista o el primero ordenar de menor a mayor
        if(name == aux || name == 1){
            Collections.sort(n);
        }
        //Si no, ordenar de mayor a menor
        else{
            Comparator<Integer> comparador = Collections.reverseOrder();
            Collections.sort(n, comparador);
        }
        n.remove(name);
        return n;
    }
    
    //Va a estar esperando tramas de elección, luego las analiza, actualiza sus siguientes
    //construye la trama de nuevo y la envía al sig
    public Runnable eleccion(){
        return new Runnable(){
            @Override
            public void run(){
                ObjectInputStream ois=null;
                try{
                    ServerSocket s = new ServerSocket(2066);
                    System.out.println("Servidor de elección iniciado...");
                    while(true){
                        Socket cl = s.accept();
                        //System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
                        ois = new ObjectInputStream(cl.getInputStream());
                        //oos = new ObjectOutputStream(cl.getOutputStream());
                        ArrayList<Integer> n = (ArrayList<Integer>)ois.readObject();
                        //Si el primer elemento que llega es un -1, significa que quiere inciar una pelea pokemon
                        System.out.println("Trama "+ n.subList(0, n.size()));
                        if(n.get(0).equals(-1)){
                            //Si el primero de los nombres es el mismo que el suyo, significa que va a ser el nuevo coordinador
                            if(n.get(1).equals(name)){
                                //lista auxiliar es la que se va a enviar
                                ArrayList<Integer> la = (ArrayList<Integer>)n.clone();
                                
                                la.set(0, -2);
                                //Indicar que ahora él es el coordinador
                                elsujeto = name;
                                //Actualizar tabla de los siguientes
                                namae = prepaSig(n);
                                System.out.println("La nueva tabla de siguientes "+namae.subList(0, namae.size()));
                                System.out.println("Valor de la "+la.subList(0, la.size()));
                                //Guardar en el archivo - por si se cae, para cuando se vuelva a levantar ya sabe a donde tirar tierra xD
                                guardarLista(namae);
                                //Enviar a todos los nodos siguientes
                                for(int e=0;e<namae.size();e++){
                                    Socket nodo = new Socket(InetAddress.getByName(maq+namae.get(e)), 2066);
                                    ObjectOutputStream ods = new ObjectOutputStream(nodo.getOutputStream());
                                    System.out.println("Habemus cordinadus a "+ maq+namae.get(e));
                                    System.out.println(la.subList(0, la.size())+"\n");
                                    ods.writeObject(la);
                                    ods.flush();
                                    nodo.close();
                                }
                                //Enviar a todos los FE
                                multiCoordinador(name);
                            }
                            //En otro caso, pásalas si no te embarazas 
                            else{
                                //Si no estás, métete
                                if(!n.contains(name)) n.add(name);
                                //Ordenar de menor a mayor
                                Collections.sort(n);
                                System.out.println("Lista de siguientes, soy "+name);
                                for(int a : namae){
                                    System.out.print(a+" ");
                                }
                                System.out.println("");
                                //Enviar al siguiente
                                for(int e=0;e<namae.size();e++){
                                    //comprobar que esta vivo,si no lo toma como muerto y envia al siguiente
                                    if(stillAlive(InetAddress.getByName(maq+namae.get(e)))){
                                        Socket nodo = new Socket(InetAddress.getByName(maq+namae.get(e)), 2066);
                                        ObjectOutputStream ods = new ObjectOutputStream(nodo.getOutputStream());
                                        System.out.println("Enviando trama de elección a "+namae.get(e));
                                        System.out.println(n.subList(0,n.size())+"\n");
                                        ods.writeObject(n);
                                        ods.flush();
                                        nodo.close();
                                        break;
                                    }
                                }
                            } 
                        }
                        //En cambio si es un -2, significa Yes, we can 
                        else if(n.get(0).equals(-2)){
                            //Indicar quien es el nuevo coordinador
                            elsujeto = n.get(1);
                            //Actualizar tabla de los siguientes
                            namae = prepaSig(n);
                            //Guardar en el archivo - por si se cae, para cuando se vuelva a levantar ya sabe a donde tirar tierra xD
                            guardarLista(namae);
                            System.out.println("La nueva tabla de siguientes "+namae.subList(0, namae.size()));
                            System.out.println("Yes, we can!: "+elsujeto);
                            procesando = true;
                        }
                    }
                }catch(Exception ex){
                    System.out.println("Error en servidor de elección "+ex);
                }
            }
        };
    }
    
    //Guardar un objeto en un archivo
    public void guardarLista(ArrayList<Integer> al){
        File archivo=new File("siguientes.obj");
        try{
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo));
        oos.writeObject(al);
        oos.close();
        }catch(Exception ex){
            System.out.println("Error al crear el archivo nuevo "+ex);
        }
    }
    
    //Cargar un objeto en un archivo
    public ArrayList<Integer> cargarLista(){
        ArrayList<Integer> lp = new ArrayList<Integer>();
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("siguientes.obj"));
            lp =(ArrayList<Integer>)ois.readObject();
        }catch(Exception ex){
            System.out.println("El archivo no existe o esta dañado, creando uno nuevo "+ex);
            guardarLista(namae);
        }
        finally{
            return lp;
        }
    }
    
    //Escucha cuando un FE le dice que se cayó el primario
    public void serverChismes(){
        Thread t = new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    ServerSocket s = new ServerSocket(2068);
                    while(true){
                        Socket cl = s.accept();
                        procesando = false;
                        BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                        br.readLine();
                        System.out.println("Un pajarito me dijo que el coordiandor se murió");
                        PrintWriter pw =new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                        //Si ya es el coordinador, namás le das el avión jajajaja
                        if(elsujeto == name){
                            pw.println("Listo"); //Le envía cuando hay nuevo coordinador
                            pw.flush();
                        }
                        //Ah no ma, sí tiene razón
                        else{
                            //Esperar a que termine de escoger un nuevo coordinador
                            if(timeToDuel()){
                                pw.println("Listo"); //Le envía cuando hay nuevo coordinador
                                pw.flush();
                            }
                            else{
                                System.out.println("Algo salió mal en el server de chismes");
                                pw.println(":C"); //Le envía cuando hay nuevo coordinador
                                pw.flush();
                            }
                        }
                    }
                } catch (IOException ex) {
                    System.out.println("Error en el servidor de chismes "+ ex);
                }
            }
        });
        t.start();
    }
    
    //Inicia el canal muticast 
    public void iniciarMulti(){
        try {
            s1 = new MulticastSocket(9876);
            s1.setReuseAddress(true);
            s1.setTimeToLive(3);
            gru = InetAddress.getByName("228.1.1.2");
            s1.joinGroup(gru);
            System.out.println("Canal de multicast iniciado");
        } catch (IOException ex) {
            System.out.println("Error inciando multicast");
        }
    }
    
    //Envía a todos los FE cuál es el nuevo coordinador 
    //Ejecutable del hilo que actualiza el tiempo en relojes cliente - Multicast
    public void multiCoordinador(int no){
        try{
            System.out.println("Voy a enviar que soy coordinador a los FE");
            //gpo = InetAddress.getByName("228.1.1.2");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(no);
            dos.flush();
            byte[] b = baos.toByteArray();
            DatagramPacket p = new DatagramPacket(b,b.length,gru,2000);
            s1.send(p);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /*----------------------------------Código replicación(priamria)---------------------------*/
    //Servidor que cuando le piden la base de datos, genera sql y envia
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
                        int porcentaje, n;
                        while(enviados<tam){
                            n = dis.read(b);
                            //System.out.println("Mira b"+b);
                            dos.write(b,0,n);
                            dos.flush();
                            enviados += n;
                            porcentaje = (int)(enviados*100/tam);
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

    //Pide al siguiente la base de datos
    public void pedirBD(){
        try {
            for(int e=0;e<namae.size();e++){
                //comprobar que esta vivo,si no lo toma como muerto y envia al siguiente
                if(stillAlive(InetAddress.getByName(maq+namae.get(e)))){
                    //ConexiónBD con = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306/libreriad");
                    con.borrarBD();
                    Socket cl = new Socket(InetAddress.getByName(maq+namae.get(e)), 2069);
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
    
    //Para el nodo principal
    public boolean replicacion(Peticion p){
        try{
            ObjectOutputStream oos=null;
            //Envíar a todos los nodos secuandarios 
            //La primera posición es del principal
            boolean b = true;
            for(int j=0;j<namae.size();j++){
                InetAddress dir = InetAddress.getByName(maq+namae.get(j));
                //Determinar si el nodo esta disponible
                if(stillAlive(dir)){
                    Socket cl = new Socket(dir, 2065);
                    oos = new ObjectOutputStream(cl.getOutputStream());
                    //ois = new ObjectInputStream(cl.getInputStream());
                    //System.out.println("Conexión establecida...");
                    System.out.println("Enviando Objeto");
                    oos.writeObject(p);
                    oos.flush();
                    //Creamos un flujo de caracter ligado al socket para recibir el mensaje
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
    public Runnable serverReplica(){
        return new Runnable(){
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
        };
    }
    
    /*----------------------------------Código libros--------------------------*/
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
    
    //CÓDIGO QUE TRAE EL NÚMERO DE LIBROS DISPONIBLES Y LO ASIGNA ABAJO :P
    public int idLibro (){
        Random r = new Random();
        int num = r.nextInt(25)+1;   
        //idDis=num;
        return num;
    }
    
    //Ejecutable del hilo de las peticiones de libros
    public Runnable servidorPeticiones(){
         return new Runnable(){
            @Override
            public void run() {
                try{
                    ServerSocket s = new ServerSocket(1234);
                    System.out.println("Servidor de peticiones inciado...");
                    //System.out.println( "IP: " + hostIP + "\n" + "Name: " + hostName);

                    //el servidor espera dentro de un ciclo infinitola solicitud de conexión de un cliente
                    while(true){
                        noLibros=con.obtenerLibros();
                        lbLibros.setText("Libros disponibles: "+ noLibros);
                        Socket cl= s.accept();
                        Peticion p = new Peticion();
                        
                        //Obtener la IP del cliente
                        p.setIp(cl.getInetAddress()+":"+cl.getPort());
                        System.out.println("Conexión establecida desde "+p.getIp());

                        //Ligamos un printwriter a un flujo de salida de caracter
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                        
                        //Si hay libros disponibles
                        if(noLibros!=0){
                            
                            //p.setLibro(obtenerNombreLibro());

                            //Establecer la hora local
                            p.setHora(tiempo[0]);
                            //Aquí checa la disponibilidad del libro gg.
                            while(true){
                                int aux = idLibro();
                                //System.out.println("Aqui: "+ con.obtenerDisp(aux));
                                if("Disponible".equals(con.obtenerDisp(aux))){
                                    p.setLibro(con.obtenerNombreLibro(aux));
                                    jNomLibro.setText(p.getLibro());
                                    muestraImagen();
                                    JOptionPane.showMessageDialog(null, "Libro Prestado");
                                    break;
                                }
                            }
                            
                            //Obtener la fecha del día
                            Date d = new Date();
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            String ss = dateFormat.format(d);
                            p.setFecha(ss);

                            //Registrar en la base de datos la petición, el usuario y modificar la disponibilidad
                            con.almacenaUsuario(p);
                            con.almacenaPedido(p);
                            con.almanenaUsuarioSes(p);
                            con.modifDisp(p.getLibro());
                            
                            //Replicación a todos los nodos secundarios
                            if(replicacion(p)) System.out.println("Todo salió bien en los secundarios");
                            
                            //Hasta que todos hayan contestado, se envía al cliente
                            System.out.println("Nombre del libro: "+p.getLibro());
                            pw.println(p.getLibro());  //Enviar el nombre del libro
                            pw.flush();
                            
                            noLibros=con.obtenerLibros();
                            lbLibros.setText("Libros disponibles: "+ noLibros);
                        }
                        else{
                            //Enviar que ya no hay libros
                            pw.println("Ya no hay >:v");//Supongo que ningún libro se llama así xD
                            pw.flush();
                            JOptionPane.showMessageDialog(null, 
                              "Ya no hay libros :C", 
                              "Changos!", 
                              JOptionPane.WARNING_MESSAGE);
                            lbLibros.setText("Libros disponibles: SOLD OUT");
                        }
                    }
                }catch(Exception ex){
                    System.out.println("Error en el hilo del servidor de libros "+ex);
                }
            }
        };
    } 
    
    /*----------------------------------Código Relojes--------------------------*/
    public void muestraImagen(){
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
    
    //Todo lo relacionado con crear los relojes, su hora, los hilos que lo manejan ETC
    public void iniciarRelojes(){
        //Llenar el arreglo de los botones
        BRelojes = new JButton[4];
        BRelojes[0] = r1;
        BRelojes[1] = r2;
        BRelojes[2] = r3;
        BRelojes[3] = r4;
        
        //Instanciar la ventana de modificar
        mr = new ModReloj();
        tiempo = new String[4];
        segundero = new int[4];
        on = new boolean[4];
        hilo = new Thread[4];
        

        for(i=0;i<4;i++){
            //Todos los relojes online disponibles
            //reonline[i]= false;
            
            //Tener la variable e evita que los hilos se declaren con el mismo valor de i = 3
            int e=i;
            
            //Encender todos los relojes
            on[i] = true;
            
            segundero[i] = 1000;
            
            //Código de cada reloj (Hilos)
            hilo[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int seg=0, min, hor;
                        while(true){
                            while(on[e]){

                                tiempo[e] = BRelojes[e].getText();

                                seg = Integer.parseInt(tiempo[e].substring(tiempo[e].lastIndexOf(":")+1));
                                min = Integer.parseInt(tiempo[e].substring(tiempo[e].indexOf(":")+1,tiempo[e].lastIndexOf(":")));
                                hor = Integer.parseInt(tiempo[e].substring(0,2));
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

                                BRelojes[e].setText(cadenaDig(hor)+":"+cadenaDig(min)+":"+cadenaDig(seg));
                                Thread.sleep(segundero[e]);
                            }
                        System.out.println();
                        }
                        } catch (Exception ex) {
                            System.out.println("Error en hilo "+e+": "+ex);
                        }
                }
            });
            
            
            //Iniciar los hilos
            hilo[i].start();
            
        }
        
        //Obtener la hora local para el primer reloj
        Date date = new Date();
        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
        setTime(hourFormat.format(date)+"", 0, 1000, false);
        
        //Asignar hora random a los demás relojes
        for(int j=1;j<4;j++)
            setTime(cadenaDig((int) (Math.random() * 23))+":"+
                    cadenaDig((int) (Math.random() * 59))+":"+
                    cadenaDig((int) (Math.random() * 59)), j, 1000, false);
    }
    
    //Ejecutable del hilo que actualiza el tiempo en relojes cliente - Multicast
    public Runnable servidorRelojes(){
        return new Runnable(){
            @Override
            public void run() {
                try{
                    s = new MulticastSocket(9875);
                    s.setReuseAddress(true);
                    s.setTimeToLive(3);
                    gpo = InetAddress.getByName("228.1.1.1");
                    s.joinGroup(gpo);//NOs unimos al canal multicast(Aunque seamos el server)
                    System.out.println("Servidor de relojes iniciado ");

                    //Recibir peticiones
                    //Datagrama para recibir
                    DatagramPacket rec = new DatagramPacket(new byte[20], 20);
                    //Datagrama para enviar
                    //Envia a los que esten suscritos al puerto 4000
                    p = new DatagramPacket(tiempo[0].getBytes(),tiempo[0].getBytes().length,gpo,4000);
                    
                    while(true){
                        s.receive(rec);
                        System.out.println("Me llegó una petición de reloj "+ new String(rec.getData()));
                        String aux = new String(rec.getData());
                        if(aux.charAt(0)=='c'){
                            //Enviar la hora del número que pidió el usuario
                            String msj = new String(tiempo[Integer.parseInt(aux.substring(1,2))]+aux.substring(1,2));
                            System.out.println("Voy a enviar "+msj);
                            p.setData(msj.getBytes());
                            s.send(p);
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
    }
    
    public static void setTime(String nvoTime, int noReloj, int seg, boolean b){
        BRelojes[noReloj].setText(nvoTime);
        
        //Reanudar el reloj
        on[noReloj] = true;
        
        //Nuevo valor del segundero
        segundero[noReloj] = seg;
        
        //Enviar el tiempo a los clientes
        //Si el servidor yá fue iniciado
        if(p!=null && b){
            p.setData((nvoTime+noReloj).getBytes());
            try {
                s.send(p);
                System.out.println("Envio "+nvoTime+noReloj );
            } catch (IOException ex) {
                System.out.println("Error en envio "+ ex);
            }
        }
    }
    
    //Si recibe un número de una dígito, lo regresa con un '0' a su izquierda
    public static String cadenaDig(int h){
            if(h<10) return new String("0"+h);
            else return new String(h+"");
    }
    
    /*----------------------------------Constructor-----------------------------*/
    public MuestraImage() {
        initComponents();
        setLocationRelativeTo(this);
        btnReIni.setVisible(true);
        
        //Vista para mostrar info del libro 
        vistaInfoLibro();
        //Código para los relojes
        iniciarRelojes();
        
        
        try {
            //Saber cuál es el nombre de la máquina
            //Suponiendo que todos tienen un número de nombre xD
            String aux = InetAddress.getLocalHost().getHostName()+"";
            name = Integer.parseInt(aux.charAt(1)+"");
            System.out.println("Mi nombre "+name);
        } catch (UnknownHostException ex) {
            System.out.println("Error al obtener mi nombre... Who am i?: "+ ex);
        }
        //Hilo que se encarga de todo el algoritmo del anillo
        Thread hiloEleccion = new Thread(eleccion());
        hiloEleccion.start();
        
        //Hilo para que informe si esta vivo el nodo
        Thread alive = new Thread(vivo());
        alive.start();
        
        //Servidor de las peticiones de libro
        Thread serverLibros = new Thread(servidorPeticiones());
        serverLibros.start();
        
        //Cargar los siguientes de una archivo
        //Si esta vacío es la primera vez que aparece el nodo
        namae = cargarLista();
        
        //Iniciar servidor de relojes
        Thread serverMulticastRelojes = new Thread(servidorRelojes());
        serverMulticastRelojes.start();
        
        //Iniciar el canal de comunicación que envía cuando hay nuevo coordinador a los FE
        iniciarMulti();
        
        //Hilo para enviar BD
        enviarBD();
        
        //Si no hay siguientes
        if(namae.size() == 0){
            //Si es el primerísimo de todos 
            if(name == 1){
                System.out.println("Estoy solo en el mundo\nHello darkness my old friend");
                elsujeto = name;

                //Código para las peticiones
                btnReIni.setVisible(true);
                
                //Generar sql de la bd
                con.respaldarBD();
                
                //Escuchar cada que hay una replica
                Thread replica = new Thread(serverReplica());
                replica.start();

                namae.add(2);
            }
            //Si es un nodo nuevo y es secundario
            else{
                //El nodo 1 siempre debe estar encendido cuando se inicia un nodo nuevo
                namae.add(1);
                
                timeToDuel();
                Thread beats = new Thread(beat());
                beats.start();
                //Escuchar cada que hay una replica
                Thread replica = new Thread(serverReplica());
                replica.start();
                //Escuchar a los FE cuando se caíga el principal
                serverChismes();
                
                pedirBD();
            }
        }
        //Si esta reviviendo de sus cenizas cual ave fénix
        else if(name!=1){
            timeToDuel();
            Thread beats = new Thread(beat());
            beats.start();
            //Escuchar cada que hay una replica, el primario nunca lo va a iniciar
            Thread replica = new Thread(serverReplica());
            replica.start();
            //Escuchar a los FE cuando se caíga el principal
            serverChismes();
            pedirBD();
        }
        //Y ya si es el coordinador principal el que revive
        else{
            //Escuchar cada que hay una replica
            Thread replica = new Thread(serverReplica());
            replica.start();
            timeToDuel();
            pedirBD();
        }
        noLibros=con.obtenerLibros();
        lbLibros.setText("Libros disponibles: "+ noLibros);
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
        r1.setForeground(new java.awt.Color(0, 0, 0));
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
        btncor.setActionCommand("Coordinador");
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
                        .addComponent(lbLibros)
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
                                .addComponent(lbLibros)))
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
        on[0] = false;
        //Mandar tiempo y no de reloj al otr frame
        mr.setTime(tiempo[0], 0, segundero[0]);
        mr.setVisible(true);
    }//GEN-LAST:event_r1ActionPerformed

    private void r2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r2ActionPerformed
        on[1] = false;
        mr.setTime(tiempo[1], 1, segundero[1]);
        mr.setVisible(true);
    }//GEN-LAST:event_r2ActionPerformed

    private void r3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r3ActionPerformed
        on[2] = false;
        mr.setTime(tiempo[2], 2, segundero[2]);
        mr.setVisible(true);
    }//GEN-LAST:event_r3ActionPerformed

    private void r4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r4ActionPerformed
        on[3] = false;
        mr.setTime(tiempo[3], 3, segundero[3]);
        mr.setVisible(true);
    }//GEN-LAST:event_r4ActionPerformed
    
    
    
    private void btnReIniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReIniActionPerformed
        
        //Modificar la disponibilidad de manera local
        con.modifDispo();
        noLibros=con.obtenerLibros();
        lbLibros.setText("Libros disponibles: "+ noLibros);
        replicacion(new Peticion("-1"));
        /*try {
            //Modificar la disponibilidad de manera remota
            //if(!stub.reiniciarSesion()) System.out.println("Explotó algo EN el servidor remoto");
        } catch (RemoteException ex) {
            System.out.println("Explotó al enviar el servidor remoto D:");
        }*/
    }//GEN-LAST:event_btnReIniActionPerformed

    private void btncorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncorActionPerformed
        System.out.println("El coordinador es: "+elsujeto);
        System.out.println("Mi tabla de siguientes es ");
        for(int a : namae){
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
    private javax.swing.JLabel jMuestraLibro;
    private javax.swing.JTextField jNomLibro;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jSalir;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbLibros;
    public static javax.swing.JButton r1;
    public static javax.swing.JButton r2;
    public static javax.swing.JButton r3;
    public static javax.swing.JButton r4;
    // End of variables declaration//GEN-END:variables
}
