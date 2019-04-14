package libreriad;

import java.io.Serializable;

public class Peticion implements Serializable {
    String ip, hora, libro, fecha;
    
    
    public Peticion(){
        
    }
    
    //Constructor para cuando se va a reiniciar la sesion
    public Peticion(String ip){
        this.ip = ip;
    }
    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getLibro() {
        return libro;
    }

    public void setLibro(String libro) {
        this.libro = libro;
    }
    
}
