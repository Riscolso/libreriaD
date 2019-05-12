//Cuidado en donde poner los try-catch dentro de los ciclos infinitos
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
* @version 0.0
* @since   2019-04-05
*/

public class AlgoritmoBerkeley {
    /**
     * La hora buena
     */
    String hora;
    /**
     * Cada cuando se debe sincronizar la hora
     */
    int y;
    /**
     * Lista de todos los equipos que se han conectado al servidor
     */
    public ArrayList<Equipo> equipos = new ArrayList<Equipo>();
    final static int PTOBER = 2070; //El puerto definido para recibir y enviar tramas de este algorimto
    final static int PTONVO = 2071; //Puerto definido para cuando un nodo nuevo inicia
    ConexiónBD con = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306/      INSERTE NOMBRE DE LA BASE DE DATOS      "); //Objeto para usar la base de datos
    final static String IPSERV = "localhost"; //Dirección ip del servidor de tiempo
    public AlgoritmoBerkeley(){
        
    }
    
    /**
     * Calcula el tiempo (en segundos) de retardo entre los ciclos de sincronización de hora 2PS (P=latencia máxima,S=Tolerancia) <br>
     * Obtener P de {@link #obtenerLatenciaMax() } <br>
     * S es al gusto
     * Completado.
     */
    public void calcularY(){
        //Tolerancia de 1 minuto
        y = 2*obtenerLatenciaMax()*60;
    }
    
    /**
     * Pide la hora de todos los equipos; las ips se obtienen del arreglo "equipos"
     * @return Areeglo de enteros con las horas de los equipos
     */
    public ArrayList<String> pedirHora(){
        ArrayList<String> horas = new ArrayList<String>();
        return horas;
    }
    
    /**
     * Envia la nueva hora y su valor de ajuste a un equipo especificada <br>
     * La trama se envía a ip con el puerto ptoBer
     * Debe ser un String con la forma "5" 0 "-3"
     * @param ip Dirección del equipo al que se va a enviar 
     * @param ajuste Que tanto debe de adelantarse(cantidad) o relentizarse(-cantidad) el reloj del nodo (en segundos)
     */
    public void enviaHora(String ip, int ajuste){
        
    }
    
    /**
     * Obtiene la latencia ente la computadora actual y al especificada en la ip
     * @param ip computadora a la cual se va a calcular la latencia
     * @return latencia en segundos
     */
    public int calcularLatencia(InetAddress ip){
        return 1;
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
     */
    public void berkeley(){
        Thread t = new Thread(new Runnable(){
                @Override
                public void run(){
                    while(true){
                        
                    }
                }
            }
        );
        t.start();
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
     * Estado: En pruebas con nombre de IP
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
                            Equipo e = new Equipo(ia.getHostAddress(), br.readLine(), calcularLatencia(ia));
                            //Guardar en la BD  y asignar su Id al objeto
                            e.setId(con.registrarEquipo(e));
                            //Agregar a equipos
                            equipos.add(e);
                            //Imprimir datos
                            System.out.println("Se encontró un nuevo nodo, guadado en la base de datos");
                            e.imprimirEquipo();
                            //Calcular el nuevo tiempo de retraso para sincronización
                            calcularY();
                            //Cerrar flujos
                            cl.close();
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
            System.out.println("Error enviando presentación");
            e.printStackTrace();
        }
    }
    
    /**
     * Hilo encargado de recibir peticiones del servidor de tiempo por el puerto ptoBer <br>
     * 1.-Recibe la petición de hora <br>
     * 2.-Envía la hora actual <br>
     * 3.-Recibe una trama con un String de tipo "5" o "-3" <br>
     * 4.-Muestra en consola lo recibido <br>
     * 5.-Reletiza (ZA WARUDO TOKI WA TOMARE!) o adelanta la hora (viaja en el tiempo) <br>
     * 
     * Estado: En pruebas
     */
    public static void hiloEscuchaHora(){
        Thread t = new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        ServerSocket s = new ServerSocket(PTOBER);
                        while(true){
                            System.out.println("Servidor de escucha hora iniciado...");
                            Socket cl= s.accept();
                            System.out.println("Alguien me pidió la hora");
                            //Enviando la hora
                            String msj = MuestraImage.r1.getText();
                            //String msj = "01:01:09";
                            PrintWriter pw =new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                            pw.println(msj);
                            pw.flush();
                            //Recibiendo la hora
                            BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                            int ajuste = Integer.parseInt(br.readLine());
                            System.out.println("Se recibió "+ ajuste);
                            //Volver a obtener el tiempo
                            msj = MuestraImage.r1.getText();
                            //Relentizar el tiempo
                            if(ajuste<0){
                                ajuste*=-1;
                                System.out.println("ZA WARUDO TOKI WO TOMARE! ");
                                MuestraImage.setTime(msj, 0, 0, false);
                                try {
                                    Thread.sleep(ajuste*1000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(AlgoritmoBerkeley.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                MuestraImage.setTime(msj, 0, 1000, false);
                                System.out.println("Toki wa ugoki dasu");
                            }
                            //Adelantar el reloj
                            else if (ajuste>0){
                                int seg = Integer.parseInt(msj.substring(msj.lastIndexOf(":")+1));
                                int min = Integer.parseInt(msj.substring(msj.indexOf(":")+1,msj.lastIndexOf(":")));
                                int hor = Integer.parseInt(msj.substring(0,2));
                                if(seg+ajuste <59) seg += ajuste;
                                else{
                                    if(min<59){
                                        min++;
                                        seg -= ajuste; 
                                    }
                                    else{
                                        min = 0;
                                        if(hor<23){
                                            hor++;
                                            seg -= ajuste;
                                        }
                                        else hor=0;
                                    }
                                }
                                String time = cadenaDig(hor)+":"+cadenaDig(min)+":"+cadenaDig(seg);
                                System.out.println("El nuevo tiempo es :"+time);
                                MuestraImage.setTime(time, 0, 1000, false);
                            }
                            br.close();
                            pw.close();
                            cl.close();
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