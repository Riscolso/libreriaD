package libreriad;

import java.net.InetAddress;


public class Host {
    public static void main(String[] args) {
        try{
            while(true){
                System.out.println("La ip es: "+InetAddress.getByName("E2"));
                Thread.sleep(400);
            }
        }
        
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
