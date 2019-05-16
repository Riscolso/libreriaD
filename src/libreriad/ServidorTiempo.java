package libreriad;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
* <h1>Servidor de tiempo</h1>
* Se encarga de sincronizar el reloj entre los nodos del sistema distribuido
* @author  Equipo 3 RULEZ
* @version 0.0
* @since   2019-04-05
*/
public class ServidorTiempo {
    AlgoritmoBerkeley ab; //Todo lo relacionado con el algoritmo
    
    public static void main (String [] args){
        new ServidorTiempo();
    }
    
    public ServidorTiempo(){
        ab = new AlgoritmoBerkeley();
        //Asignar tiempo a el servidor
        Date date = new Date();
        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
        ab.tiempo = hourFormat.format(date);
        System.out.println("El tiempo es: "+ab.tiempo);
        ab.hiloEscuchaEquipos();
        ab.berkeley();
    }
}
