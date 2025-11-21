package proyecto.entidades;

public class Libro {

    private String isbn;
    private String titulo;
    private String autor;
    private int total_ejemplares;
    private int disponibles_sede_1;
    private int disponibles_sede_2;

    public Libro() {

    }

    public Libro(String isbn, String titulo, String autor, int totalEjemplares, int disponiblesSede1, int disponiblesSede2) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.total_ejemplares = totalEjemplares;
        this.disponibles_sede_1 = disponiblesSede1;
        this.disponibles_sede_2 = disponiblesSede2;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getTotal_ejemplares() {
        return total_ejemplares;
    }

    public void setTotal_ejemplares(int total_ejemplares) {
        this.total_ejemplares = total_ejemplares;
    }

    public int getDisponibles_sede_1() {
        return disponibles_sede_1;
    }

    public void setDisponibles_sede_1(int disponibles_sede_1) {
        this.disponibles_sede_1 = disponibles_sede_1;
    }

    public int getDisponibles_sede_2() {
        return disponibles_sede_2;
    }

    public void setDisponibles_sede_2(int disponibles_sede_2) {
        this.disponibles_sede_2 = disponibles_sede_2;
    }
}
