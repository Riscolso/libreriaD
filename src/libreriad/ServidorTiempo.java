package libreriad;

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
        ab.hiloEscuchaEquipos();
        //ab.berkeley();
    }
}
