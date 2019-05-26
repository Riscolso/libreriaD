package libreriad;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static libreriad.MuestraImage.procesando;
import static libreriad.MuestraImage.s1;

/**
 * Implementación del algoritmo del anillo para decidir los coordinadores en un sistema distribuido
 * @author Ricardo
 */
public class AlgoritmoAnillo {
    
    /**
     * Lista con los nombres de los equipos del sistema distribuido.
     */
    public static ArrayList<Integer> namae = new ArrayList<Integer>();
    
    /**
     * Nombre del propio equipo.
     */
    Integer name;
    
    /**
     * Nodo coordinador.
     */
    int elsujeto = -1;
    
    /**
     * Cada cuanto tiempo actúa el algoritmo de latido (milisegundos)
     */
    private final int KANADE = 1000*60*5; //Cada 5 minutos.
    
    /**
     * Nombre de la máquina.
     */
    public static final String MAQ = "E";
    
    /**
     * Grupo multicast
     */
    InetAddress gru;
    
    /**
     * Algoritmo beat, cada cierto tiempo verifica que el nodo principal se encuentre disponible 
     */
    public void beat(){
        Thread t = new Thread(new Runnable(){
            @Override
            public void run(){
                while(true){
                    try{
                        //Si es el coordinador principal, detiene el hilo, checa esto cada 2 minutos
                        while(elsujeto != name && elsujeto != -1){
                            //Nodo principal
                            InetAddress coordinador = InetAddress.getByName(MAQ+elsujeto);
                            //Comprobar que esta muerto
                            if(!stillAlive(coordinador)){
                                System.out.println("El coordinado..... AH MUERTO! CHAN CHAN CHANNNNN");
                                elsujeto = -1;
                                procesando = true;
                                timeToDuel();
                            }
                            Thread.sleep(KANADE);
                        }
                        //Para que no gaste muchos recursos
                        //System.out.println("Hilo de beat apagado");
                        Thread.sleep(60*1000);
                        System.out.print("");
                    }catch(Exception ex){
                        System.out.println("Error en el hilo de latido "+ex);
                    }
                }
            }
        });
        t.start();
    }
    
    /**
     * Verifica que unla aplicación esté corriendo un equipo determinado. <br>
     * Se da tolerancia de 3 segundos en caso de no ser alcanzable.
     * @param ia Dirección Ip del equipo.
     * @return true si el equipo responde correctamente, false si el equipo no es alcanzable o no tiene la aplicación corriendo.
     */
    public static boolean stillAlive(InetAddress ia){
        try {
            Socket cl = new Socket();
            //Le damos 3 segundos para aceptar la conexión, si no la app en la PC no esta iniciada
            cl.connect( new InetSocketAddress(ia, 2067), 3000);
            BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            br.readLine();
            //System.out.println("Me llegó "+ br.readLine());
            return true;
        } catch (SocketTimeoutException ste){
            System.out.println("La aplicación al nodo que se quiere conectar no está disponible");
            return false;
        } catch (IOException ex) {
            //Cuando no se puede hacer conexión con nodo especificado
            return false;
        }
    }
    
    /**
     * Espera constantemente peticiones se clientes, con el objetivo de saber si la aplicación esta corriendo en este equipo
     */
    public void vivo(){
        Thread t = new Thread( new Runnable(){
            @Override
            public void run(){
                    ServerSocket s = null;
                try {
                    s = new ServerSocket(2067);
                } catch (IOException ex) {
                    System.out.println("Error al iniciar el server socket "+ex);
                }
                while(true){
                    try {
                        Socket cl = s.accept();
                        //Tiempo límite para que el nodo conteste
                        cl.setSoTimeout(5000);
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                        pw.println("This was a triumph");
                        pw.flush();
                    }catch (SocketException ex) {
                        System.out.println("No se pudo alcanzar el cliente");
                        Logger.getLogger(AlgoritmoAnillo.class.getName()).log(Level.SEVERE, null, ex);
                    }catch (IOException ex) {
                        System.out.println("Error al enviar confirmación de vivo");
                        Logger.getLogger(AlgoritmoAnillo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        t.start();
    }
    
    /**
     * Inicia una elección. <br>
     * Envía una trama de elección a el nodo siguiente, en caso de no estar disponible lo brinca.<br> 
     * Retorna un valor cuando la elección haya acabado.
     * @return true si se escogió un nuevo coordinador, false si hubo algún error al inicial la elección.
     */
    public boolean timeToDuel(){
        //Enviar al siguiente
        //El ciclo empieza en su nombre +1, ósea el siguiente directo
        try{
            boolean b=false;
            ArrayList<Integer> n = new ArrayList<Integer>();
            //Decir que es una elección
            n.add(-1);
            //Encolarse
            n.add(name);
            for(int e=0;e<namae.size();e++){
                //comprobar que esta vivo,si no lo toma como muerto y envia al siguiente
                if(stillAlive(InetAddress.getByName(MAQ+namae.get(e)))){
                    Socket nodo = new Socket(InetAddress.getByName(MAQ+namae.get(e)), 2066);
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
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * inicia una elección a un nodo específico <br>
     * Este siempre debe estar vivo.
     * @param inicial Número del equipo al cuál se envíara la trama de elección.
     */
    public void timeToDuel(int inicial){
        //Enviar al siguiente
        //El ciclo enpieza en su nombre + 1, ósea el siguiente directo
        try{
            ArrayList<Integer> n = new ArrayList<Integer>();
            //Decir que es una elección
            n.add(-1);
            //Encolarse
            n.add(name);
            if(stillAlive(InetAddress.getByName(MAQ+inicial))){
                Socket nodo = new Socket(InetAddress.getByName(MAQ+inicial), 2066);
                ObjectOutputStream ods = new ObjectOutputStream(nodo.getOutputStream());
                System.out.println("Enviando trama de elección");
                System.out.println(n.subList(0,n.size())+"\n");
                ods.writeObject(n);
                ods.flush();
                nodo.close();
            }
        }catch(Exception ex){
            System.out.println("No se pudo enviar la trama de elección "+ex);
        }
    }
    
    /**
     * Otorga formato a una lista de números para usarse como tabla de siguientes
     * @param n Lista a ser formateada.
     * @return Lista formateada.
     */
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
    
    /**
     * Hilo es cual espera tramas de elección para analizarlas, actualiza la tabla de siguientes construye la trama de nuevo y la envía al sig.
     */
    public void eleccion(){
        Thread t = new Thread(new Runnable(){
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
                                    Socket nodo = new Socket(InetAddress.getByName(MAQ+namae.get(e)), 2066);
                                    ObjectOutputStream ods = new ObjectOutputStream(nodo.getOutputStream());
                                    System.out.println("Habemus cordinadus a "+ MAQ+namae.get(e));
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
                                    if(stillAlive(InetAddress.getByName(MAQ+namae.get(e)))){
                                        Socket nodo = new Socket(InetAddress.getByName(MAQ+namae.get(e)), 2066);
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
        });
        t.start();
    }
    
    /**
     * Guardar un objeto en un archivo.
     * @param al Lista de enteros a ser guardada.
     */
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
    
    /**
     * Cargar un objeto a partir de una archivo.
     * @return Lista de enteros cargada del archivo.
     */
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
    
    /**
     * Hilo el cual espera tramas de los FrontEnds lo cuales avisan cuando el coordinador esta nadando con los peces.
     */
    public void serverChismes(){
        Thread t = new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    ServerSocket s = new ServerSocket(2068);
                    while(true){
                        Socket cl = s.accept();
                        procesando = false;
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
                                pw.println(":C"); 
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
    
    /**
     * Inicia el canal multicast el cual informa a los FrontEnd cual equipo es el nuevo coordinador.
     */
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
    
    /**
     * Envía a todos los FE cuál es el nuevo coordinador 
     * @param no Número del nuevo coordinador.
     */
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
}
