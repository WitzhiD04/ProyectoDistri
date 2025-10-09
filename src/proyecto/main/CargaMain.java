package proyecto.main;

import proyecto.gestor.GestorCarga;

public class CargaMain {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Error: Debe proporcionar la dirección del host y puerto como argumentos.");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        GestorCarga gestor = new GestorCarga(host, port);
        gestor.gestor();
    }
}
