package proyecto.entidades;

public class Prestamo {

    String id_prestamo;
    String isbn;
    String sede_prestamo;
    String fecha_prestamo;
    String fecha_devolucion_estimada;
    String estado;

    public Prestamo() {

    }

    public Prestamo(String id_prestamo, String isbn, String sede_prestamo, String fecha_prestamo, String fecha_devolucion_estimada, String estado) {
        this.id_prestamo = id_prestamo;
        this.isbn = isbn;
        this.sede_prestamo = sede_prestamo;
        this.fecha_prestamo = fecha_prestamo;
        this.fecha_devolucion_estimada = fecha_devolucion_estimada;
        this.estado = estado;
    }

    public String getId_prestamo() {
        return id_prestamo;
    }

    public void setId_prestamo(String id_prestamo) {
        this.id_prestamo = id_prestamo;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getSede_prestamo() {
        return sede_prestamo;
    }

    public void setSede_prestamo(String sede_prestamo) {
        this.sede_prestamo = sede_prestamo;
    }

    public String getFecha_prestamo() {
        return fecha_prestamo;
    }

    public void setFecha_prestamo(String fecha_prestamo) {
        this.fecha_prestamo = fecha_prestamo;
    }

    public String getFecha_devolucion_estimada() {
        return fecha_devolucion_estimada;
    }

    public void setFecha_devolucion_estimada(String fecha_devolucion_estimada) {
        this.fecha_devolucion_estimada = fecha_devolucion_estimada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
