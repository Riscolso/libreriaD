package libreriad;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public interface Respaldo extends Remote {
    void guardarLibro(Libro l)throws RemoteException;
    void respaldoPeticion(Peticion p) throws RemoteException;
    boolean reiniciarSesion() throws RemoteException; 
}
