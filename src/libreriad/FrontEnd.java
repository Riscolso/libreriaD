//Canal para el multicast de coordinador
//228.1.1.2

package libreriad;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static libreriad.RelojUsuario.setTime;

public class FrontEnd {
    public static int elsujeto = -1;
    //Trata de conectar a un coordinador y regresa su número
    public int buscarElSujeto() throws UnknownHostException{
        //Va a tratar de conectar a un coordinador, tolerancia de hasta 2 muertos
        for(int i=1;i<4;i++){
            if(MuestraImage.stillAlive(InetAddress.getByName(MuestraImage.maq+i))){
                elsujeto = i;
                return i;
            }
        }
        return -1;
    }
    
    //Le dice a los coordinadores secuandarios cuando el primario esta muerto
    // si hay servidores vivos
    //False si no hay ninguno
    public boolean chismoso() throws UnknownHostException, IOException{
        //Tolerancia de 3 nodos 
        for(int i=elsujeto;i<4;i++){
            InetAddress dir = InetAddress.getByName(MuestraImage.maq+i);
            if(MuestraImage.stillAlive(dir)){
                Socket cl = new Socket(dir, 2068);
                PrintWriter pw =new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                pw.println("Ey, psss, se cayó el principal"); //En realidad ni siquiera lee el mensaje :v
                pw.flush();
                //Va a esperar hasta que la elección acabe 
                BufferedReader br2 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                String mensaje = br2.readLine();
                if(mensaje.equals("Listo")) {
                    elsujeto = i;
                    return true;
                }
            }
        }
        return false;
    }
    
    /*Checa si el coordinador esta vivo, si sí, pide un libro
    Si no, inicia la votación en los nodos secundarios y manda */
    public String peticion() throws UnknownHostException, IOException{
        //Si el coordinador al que apunta esta muerto se escoge uno nuevo
        if(!MuestraImage.stillAlive(InetAddress.getByName(MuestraImage.maq+elsujeto))){
            //Si se escogió un nuevo coordinador
            if(chismoso()) return pedirLibro();
            //Si ya no hay ninguno
            else System.out.println("Ya valió, no hay servidores vivos");
        }
        //Si el coordinador esta vivo
        System.out.println("Entro con "+elsujeto);
        return pedirLibro();
    }
    
    //Escucha constantemenete cual es el nuevo coordinador
    public void servidorCoordinador() throws IOException{
        MulticastSocket cl = new MulticastSocket(4001);
        InetAddress gpo;
        //System.out.println("Cliente escuchando puerto "+cl.getLocalPort());
        cl.setReuseAddress(true);
        gpo=InetAddress.getByName("228.1.1.2"); //Puede entrar dentro de un rango no válido
        
        cl.joinGroup(gpo);
        System.out.println("Servidor de coordinador iniciado...");
        //DatagramPacket p = new DatagramPacket(new byte[20], 20);
        //cl.receive(p);
        Thread canal = new Thread(new Runnable(){
            @Override
            public void run(){
                while(true){
                    DatagramPacket p = new DatagramPacket(new byte[2000], 2000);
                    try {
                        cl.receive(p);
                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData()));
                        //Establecer al nuevo coordinador
                        elsujeto = dis.readInt();
                        System.out.println("Nuevo coordinador "+ elsujeto);
                    } catch (IOException ex) {
                        System.out.println("Error en hilo de servidor coordinador "+ex);
                    }
                }
            }
        });
        canal.start();
    }
    
    public String pedirLibro() throws UnknownHostException, IOException{
        Socket cl= new Socket(InetAddress.getByName(MuestraImage.maq+elsujeto),1234);
        //Hacemos una petición
        PrintWriter pw =new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
        pw.println("Dame libro >:v");
        pw.flush();     

        //Creamos un flujo de caracter ligado al socket para recibir el mensaje
        BufferedReader br2 = new BufferedReader(new InputStreamReader(cl.getInputStream()));

        //Leemos el mensaje recibido 
        String mensaje= br2.readLine();
        return mensaje;
    }
    
    
}
