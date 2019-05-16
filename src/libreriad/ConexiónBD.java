package libreriad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConexiónBD {
    
    private String user;
    private String password;
    private String url;
    private Connection con=null;

    public ConexiónBD(String user, String password, String url) {
        this.user = user;
        this.password = password;
        this.url = url;
    }
    
    
    public Connection getConnection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con=DriverManager.getConnection(url,user,password);
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConexiónBD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ConexiónBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    return con;
    }
    
    public void almacenaDatos(Libro l){
        Connection con= getConnection();
        String insertar = "INSERT INTO Libro (ISBN, Nombre, Autor, Editorial, Precio, Portada) VALUES (?,?,?,?,?,?)";
        //FileInputStream fi;
        PreparedStatement ps;
        try{
            //File file = new File(l.getRuta());
            //fi = new FileInputStream(file);
            
            ps = con.prepareStatement(insertar);
            ps.setInt(1, l.getISBN());
            ps.setString(2,l.getNombre());
            ps.setString(3, l.getAutor());
            ps.setString(4, l.getEditorial());
            ps.setFloat(5, l.getPrecio());
            ps.setBinaryStream(6, l.getFi());
            ps.executeUpdate();
            System.out.println("Libro Almacenado");
           
        } catch(Exception ex){
            System.out.println("Error al guardar datos, ggg salu2.");
            System.out.println(ex);
        }
    }
    
    public void almacenaUsuario(Peticion p) {
        Connection con = getConnection();
        String insertar= "INSERT INTO Usuario (IP) VALUES (?)";
        PreparedStatement ps;
        try{
            ps=con.prepareStatement(insertar);
            ps.setString(1, p.getIp());
            ps.executeUpdate();
            System.out.println("Usuario añadido, ggg.");
        }catch(Exception ex){
            System.out.println("Error al guardar Usuario, ggg salu2.");
            System.out.println(ex);
        }
    }
    
    public void almacenaPedido(Peticion p){
        Connection con = getConnection(); 
        String insertar= "INSERT INTO Pedido (Fecha, Hora_Inicio) VALUES (?,?)";
        PreparedStatement ps;
        try{
            ps=con.prepareStatement(insertar);
            ps.setString(1, p.getFecha());
            ps.setString(2, p.getHora());
            ps.executeUpdate();
            System.out.println("Pedido añadido, ggg.");
            almacenaSesion(p.getHora(),p.getLibro());
        }catch(Exception ex){
            System.out.println("Error al guardar Pedido, ggg salu2.");
            System.out.println(ex);
        }
    }
    
    public void almacenaSesion(String hora, String nom){
        Connection con = getConnection(); 
        String insertar= "INSERT INTO Sesion (ID_Pedido, ID_Libro) SELECT Pedido.ID, Libro.ISBN FROM Pedido, Libro"
                + " WHERE Pedido.Hora_Inicio=? AND Libro.Nombre=?";
        PreparedStatement ps;
        try{
            ps=con.prepareStatement(insertar);
            ps.setString(1, hora);
            ps.setString(2, nom);
            ps.executeUpdate();
            System.out.println("Sesion añadida, ggg.");
        }catch(Exception ex){
            System.out.println("Error al guardar la sesion, gg salu2.");
            System.out.println(ex);
        }
    }

    public void almanenaUsuarioSes(Peticion p){
        Connection con = getConnection();
        String insertar = "INSERT INTO UsuarioSesion (ID_Usuario, ID_Pedido) SELECT u.ID AS Usuario, p.ID AS Pedido "
                + "FROM Usuario u, Pedido p "
                + "WHERE u.IP=? AND p.Fecha=?";
        PreparedStatement ps;
        try{
            ps=con.prepareStatement(insertar);
            ps.setString(1, p.getIp());
            ps.setString(2, p.getFecha());
            ps.executeUpdate();
            System.out.println("UsuarioSesion agregada, ggg.");
        }catch(Exception ex){
            System.out.println("Error al añadir UsuarioSesion, salu2 a todos.");
            System.out.println(ex);
        }
    }
    
    public int obtenerLibros(){
        Connection con = getConnection();
        PreparedStatement s=null;
        ResultSet rs;
        int noLibros=0;
        //ConexiónBD bd = new ConexiónBD("libreria", "root", "root", "jdbc:mysql://localhost:3306/libreriad");
        
        try{
            s = con.prepareStatement("SELECT COUNT(Disponibilidad) FROM Libro WHERE Disponibilidad='Disponible'");
            rs = s.executeQuery();
            while (rs.next()) {
                noLibros = rs.getInt(1);
                //System.out.println(noLibros);
            }
        }catch(SQLException ex){
            System.err.println(ex);
        }
        return noLibros;
    }
    
    
    public void modifDisp(String nombre){
        PreparedStatement s=null; 
        Connection con = getConnection();
        //ConexiónBD bd = new ConexiónBD("libreria", "root", "root", "jdbc:mysql://localhost:3306/libreriad");
        
        try{
            s = con.prepareStatement("UPDATE Libro SET Disponibilidad=? WHERE Nombre=?");
            s.setString(1, "No Disponible");
            s.setString(2, nombre);
            s.executeUpdate();
            
        }catch(SQLException ex){
            System.err.println(ex);
        } 
    }
    
    public String obtenerNombreLibro(int num){
        PreparedStatement s=null;
        ResultSet rs;
        String nombL="";
        Connection con = getConnection();
        try{
            s = con.prepareStatement("SELECT Nombre FROM Libro WHERE ISBN=?");
            s.setInt(1, num);
            rs = s.executeQuery();
            while (rs.next()) {
                nombL = rs.getString("Nombre");                  
            }
        }catch(SQLException ex){
            System.err.println(ex);
        }
        return nombL;
    }
    
    public boolean modifDispo(){
        Connection con = getConnection();
        PreparedStatement s=null; 
        
        try{
            s = con.prepareStatement("UPDATE Libro SET Disponibilidad=?");
            s.setString(1, "Disponible");
            s.executeUpdate();
            //En caso de que todo salga bien regresa true
            return true;
        }catch(SQLException ex){
            System.err.println(ex);
            //Si algo explota regresa false
            return false;
        }  
    }
    
    public void respaldarBD(){
        try {
            Process p = Runtime.getRuntime().exec("C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump -u root -proot libreriad");
            InputStream is = p.getInputStream();
            FileOutputStream fos = new FileOutputStream("respaldito.sql");

            byte[] buffer = new byte[1000];

            int leido = is.read(buffer);
            while (leido > 0) {
                fos.write(buffer, 0, leido);
                leido = is.read(buffer);
            }
            fos.close();

        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    public void cargarBD(){
        try {
            Process p = Runtime.getRuntime().exec("C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql -u root -proot libreriad");
            OutputStream os = p.getOutputStream();
            FileInputStream fis = new FileInputStream("respaldito.sql");

            byte[] buffer = new byte[1000];

            int leido = fis.read(buffer);
            while (leido > 0) {
                os.write(buffer, 0, leido);
                leido = fis.read(buffer);
            }
            os.flush();
            os.close();
            fis.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    public String obtenerDisp(int d){
    PreparedStatement s=null;
    ResultSet rs;
    String dispL="";
    Connection con = getConnection();
    try{
        s = con.prepareStatement("SELECT Disponibilidad FROM Libro WHERE ISBN="+d);
        rs = s.executeQuery();
        while (rs.next()) {
            dispL = rs.getString("Disponibilidad");     
        }
    }catch(SQLException ex){
        System.err.println(ex);
    }
    return dispL;
    }
    
    public void crearBD(){
        Connection con = getConnection();
        PreparedStatement s=null; 
        try{
            s = con.prepareStatement("CREATE DATABASE libreriad");
            s.executeUpdate();
            System.out.println("BD creada correctamente.");
    } catch (SQLException ex) {
            System.out.println("Base de datos existente.");
        }
    }
    
    public void borrarBD(){
        Connection con = getConnection();
        PreparedStatement s=null; 
        try{
            s = con.prepareStatement("DROP DATABASE libreriad");
            s.executeUpdate();
            System.out.println("BD borrada correctamente.");
    } catch (SQLException ex) {
            System.out.println("Error al borrar la BD");
        }
    }
    
    /**
    * Obtiene la información de los equipos, la encapsula en objetos tipo "Equipo"
    * y los agrega a una lista
    * @return ArrayList [&#8249;]Equipo[&#8250;] Todos los equipos de la base de datos
    */
    public ArrayList<Equipo> obtenerEquipos(){
        ArrayList<Equipo> equipos = new ArrayList<Equipo>();
        Connection con = getConnection();
        PreparedStatement ps=null;
        ResultSet rs;
        Equipo e;
            try{
                ps = con.prepareStatement("SELECT*FROM Equipos");
                rs = ps.executeQuery();
                while (rs.next()) {
                    e = new Equipo(
                        rs.getString("IP"),
                        rs.getString("Nombre"),
                        rs.getInt("Latencia")
                    );
                    equipos.add(e);
                }
            }catch(SQLException ex){
                System.out.println(ex);
            }
        return equipos;
    }
    /*  Con un for obtener datos de todos alv 
     for(int i = 0; i < con.obtenerEquipos().size();i++){
            System.out.println("Información: "+con.obtenerEquipos().get(i).getIp()+", "+con.obtenerEquipos().get(i).getNombre());   
       }
    */
    
    /**
    * Registra un equipo en la base de datos y regresa su id de la BD
    * @param e objeto con ip, nombre y latencia que se va a registrar
    * @return El Id asignado a el equipo recien registrado en la BD
    */
    public int registrarEquipo(Equipo e){
        Connection con = getConnection();
        String ins= "INSERT INTO Equipos (IP, Nombre, Latencia) VALUES (?,?,?)";
        PreparedStatement ps;
        try{
            ps=con.prepareStatement(ins);
            ps.setString(1, e.getIp());
            ps.setString(2, e.getNombre());
            ps.setInt(3, e.getLatencia());
            ps.executeUpdate();
            System.out.println("Equipo guardado, ggg.");
        }catch(Exception ex){
            System.out.println("Error al guardar Equipo, ggg salu2.");
            System.out.println(ex);
        }

        return e.getId();
    }
    
    //Metodo que regresa el ID con base al metodo de arribita, ggg salu2.
    public int idEquipo(String nombre){
        Connection con = getConnection();
        PreparedStatement ps;
        ResultSet rs;
        int id=0;
        try {
            ps = con.prepareStatement("SELECT ID FROM Equipos WHERE Nombre=?");
            ps.setString(1, nombre);
            rs = ps.executeQuery();
            while (rs.next()) {
                id = rs.getInt("ID");
            }
        } catch (SQLException ex) {
                System.err.println(ex);
        }
        return id;
    }
    
    /**
     * Registra en las tablas HoraCentral y Hora equipos los parametros dados
     * @param hp hPrev de Hora central
     * @param hr hRef de Horal central
     * @param idyhorEqui Mapa hash ("arreglo") con la dupla de Id del equipo y su hora, IDEquipo y h equipo de la tabla Hora equipos 
     * @param ad tiempo que se adelantó el reloj (en segundos)
     * @param rel tiempo que se retrasó el reloj (en segundos)
     * @see <a href="https://jarroba.com/map-en-java-con-ejemplos" > HasMaps </a> para saber como obtener los datos de la dupla
     */
    
    //Se reciben los parámetros correspondientes, una vez que entra el Map, se regresa la llave y el valor para esos mismos 
    //llevarlos a la base de datos en un UPDATE ggg.
    public void registrarHora(int hp, int hr, Map<Integer, String> idyhorEqui, int ad, int rel){
            Iterator regresa = idyhorEqui.keySet().iterator();
            Integer key = (Integer) regresa.next();
            String val = idyhorEqui.get(key);
            Connection con = getConnection();
            
            String ins = "INSERT INTO HoraCentral (hPrev, hRef) VALUES ("+hp+","+hr+")";
            String ins2 = "INSERT INTO HoraEquipos (IDhSincr) SELECT ID FROM HoraCentral WHERE hPrev="+hp+" AND hRef="+hr+" ";
            String ins3 = "UPDATE HoraEquipos SET IDEquipo="+key+", hEquipo="+"'"+val+"'"+", aEquipo="+ad+", ralentizar="+rel+" "
                    + "WHERE IDhSincr = (SELECT ID FROM HoraCentral "
                    + "WHERE hPrev="+hp+" AND hRef="+hr+")";
            System.out.println(ins3);
        try {
            Statement ps = con.createStatement();
            ps.addBatch(ins);
            ps.addBatch(ins2);
            ps.addBatch(ins3);
            ps.executeBatch();
        } catch (SQLException ex) {
            Logger.getLogger(ConexiónBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
