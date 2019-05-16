package libreriad;

import java.net.InetAddress;


public class Host {
    public static void main(String[] args) {
        try{
            AlgoritmoBerkeley ab = new AlgoritmoBerkeley();
            //ab.enviaHora("192.168.1.74", -5);
            //ab.calcularLatencia(InetAddress.getByName("127.0.0.1"));
            System.out.println("Priemro "+ ab.timeASeg("23:59:59"));
            System.out.println("Esto "+ ab.segATime(ab.timeASeg("23:59:59")));
        }
        
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
