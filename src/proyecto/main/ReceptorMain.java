package proyecto.main;

import proyecto.actor.ReceptorReplicacion;
import proyecto.gestor.GestorAlmacenamiento;

public class ReceptorMain {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Uso: java ReceptorMain <sede1|sede2> <puerto_PULL> <puerto_REP_Servicio>");
            System.err.println("Ejemplo Sede 1: java ReceptorMain sede1 6007 6009");
            System.err.println("Ejemplo Sede 2: java ReceptorMain sede2 6008 6010");
            System.exit(1);
        }

        String idSede = args[0];
        int puertoPull = Integer.parseInt(args[1]);
        int puertoRep = Integer.parseInt(args[2]); // <-- NUEVO PUERTO

        GestorAlmacenamiento ga = GestorInstancias.obtenerGA(idSede);

        ReceptorReplicacion receptor = new ReceptorReplicacion(puertoPull, puertoRep, ga);
        receptor.run();
    }
}
