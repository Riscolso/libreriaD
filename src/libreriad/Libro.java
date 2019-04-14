package libreriad;

import java.io.FileInputStream;
import java.io.Serializable;

public class Libro implements Serializable{
    int ISBN;
    String nombre, autor, editorial, ruta;
    float precio;
    FileInputStream fi;

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
