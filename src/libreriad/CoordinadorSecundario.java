package libreriad;
import java.io.Serializable;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CoordinadorSecundario implements Respaldo {
    ConexiónBD con = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306/libreria2");
	
    public static void main(String args[]) {
	try {
            java.rmi.registry.LocateRegistry.createRegistry(1099); //puerto default del rmiregistry
            System.out.println("RMI registro listo.");
	} catch (Exception e) {
            System.out.println("Excepcion RMI del registry:");
            e.printStackTrace();
	  }//catch
        try {
            System.setProperty("java.rmi.server.codebase","file:/c:/Temp/Suma/");
	    CoordinadorSecundario obj = new CoordinadorSecundario();
	    Respaldo stub = (Respaldo) UnicastRemoteObject.exportObject(obj, 0);

	    // Ligamos el objeto remoto en el registro
	    Registry registry = LocateRegistry.getRegistry();
	    registry.bind("res", stub);

	    System.err.println("Servidor listo...");
	} catch (Exception e) {
	    System.err.println("Excepción del servidor: " + e.toString());
	    e.printStackTrace();
	}
        //Codigo
    }
    @Override
    public void guardarLibro(Libro l) throws RemoteException {
        con.almacenaDatos(l);
    }

    @Override
    public void respaldoPeticion(Peticion p) throws RemoteException {
        con.almacenaPedido(p);
        con.almacenaUsuario(p);
        con.almanenaUsuarioSes(p);
        System.out.println("Se guardó la ip: "+p.getIp());
        System.out.println("Con el libro: "+p.getLibro());
        System.out.println("A las : "+p.getHora());
    }
    
    @Override
    public boolean reiniciarSesion() throws RemoteException{
        //modifiDispo regresa true si todo se hizo bien
        //Y false si algo explotó
        return con.modifDispo();
    }
}
