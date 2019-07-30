package libreriad;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import static libreriad.MuestraImage.*;

/**
 * Para generar un reloj se necesita crear una instancia de clase; objeto
 * @author Ricardo :P
 */
public class Reloj {
    /**
     * Identificador de reloj, solo se usa en caso de que se use el multicast
     */
    public int noReloj;
    
    /**
     * Segundero de la instancia del reloj <br>
     * 1000 significa retraso de 1 segundo entre cada actualización de segundos  
     */
    public int segundero = 1000;
    
    
    /**
     * Switch de encendido/apagado del reloj. <br>
     * Ya que el método para pausar hilos esta depreciado. Básicamente ahí :v
     */
    public static boolean on = true;
    
    /**
     * Referencia al elemento gráfico ligado.
     */
    public static JButton btnr;
    
    
    /**
     * Crea una instancia de la clase lista para asignar un reloj
     * (Con eso de que Java no permite herencia múltiple xD)
     */
    public Reloj(JButton jbtn, int segundero){
        //Asignar el valor de retraso del segundero
        this.segundero = segundero;
        //Guardar la referencia al elemento gráfico; sirve para modificar la hora sin complicación
        btnr = jbtn;
    }
    
    public Reloj(){
        
    }
    
    /**
     * Recibe un elemento gráfico de tipo botón y le asigna un reloj <br>
     * El texto del botón debe ser inicializado en formato "hh:mm:ss" para funcionar <br>
     * Para cambiar el tiempo basta solo con modificar el texto del botón o bien con {@link #setTime(java.lang.String, int, int, boolean) }
     * @param lbr label en donde se mostrará el reloj
     * @param segundero tiempo de espera entre aumento de segundos (en milisegundos)
     */
    public void reloj(){
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int seg, min, hor;
                    while(true){
                        while(on){
                            Thread.sleep(segundero);
                            String tiempo = btnr.getText();
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
                            if(btnr.getText().equals(aux)){
                                btnr.setText(cadenaDig(hor)+":"+cadenaDig(min)+":"+cadenaDig(seg));
                            }
                        }
                        System.out.print(""); //Permite el correcto funcionamiento del switch, ya que si no hace nada al parecer vale kk
                    }
                } catch (Exception ex) {
                    System.out.println("Error en hilo: "+ex);
                }
            }
        });
        hilo.start();
    }
    
    /**
     * Establecer nuevo segundero al reloj <br>
     * NO actualiza los relojes de los clientes, multicast de java esta hecho basca, así que se debe usar {@link #enviarTime() } para eso.
     * @param seg velocidad del segundero en milisegundos
     */
    public void setTime(String nvoTime, int seg){
        btnr.setText(nvoTime);
        this.segundero = seg;
    }
    
    /**
     * Servidor de sincronización entre el coordinador y los clientes <br>
     * Se deben agregar todos los botones de los relojes al inicio, ya que después no se podrán sumar <br>
     * Se supone que funciona para n coordinadores y no importa las veces que se caigan o levanten...<br>
     * Se supone, lo dice en la doc de java sobre los multicast, pero no funciona bien, solo cuando quiere xD <br>
     * Activar solo un servidor por cada coordinador.
     * @param relojes Arreglo con todos los los relojes del coordinador <br>
     * ordenador ascendentemente
     */
    public static void servidorRelojes(ArrayList<JButton> relojes){
        Thread h = new Thread(new Runnable(){
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
                    
                    //Configurado para enviar a los clientes que estén suscritos al puerto 4000
                    String msj = "The cake is a lie";
                    //Datagrama para enviar
                    p = new DatagramPacket(msj.getBytes(),msj.getBytes().length,gpo,4000);
                    
                    while(true){
                        s.receive(rec);
                        System.out.println("Me llegó una petición de reloj "+ new String(rec.getData()));
                        String aux = new String(rec.getData());
                        //El primer símbolo del mensaje es un c, indica que es de un cliente
                        if(aux.charAt(0)=='c'){
                            //El segundo caracter es el número de reloj que se pide
                            int no = Integer.parseInt(aux.substring(1,2));
                            if(no+1>relojes.size()) System.out.println("Me pidieron un reloj que no tengo, WTF?");
                            else{
                                //Enviar la hora del número que pidió el usuario
                                msj = new String(relojes.get(no).getText()+no);
                                System.out.println("Voy a enviar "+msj);
                                p.setData(msj.getBytes());
                                s.send(p);
                            }
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        h.start();
    }
    
    
    /**
     * Actualiza el tiempo del relojs actual en los clientes <br>
     * Recuerda que cada reloj tiene que tener un noReloj para poder enviar sus horas<br>
     * No modifica la velocidad del segundero por que se me olvidó.. y me dio hueva agregarlo después, siente libre de hacerlo si quieres ;D
     */
    public void enviarTime(){
        //Enviar el tiempo a los clientes
        //Si el servidor yá fue iniciado
        if(p!=null){
            p.setData((btnr.getText()+noReloj).getBytes());
            try {
                s.send(p);
                System.out.println("Envio "+(btnr.getText()+noReloj) );
            } catch (IOException ex) {
                System.out.println("Error en envio "+ ex);
            }
        }
    }
    
    /**
     * Otorga formato de doble digito sobre un solo número <br>
     * Ósea que si le das un número de un dígito te regresa una cadena con un 0 antes :v
     * @param h número de un solo digito a formatear
     * @return Cadena formateada con dos digitos
     */
    public static String cadenaDig(int h){
        if(h<10) return new String("0"+h);
        else return new String(h+"");
    }
    
    /**
     * Otorga formato de doble digito sobre un solo número <br>
     * Ósea que si le das un número de un dígito te regresa una cadena con un 0 antes :v
     * @param h número de un solo digito a formatear
     * @return Cadena formateada con dos digitos
     */
    public static String cadenaDig(String c){
        if(c.length()<2) return new String("0"+c);
        else return c;
    }
    
    /**
     * Convierte una cadena de tiempo con formato "hh:mm:ss" en un arreglo de enteros
     * @param t tiempo en formato "hh:mm:ss"
     * @return Arreglo de tiempo en int, [hh][mm][ss]
     */
    public static Integer[] timeSTI(String t){
        Integer ti[] = new Integer[3];
        ti[2] = Integer.parseInt(t.substring(t.lastIndexOf(":")+1));
        ti[1] = Integer.parseInt(t.substring(t.indexOf(":")+1,t.lastIndexOf(":")));
        ti[0] = Integer.parseInt(t.substring(0,2));
        return ti;
    }
}
