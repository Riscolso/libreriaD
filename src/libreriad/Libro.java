package libreriad;

import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.swing.JOptionPane;
import static libreriad.MuestraImage.*;
import static libreriad.Replicacion.*;
import static libreriad.Reloj.tiempo;

public class Libro implements Serializable{
    int ISBN;
    String nombre, autor, editorial, ruta;
    float precio;
    FileInputStream fi;

    //CÓDIGO QUE TRAE EL NÚMERO DE LIBROS DISPONIBLES Y LO ASIGNA ABAJO :P
    public int idLibro (){
        Random r = new Random();
        int num = r.nextInt(25)+1;   
        //idDis=num;
        return num;
    }
    
    //Hilo de las peticiones de libros
    public void servidorPeticiones(){
        Thread hilo = new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    ServerSocket s = new ServerSocket(1234);
                    System.out.println("Servidor de peticiones inciado...");
                    while(true){
                        noLibros=con.obtenerLibros();
                        lbLibros.setText("Libros disponibles: "+ noLibros);
                        Socket cl= s.accept();
                        Peticion p = new Peticion();

                        //Obtener la IP del cliente
                        p.setIp(cl.getInetAddress()+":"+cl.getPort());
                        System.out.println("Conexión establecida desde "+p.getIp());

                        //Ligamos un printwriter a un flujo de salida de caracter
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));

                        //Si hay libros disponibles
                        if(noLibros!=0){

                            //p.setLibro(obtenerNombreLibro());

                            //Establecer la hora local
                            p.setHora(tiempo[0]);
                            //Aquí checa la disponibilidad del libro gg.
                            while(true){
                                int aux = idLibro();
                                //System.out.println("Aqui: "+ con.obtenerDisp(aux));
                                if("Disponible".equals(con.obtenerDisp(aux))){
                                    p.setLibro(con.obtenerNombreLibro(aux));
                                    jNomLibro.setText(p.getLibro());
                                    muestraImagen();
                                    JOptionPane.showMessageDialog(null, "Libro Prestado");
                                    break;
                                }
                            }

                            //Obtener la fecha del día
                            Date d = new Date();
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            String ss = dateFormat.format(d);
                            p.setFecha(ss);

                            //Registrar en la base de datos la petición, el usuario y modificar la disponibilidad
                            con.almacenaUsuario(p);
                            con.almacenaPedido(p);
                            con.almanenaUsuarioSes(p);
                            con.modifDisp(p.getLibro());

                            //Replicación a todos los nodos secundarios
                            if(replicacion(p)) System.out.println("Todo salió bien en los secundarios");

                            //Hasta que todos hayan contestado, se envía al cliente
                            System.out.println("Nombre del libro: "+p.getLibro());
                            pw.println(p.getLibro());  //Enviar el nombre del libro
                            pw.flush();

                            noLibros=con.obtenerLibros();
                            lbLibros.setText("Libros disponibles: "+ noLibros);
                        }
                        else{
                            //Enviar que ya no hay libros
                            pw.println("Ya no hay >:v");//Supongo que ningún libro se llama así xD
                            pw.flush();
                            JOptionPane.showMessageDialog(null, 
                              "Ya no hay libros :C", 
                              "Changos!", 
                              JOptionPane.WARNING_MESSAGE);
                            lbLibros.setText("Libros disponibles: SOLD OUT");
                        }
                    }
                }catch(Exception ex){
                    System.out.println("Error en el hilo del servidor de libros "+ex);
                }
            }
            });
        hilo.start();
    } 
    
    public FileInputStream getFi() {
        return fi;
    }

    public void setFi(FileInputStream fi) {
        this.fi = fi;
    }
    
    public Libro(){
        
    }

    public int getISBN() {
        return ISBN;
    }

    public void setISBN(int ISBN) {
        this.ISBN = ISBN;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }
    
}
