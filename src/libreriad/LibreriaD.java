package libreriad;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class LibreriaD {
    //Para base de datos
    
    public static void main(String[] args) throws ParseException {
        ConexiónBD con = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306/libreriad");
        con.borrarBD();
        ConexiónBD con2 = new ConexiónBD("root", "root", "jdbc:mysql://localhost:3306");
        con2.crearBD();
        con.cargarBD();
    }
    public static int idLibro() {
        Random r = new Random();
        int num = r.nextInt(25)+1;
        return num;
    }
}
    

