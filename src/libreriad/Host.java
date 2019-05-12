package libreriad;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
public class Host {
    public static void main(String[] args) {
        try{
            AlgoritmoBerkeley ab = new AlgoritmoBerkeley();
            ab.hiloEscuchaEquipos();
            
        }
        
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
