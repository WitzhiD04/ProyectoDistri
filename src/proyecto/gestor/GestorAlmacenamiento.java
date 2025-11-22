package proyecto.gestor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import proyecto.entidades.DatosBiblioteca;
import proyecto.entidades.Libro;
import proyecto.entidades.LogOperacion;
import proyecto.entidades.Prestamo;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class GestorAlmacenamiento {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String idSede;
    private String nomArchivo;
    private DatosBiblioteca datosBiblioteca;
    private static final SimpleDateFormat fechaFormato = new SimpleDateFormat("yyyy-MM-dd");

    public GestorAlmacenamiento(String idSede) {
        this.idSede = idSede;
        datosBiblioteca = new DatosBiblioteca();
        this.nomArchivo = "./db/" + idSede + "/biblioteca_data.json";
        cargarDatos();
    }

    private void cargarDatos() {
        try (FileReader reader = new FileReader(nomArchivo)) {
            datosBiblioteca = gson.fromJson(reader, DatosBiblioteca.class);
            System.out.println("GA - Sede " + idSede + " cargó " + datosBiblioteca.getLibros().size() + " libros y " + datosBiblioteca.getPrestamos().size() + " préstamos.");
        } catch (IOException e) {
            System.err.println("GA - Sede " + idSede + " ERROR al cargar BD: " + e.getMessage());
            datosBiblioteca = new DatosBiblioteca();
        }
    }

    public synchronized void guardarCambios() {
        try (FileWriter writer = new FileWriter(nomArchivo)) {
            gson.toJson(datosBiblioteca, writer);
        } catch (IOException e) {
            System.err.println("GA - Sede " + idSede + " ERROR al guardar cambios: " + e.getMessage());
        }
    }

    public Libro buscarLibro(String isbn) {
        List<Libro> libros = datosBiblioteca.getLibros();

        if (libros == null) {
            return null;
        }

        for (Libro libro : libros) {
            if (libro.getIsbn().equals(isbn)) {
                return libro;
            }
        }
        return null;
    }

    public Prestamo buscarPrestamo(String idPrestamo) {
        List<Prestamo> prestamos = datosBiblioteca.getPrestamos();

        if (prestamos == null) {
            return null;
        }

        for (Prestamo prestamo : prestamos) {
            if (prestamo.getIsbn().equals(idPrestamo)) {
                return prestamo;
            }
        }
        return null;
    }

    public synchronized LogOperacion registrarPrestamo (String isbn, String sedeOperacion) {
        Libro libroPres = buscarLibro(isbn);

        if (libroPres == null) {
            System.out.println("Fallo Préstamo: Libro no existe.");
            return null;
        }

        int disponibles;

        if (sedeOperacion.equals("sede1")) {
            disponibles = libroPres.getDisponibles_sede_1();
        } else {
            disponibles = libroPres.getDisponibles_sede_2();
        }

        if (disponibles <= 0) {
            System.out.println("Error en el Préstamo: No hay stock del libro en " + sedeOperacion);
            return null;
        }

        if (sedeOperacion.equals("sede1")) {
            libroPres.setDisponibles_sede_1(disponibles - 1);
        } else {
            libroPres.setDisponibles_sede_2(disponibles - 1);
        }

        String idPrestamo = "P" + (datosBiblioteca.getPrestamos().size() + 1);
        String fechaPrestamo = fechaFormato.format(new Date());
        String fechaDevolucion = fechaFormato.format(new Date(System.currentTimeMillis() + (14L * 24L * 60L * 60L * 1000L)));

        Prestamo nuevoPrestamo = new Prestamo(idPrestamo, isbn, sedeOperacion, fechaPrestamo,fechaDevolucion, "PRESTADO");
        datosBiblioteca.getPrestamos().add(nuevoPrestamo);
        LogOperacion log = new LogOperacion("LOG-PRE" + idPrestamo, fechaFormato.format(new Date()), "PRESTAMO", sedeOperacion, gson.toJson(nuevoPrestamo));
        datosBiblioteca.getLog_operaciones().add(log);
        guardarCambios();
        System.out.println("Préstamo registrado: " + idPrestamo);
        return log;
    }

    public synchronized LogOperacion registrarDevolucion(String isbn, String sedeOperacion) {
        Prestamo prestamo = buscarPrestamo(isbn);

        if (prestamo == null) {
            System.out.println("Fallo Devolucion: Prestamo no existe.");
            return null;
        }
        prestamo.setEstado("DEVUELTO");

        Libro libro = buscarLibro(isbn);

        if (libro == null) {
            System.out.println("Fallo Devolucion: Libro no existe.");
            return null;
        }

        if (sedeOperacion.equals("sede1")) {
            libro.setDisponibles_sede_1(libro.getDisponibles_sede_1() + 1);
        } else {
            libro.setDisponibles_sede_2(libro.getDisponibles_sede_2() + 1);
        }
        LogOperacion log= new LogOperacion("LOG-DEV-" + prestamo.getId_prestamo(), fechaFormato.format(new Date()),"DEVOLUCION", sedeOperacion, gson.toJson(prestamo));
        guardarCambios();
        System.out.println("Devolución registrada: " + isbn);
        return log;
    }

    public synchronized LogOperacion registrarRenovacion(String isbn, String sedeOperacion) {

        Prestamo prestamo = buscarPrestamo(isbn);

        if (prestamo == null) {
            System.out.println("Fallo Renovación: Prestamo no existe.");
            return null;
        }

        LogOperacion log = registrarDevolucion(isbn, sedeOperacion);
        return log;
    }

    public synchronized boolean aplicarLogRemoto(LogOperacion logRemoto) {
        if (logRemoto.getNodo_emisor().equals(idSede)) {
            return true;
        }

        System.out.println("Aplicando Log Remoto: " + logRemoto.getId_operacion());

        try {
            Prestamo prestamoAplicar = gson.fromJson(logRemoto.getPayload(), Prestamo.class);
            Libro libro = buscarLibro(prestamoAplicar.getIsbn());

            if (libro == null) {
                return false;
            }

            if (logRemoto.getTipo().equals("PRESTAMO")) {
                if (logRemoto.getNodo_emisor().equals("sede1")) {
                    int actual = libro.getDisponibles_sede_2();
                    libro.setDisponibles_sede_2(actual - 1);
                } else {
                    int actual = libro.getDisponibles_sede_1();
                    libro.setDisponibles_sede_1(actual - 1);
                }

                datosBiblioteca.getPrestamos().add(prestamoAplicar);
                datosBiblioteca.getLog_operaciones().add(logRemoto);
            }

            guardarCambios();
            System.out.println("Log Aplicado correctamente.");
            return true;

        } catch (Exception e) {
            System.err.println("Error" + e.getMessage());
            return false;
        }
    }
    public LogOperacion obtenerUltimoLog() {
        List<LogOperacion> logs = datosBiblioteca.getLog_operaciones();
        if (logs != null && !logs.isEmpty()) {
            return logs.get(logs.size() - 1);
        }
        return null;
    }
}

