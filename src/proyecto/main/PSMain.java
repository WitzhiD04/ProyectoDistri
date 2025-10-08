package proyecto.main;

import proyecto.solicitante.PS;

public class PSMain {

    public static void main(String[] args) {
        String nomArchivo = args[0];
        PS solicitante = new PS(nomArchivo);

        solicitante.servicio();
    }
}
