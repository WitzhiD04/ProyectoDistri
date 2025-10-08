package proyecto.main;

import proyecto.gestor.GestorCarga;

public class CargaMain {

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        GestorCarga gestor = new GestorCarga(host, port);
        gestor.gestor();
    }
}
