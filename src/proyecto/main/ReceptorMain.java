package proyecto.main;

import proyecto.actor.ReceptorReplicacion;
import proyecto.gestor.GestorAlmacenamiento;

public class ReceptorMain {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Uso: java ReceptorMain <sede1|sede2> <puerto_PULL>");
            System.err.println("Ejemplo Sede 1: java ReceptorMain sede1 6006");
            System.err.println("Ejemplo Sede 2: java ReceptorMain sede2 6007");
            System.exit(1);
        }

        String idSede = args[0];
        int puertoPull = Integer.parseInt(args[1]);
        int puertoRep = puertoPull + 1000; // 7006 o 7007

        // Obtener instancia única del Gestor de Almacenamiento
        GestorAlmacenamiento ga = GestorInstancias.obtenerGA(idSede);

        System.out.println("===========================================");
        System.out.println("Iniciando Receptor de Replicación");
        System.out.println("Sede: " + idSede);
        System.out.println("Puerto PULL (recepción logs): " + puertoPull);
        System.out.println("Puerto REP (servidor logs): " + puertoRep);
        System.out.println("===========================================");

        // Crear y ejecutar receptor
        ReceptorReplicacion receptor = new ReceptorReplicacion(puertoPull, puertoRep, ga);
        receptor.run();

        // Mantener el hilo principal vivo
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("ReceptorMain interrumpido");
        }
    }
}