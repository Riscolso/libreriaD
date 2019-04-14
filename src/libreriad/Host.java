package libreriad;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
public class Host {
   public static void main(String[] args) {
      byte[] b;
      try{
          ArrayList<Integer> n = new ArrayList<Integer>();
          ArrayList<Integer> la = new ArrayList<Integer>();
          n.add(1);
          la.add(4);
          n.add(2);
          //trma
          n.add(3);
          //siguites
          la.add(2);
          
          la.add(3);
          
            /*for(int c:la){
                if(!n.contains(c)) n.add(c);
            }*/
            Comparator<Integer> comparador = Collections.reverseOrder();
            Collections.sort(n, comparador);
          System.out.println("Numeros "+n.subList(0, n.size()));
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }
}
