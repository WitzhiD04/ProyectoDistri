package proyecto.main;

import proyecto.actor.ReceptorReplicacion;
import proyecto.gestor.GestorAlmacenamiento;

public class ReceptorMain {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Uso: java MainReceptor <sede1|sede2> <puerto_propio_PULL>");
            System.err.println("Ejemplo Sede 1: java MainReceptor sede1 6006");
            System.exit(1);
        }

        String idSede = args[0];
        int puerto = Integer.parseInt(args[1]);

        GestorAlmacenamiento ga = GestorInstancias.obtenerGA(idSede);

        ReceptorReplicacion receptor = new ReceptorReplicacion(puerto, ga);
        receptor.run();
    }
}
