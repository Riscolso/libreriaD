package libreriad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static libreriad.MuestraImage.cadenaDig;

/**
* <h1>Clase encargada de todo lo relacionado con el Argotirmo de Berkeley</h1> <br>
* Los métodos staticos presentarseServTime() y hiloEscuchaHora() son para usarse en el 
* código de los nodos del sistema distribuido. <br>
* Todo lo demás esta pensado para usarse en el Servidor de Tiempo
* @author  Equipo 3 RULEZ
* @version 1.0
* @since   2019-04-05
*/

public class AlgoritmoBerkeley {
    /**
     * El tiempo bueno
     */
    String tiempo;
    /**
     * Cada cuando se debe sincronizar la hora en milisegundos
     */
    int y = 1000*60;
    /**
     * Lista de todos los equipos que se han conectado al servidor
     */
    /**
     * Tiempo de tolerancia para obtener Y en milisegundos
     */
    final int TIMETOLERA  = 60*1000;
    /**
     * Tiempo de referencia para discriminar horas (En segundos)
     * 24 minutos
     */
    final int TIMEREF = 24*60;
    public ArrayList<Equipo> equipos = new ArrayList<Equipo>();
    final static int PTOBER = 2070; //El puerto definido para recibir y enviar tramas de este algorimto
    final static int PTONVO = 2071; //Puerto definido para cuando un nodo nuevo inicia
    ConexiónBD con = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306/relojd"); //Objeto para usar la base de datos
    final static String IPSERV = "192.168.1.67"; //Dirección ip del servidor de tiempo
    public AlgoritmoBerkeley(){
        
    }
    
    /**
     * Calcula el tiempo (en segundos) de retardo entre los ciclos de sincronización de hora 2PS (P=latencia máxima,S=Tolerancia) <br>
     * Obtener P de {@link #obtenerLatenciaMax() } <br>
     * S es al gusto
     * Estado: Completado.
     */
    public void calcularY(){
        //Tolerancia de 1 minuto
        y = 2*obtenerLatenciaMax()*TIMETOLERA;
        System.out.println("Nuevo tiempo Y (Milisegundos): "+y+" osea cada" + y/1000*60+" minutos");
    }
    
    /**
     * Pide la hora a los equipos; las ips se obtienen del arreglo "equipos"
     * @return Arreglo de objetos de equipos con horas listas
     * 
     * Estado: En pruebas
     */
    public ArrayList<Equipo> pedirHoras(){
        ArrayList<Equipo> aux = new ArrayList<Equipo>();
        for(Equipo e : equipos){
            try {
                Socket cl = new Socket(e.getIp(),PTOBER);
                cl.setSoTimeout(10000);//Tienen 10 seg para contestar o hay tabla
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                //Envíar una p al servidor hace que este responda con su hora
                pw.println("p");
                pw.flush();
                BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                String msj = br.readLine();
                //System.out.println("Hora recibida "+msj);
                Equipo ea = e;
                ea.sethEquipo(msj);
                aux.add(e);
            } catch (IOException ex) {
                Logger.getLogger(AlgoritmoBerkeley.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error al recibir la hora de equipo "+e.getNombre());
            }
        }
        return aux;
    }
    
    /**
     * Envia el valor de ajuste a un equipo especificado <br>
     * La trama se envía a ip con el puerto ptoBer <br>
     * Debe ser un String con la forma "5" 0 "-3" 
     * @param ip Dirección del equipo al que se va a enviar 
     * @param ajuste Que tanto debe de adelantarse(cantidad) o relentizarse(-cantidad) el reloj del nodo (en segundos)
     * 
     * Estado: En pruebas
     */
    public void enviaAjuste(String ip, int ajuste){
        try {
            //Crear el socket
            Socket cl = new Socket(ip,PTOBER);
            PrintWriter pw =new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
            //Obtener el nombre de la máquina
            pw.println(ajuste+"");
            pw.flush();
            //Cerramos los flujos, el socket y terminamos el programa
            pw.close();
            cl.close();
        } catch (IOException ex) {
            System.out.println("Error "+ex);
            System.out.println("No se pudo enviar el ajuste a "+ip);
        }
    }
    
    /**
     * Obtiene la latencia ente la computadora actual y la especificada en la ip
     * Lo hace 3 veces y obtiene el promedio
     * @param ip computadora a la cual se va a calcular la latencia
     * @return latencia en segundos
     * 
     * Estado: Completo
     */
    public int calcularLatencia(InetAddress ip){
        long a,b, t=0;
        for(int i=0;i<3;i++){
            a = System.currentTimeMillis();
            try {
                if(!ip.isReachable(5000)){
                    System.out.println("No se puede llegar a la ip");
                    return -1;
                }  
            } catch (IOException ex) {
                Logger.getLogger(AlgoritmoBerkeley.class.getName()).log(Level.SEVERE, null, ex);
            }
            b = System.currentTimeMillis();
            t += b-a;
        }
        //System.out.println("Latencia = "+t/3);
        return (int)t/3;
    }
    
    /**
     * Hilo encargado sincronizar todos los equipo cada "y" cantidad de tiempo  <br>
     * 1.-Envía peticiones de hora a los nodos del sistema (ojo, puede no haber ninguno y por tanto no hace nada)<br>
     * 2.-Una vez que las tenga todas; por todas se refiere a los nodos del arreglo "equipos", calcula la hora contemplando la hora razonable <br>
     * 4.-Haciendo uso del método {@link #enviaHora(java.lang.String, int)}, envía a un nodo determinado si debe adelantarse o atrasarse x segundos <br>
     * 5.-Registra en la base de datos usando el método {@link ConexiónBD#registrarHora(int, int, java.util.Map, int, int) } <br>
     * registrarHora(hora anterior, hora nueva, Dupla(int, String) con id  y hora de los equipos, tiempo que se adelantó, tiempo que se atrasó) <br>
     * @see <a href="https://jarroba.com/map-en-java-con-ejemplos"> Maps </a> Para información sobre como usar los mapHash
     * 6.-Actualizar la variable global hora <br>
     * 5.-Esperar "y" segundos y vuelve a empezar <br>
     * 
     * Estado: En pruebas - base de datos
     */
    public void berkeley(){
        Thread t = new Thread(new Runnable(){
                @Override
                public void run(){
                    while(true){
                        if(equipos.size()!=0){
                            int time = timeASeg(tiempo); //Tiempo del servidor
                            int t=0, c=0; //C es un contador que indica cuantos equipos se usaron
                            ArrayList<Equipo> es = pedirHoras();
                            for(Equipo e : es){
                                int timeE = timeASeg(e.gethEquipo())+(e.getLatencia()/2); //Tiempo del equipo
                                //Discriminar por hora de referencia
                                if(timeE<time+TIMEREF && timeE>time-TIMEREF){
                                    t+=timeE;
                                    c++;
                                }
                                else{
                                    System.out.println("Se discriminó la hora del equipo "+e.getNombre());
                                }
                            }
                            //Calcular el promedio
                            int prom = t/c;
                            //Calcular y enviar el ajuste de cada equipo
                            for(Equipo e : es){
                                int timeE = timeASeg(e.gethEquipo());
                                int ajuste = (prom+(e.getLatencia()/2))-timeE;
                                e.setAdelantar(0);
                                e.setRelentizar(0);
                                if(ajuste<0){
                                    e.setRelentizar(ajuste);
                                }
                                else if(ajuste>0){
                                    e.setAdelantar(ajuste);
                                }
                                enviaAjuste(e.getIp(),ajuste);
                            }
                            //Registrar en la BD
                            System.out.println("Base de datos tiempo: "+tiempo);
                            System.out.println("SegATIME "+segATime(prom));
                            con.registrarHora(tiempo,segATime(prom), es);
                            //Nueva hora
                            tiempo = segATime(prom);
                            System.out.println("La nueva hora es "+tiempo);
                        }
                        //Esperar
                        try {
                            Thread.sleep(y);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(AlgoritmoBerkeley.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.print("");
                    }
                }
            }
        );
        t.start();
    }
    
    /**
     * Convierte una cadena de formato "hh:mm:ss" a un entero con los segundos totales
     * @param tiempo cadena en formato "hh:mm:ss"
     * @return Cantidad de segundos que hay en la hora especificada
     */
    public int timeASeg(String tiempo){
        int seg = Integer.parseInt(tiempo.substring(tiempo.lastIndexOf(":")+1));
        int min = Integer.parseInt(tiempo.substring(tiempo.indexOf(":")+1,tiempo.lastIndexOf(":")));
        int hor = Integer.parseInt(tiempo.substring(0,2));
        return seg+(60*min)+(60*60*hor);
    }
    
    /**
     * Convierte una cantidad de segundos a tiempo de forma "hh:mm:ss"
     * @param s cantidad de segundos
     * @return tiempo
     */
    public String segATime(int s){
        int hor = s/(3600);
        int min = s%(3600)/60;
        int seg = s%60;
        return cadenaDig(hor)+":"+cadenaDig(min)+":"+cadenaDig(seg);
    }
    
    /**
     * Obtiene la latencia más grande de los equipos de la variable global "equipos" (en segundos)
     * @return latencia mas grande
     * 
     * Estado: completado
     */
    public int obtenerLatenciaMax(){
        int m=0, l=0;
        for(Equipo e: equipos){
            l = e.getLatencia();
            if(l>m) m = l;
        }
        return m;
    }
    
    /**
     * Hilo encargado de escuchar constantemente nodos nuevos por el número de puerto ptoNvo <br>
     * Cuando llega una trama nueva <br>
     * 1.-Se obtiene la IP, el nombre y la latencia; esta última se obtiene de {@link #calcularLatencia(java.net.InetAddress)  } <br>
     * 2.-Se encapsula lo anterior en un objeto de tipo Equipo <br>
     * 3.-Se guarda en la base de datos y se asigna su ID a el mismo objeto usando el método int registrarEquipo(Equipo e) <br>
     * PE: {@code equipo.setId(registrarEquipo(equipo));} <br>
     * 4.-Agregar el objeto con id, ip, nombre y latencia a la variable global "equipos" <br>
     * 5.-Llamar el método {@link #calcularY() } <br>
     * 
     * Estado: Completado - Desbloquear BD
     */
    public void hiloEscuchaEquipos(){
        Thread t = new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        ServerSocket s = new ServerSocket(PTONVO);
                        System.out.println("Servidor de escucha equipos iniciado...");
                        while(true){
                            Socket cl= s.accept();
                            InetAddress ia = cl.getInetAddress();
                            BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                            //Encapsular información
                            String aux = br.readLine();
                            if(!contieneNombre(aux)){
                                Equipo e = new Equipo(ia.getHostAddress(), aux, (int)calcularLatencia(ia));
                                //Guardar en la BD  y asignar su Id al objeto
                                e.setId(con.registrarEquipo(e));
                                //Agregar a equipos
                                equipos.add(e);
                                //Imprimir datos
                                System.out.println("Se encontró un nuevo nodo, guadado en la base de datos");
                                e.imprimirEquipo();
                                //Calcular el nuevo tiempo de retraso para sincronización
                                //calcularY();
                                //Cerrar flujos
                                cl.close();
                            }
                            else System.out.println("El equipo ya había sido registrado antes");
                        }
                    } catch (IOException ex) {
                        System.out.println("Error en hilo escucha equipos ");
                        ex.printStackTrace();
                    }
                }
            }
        );
        t.start();
    }
    
    /**
     * Busca y dice si hay un objeto con la cadena n en la lista de equipos
     * @param n cadena correspodiente al nombre de la clase Equipo
     * @return true- si existe, en caso contrario false
     */
    public boolean contieneNombre(String n){
        for(Equipo e: equipos){
            if(e.getNombre().equals(n)) return true;
        }
        return false;
    }
    
    
    /*------------------------------------------ CÓDIGO PARA LOS NODOS ------------------------------------------------*/
    /**
     * Enviar una trama a el servidor de tiempo, usando la ip ipServ y el puerto ptoNvo <br>
     * Se envía el nombre de la PC ya que este no se puede obtener en el servidor <br>
     * El mensaje puede ser el que sea <br>
     * Recibe una trama y muestra en consola que se ha registrado con el servidor de tiempo
     * 
     * Estado: Completo
     */
    public static void presentarse(){
        try{
            //Crear el socket
            Socket cl = new Socket(IPSERV,PTONVO);
            PrintWriter pw =new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
            //Obtener el nombre de la máquina
            pw.println(InetAddress.getLocalHost().getHostName());
            pw.flush();
            //Cerramos los flujos, el socket y terminamos el programa
            pw.close();
            cl.close();
        }catch(Exception e){
            System.out.println("Error enviando presentación "+e);
        }
    }
    
    /**
     * Hilo encargado de recibir peticiones o ajustes del servidor de tiempo por el puerto ptoBer <br>
     * -Recibe la petición de hora <br>
     * -Envía la hora actual <br>
     * -Recibe un ajuste con un String de tipo "5" o "-3" <br>
     * -Muestra en consola lo recibido <br>
     * -Reletiza o adelanta la hora (viaja en el tiempo) <br>
     * 
     * Estado: Pruebas
     */
    public static void hiloEscuchaHora(){
        Thread t = new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        ServerSocket s = new ServerSocket(PTOBER);
                        System.out.println("Servidor de escucha hora iniciado...");
                        while(true){
                            Socket cl= s.accept();
                            BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                            String aux = br.readLine();
                            //Si se esta pidiendo la hora
                            if(aux.equals("p")){
                                //Enviando la hora
                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                                pw.println(MuestraImage.r1.getText());
                                pw.flush();
                                pw.close();
                            }
                            //Si se esta recibiendo un ajuste
                            else{
                                //Recibiendo el ajuste
                                int ajuste = Integer.parseInt(aux);
                                System.out.println("Se recibió "+ ajuste);
                                //Obtener el tiempo
                                String msj = MuestraImage.r1.getText();
                                //Relentizar el tiempo
                                if(ajuste<0){
                                    ajuste*=-1;
                                    System.out.println("ZA WARUDO TOKI WO TOMARE!");
                                    //Relentelizar al 25%
                                    MuestraImage.setTime(msj, 0, 1000*4, true);
                                    try {
                                        Thread.sleep(ajuste*4*1000);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(AlgoritmoBerkeley.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    //Regresar al segundero normal
                                    MuestraImage.setTime(msj, 0, 1000, true);
                                    System.out.println("Toki wa ugoki dasu");
                                }
                                //Adelantar el reloj
                                else if (ajuste>0){
                                    int seg = Integer.parseInt(msj.substring(msj.lastIndexOf(":")+1));
                                    int min = Integer.parseInt(msj.substring(msj.indexOf(":")+1,msj.lastIndexOf(":")));
                                    int hor = Integer.parseInt(msj.substring(0,2));
                                    if(seg+ajuste <60) seg += ajuste;
                                    else{
                                        if(min+1<60){
                                            min++;
                                            seg = ajuste-(60-seg);
                                        }
                                        else{
                                            min = 0;
                                            if(hor+1<24){
                                                hor++;
                                                seg = ajuste-(60-seg);
                                            }
                                            else{
                                                hor=0;
                                                seg = ajuste-(60-seg);
                                            }
                                        }
                                    }
                                    String time = cadenaDig(hor)+":"+cadenaDig(min)+":"+cadenaDig(seg);
                                    System.out.println("El nuevo tiempo es "+time);
                                    MuestraImage.setTime(time, 0, 1000, true);
                                }
                                br.close();
                                cl.close();
                            }
                        }
                    } catch (IOException ex) {
                        System.out.println("Error en hilo escucha equipos ");
                        ex.printStackTrace();
                    }
                }
            }
        );
        t.start();
    }    
}
