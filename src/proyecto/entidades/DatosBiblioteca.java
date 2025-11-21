package proyecto.entidades;

import java.util.ArrayList;
import java.util.List;

public class DatosBiblioteca {

    private List<Libro> libros;
    private List<Prestamo> prestamos;
    private List<LogOperacion> log_operaciones;

    public DatosBiblioteca() {
        libros = new ArrayList<>();
        prestamos = new ArrayList<>();
        log_operaciones = new ArrayList<>();
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    public List<LogOperacion> getLog_operaciones() {
        return log_operaciones;
    }

    public void setLog_operaciones(List<LogOperacion> log_operaciones) {
        this.log_operaciones = log_operaciones;
    }
}
