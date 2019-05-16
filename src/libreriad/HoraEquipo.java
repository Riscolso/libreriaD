package libreriad;


public class HoraEquipo {
    int IDEquipo;
    String hEquipo;
    int adelantar, relentizar;

    public HoraEquipo(int IDEquipo, String hEquipo, int adelantar, int relentizar) {
        this.IDEquipo = IDEquipo;
        this.hEquipo = hEquipo;
        this.adelantar = adelantar;
        this.relentizar = relentizar;
    }

    public int getIDEquipo() {
        return IDEquipo;
    }

    public void setIDEquipo(int IDEquipo) {
        this.IDEquipo = IDEquipo;
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
    
    
}
