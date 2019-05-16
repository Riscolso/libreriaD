package libreriad;

import java.net.InetAddress;


public class Host {
    public static void main(String[] args) {
        try{
            AlgoritmoBerkeley ab = new AlgoritmoBerkeley();
            ab.enviaHora("192.168.1.74", -5);
        }
        
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
