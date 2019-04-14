package libreriad;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class LibreriaD {

    public static void main(String[] args) throws ParseException {
        System.out.println(idLibro());
       
    }
    public static int idLibro() {
        Random r = new Random();
        int num = r.nextInt(25)+1;
        return num;
    }
}
    

