package libreriad;

public class Equipo {
    private String ip;
    private String nombre;
    private int  latencia; //En milisegundos
    private int id;
    
    public Equipo(){
        
    }

    public Equipo(String ip, String nombre, int latencia, int id) {
        this.ip = ip;
        this.nombre = nombre;
        this.latencia = latencia;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getLatencia() {
        return latencia;
    }

    public void setLatencia(int latencia) {
        this.latencia = latencia;
    }

    
}
