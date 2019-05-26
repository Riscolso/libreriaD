package libreriad;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import static libreriad.MuestraImage.*;

/**
 * Genera relojes y el control de estos ya sea local o remoto
 * @author Ricardo
 */
public class Reloj {
    
    /*Arreglo con Todos los botones que muentran la hora
    Pa' modificarlos por bonche, mas fácil :)*/
    public static JButton BRelojes[];
    /**
     * Arreglo que contiene los 4 tiempos de los relojes en formato hh:mm:ss
     */
    public static String tiempo[];
    /**
     * Arreglo que contiene la velocidad de los segunderos de los relojs... o relojes?
     */
    public static int segundero[];
    
    /**
     * Arreglo con el switch de encendido/apagado de los relojes.
     */
    public static boolean on[];
    
    //Todo lo relacionado con crear los relojes, su hora, los hilos que lo manejan ETC
    public void iniciarRelojes(){
        
        //Llenar el arreglo de los botones
        BRelojes = new JButton[4];
        BRelojes[0] = r1;
        BRelojes[1] = r2;
        BRelojes[2] = r3;
        BRelojes[3] = r4;
        
        //Instanciar la ventana de modificar
        
        tiempo = new String[4];
        segundero = new int[4];
        on = new boolean[4];
        Thread  hilo[] = new Thread[4];
        
        //Obtener la hora local para el primer reloj
        Date date = new Date();
        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
        BRelojes[0].setText(hourFormat.format(date));
        int i;
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
                        System.out.print("");
                        }
                        } catch (Exception ex) {
                            System.out.println("Error en hilo "+e+": "+ex);
                        }
                }
            });
            
            
            //Iniciar los hilos
            hilo[i].start();
            
        }
        
        //Asignar hora random a los demás relojes
        for(int j=1;j<4;j++)
            setTime(cadenaDig((int) (Math.random() * 23))+":"+
                    cadenaDig((int) (Math.random() * 59))+":"+
                    cadenaDig((int) (Math.random() * 59)), j, 1000, false);
    }
    
    //Ejecutable del hilo que actualiza el tiempo en relojes cliente - Multicast
    public void servidorRelojes(){
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
        });
        h.start();
    }
    
    /**
     * Establece un nuevo valor para uno de los relojes, así como su velocidad y si los cambios deben ser enviados a los clientes
     * @param nvoTime Nuevo tiempo del reloj
     * @param noReloj Número del reloj a modificar
     * @param seg Velocidad del segundero 1000 es normal, 500 es el doble de la velocidad
     * @param b True si debe ser enviado a los clientes del reloj especificado, false pos no.
     */
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
}
