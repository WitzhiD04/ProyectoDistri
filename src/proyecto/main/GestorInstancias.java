package proyecto.main;

import proyecto.gestor.GestorAlmacenamiento;


public class GestorInstancias {

    // Almacena la única instancia de GestorAlmacenamiento por proceso.
    private static GestorAlmacenamiento instanciaGA;

    public static GestorAlmacenamiento obtenerGA(String idSede) {
        if (instanciaGA == null) {
            instanciaGA = new GestorAlmacenamiento(idSede);
        }
        return instanciaGA;
    }
}