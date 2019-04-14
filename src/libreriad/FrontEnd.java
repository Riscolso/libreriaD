package libreriad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class FrontEnd {
    String maq = "E";
    //Nombres de las máquinas
    ArrayList<Integer> namae = new ArrayList<Integer>();
    //Determina si un nodo especifico esta vivo Y tiene la aplicación corriendo            
    
    public boolean stillAlive(InetAddress ia){
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
            System.out.println("Error en Still Alive");
            return false;
        }
    }
    
    /*public int iniciar(){
        for(int i=0;i< namae.size();i++){
            stillAlive("");
        }
    }*/
    public void peticion(){
        try{
            for(int i=0;i< namae.size();i++){
                
            }
            //Creamos el socket
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
            RelojUsuario.lbLibro.setText("Libro otorgado: "+ mensaje);
            //br2.close();
            //cl.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
