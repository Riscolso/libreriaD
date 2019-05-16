package libreriad;

public class Equipo {
    private String ip;
    private String nombre;
    private int  latencia; //En milisegundos
    private int id;
    private String hEquipo;
    private int adelantar, relentizar;
    
    public Equipo(){
        
    }

    public Equipo(String ip, String nombre, int latencia) {
        this.ip = ip;
        this.nombre = nombre;
        this.latencia = latencia;
    }

    public String gethEquipo() {
        return hEquipo;
    }

    public void sethEquipo(String hEquipo) {
        this.hEquipo = hEquipo;
    }

    public int getAdelantar() {
        return adelantar;
    }

    public void setAdelantar(int adelantar) {
        this.adelantar = adelantar;
    }

    public int getRelentizar() {
        return relentizar;
    }

    public void setRelentizar(int relentizar) {
        this.relentizar = relentizar;
    }
    
    /**
     * Muestra la información del objeto
     */
    public void imprimirEquipo(){
        System.out.println("Id: "+ getId());
        System.out.println("Nombre: "+getNombre());
        System.out.println("Ip: "+ getIp());
        System.out.println("Latencia: "+getLatencia());
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
