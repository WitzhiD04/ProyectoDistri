package proyecto.main;

import proyecto.actor.ReceptorReplicacion;
import proyecto.gestor.GestorAlmacenamiento;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import static java.lang.Thread.currentThread;

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
        int puertoRep = Integer.parseInt(args[2]);


        GestorAlmacenamiento ga = GestorInstancias.obtenerGA(idSede);


        ReceptorReplicacion receptor = new ReceptorReplicacion(puertoPull, puertoRep, ga);
        receptor.run();

        try (ZContext contexto = new ZContext()) {
            ZMQ.Socket checkerResponder = contexto.createSocket(SocketType.REP);
            while (!Thread.currentThread().isInterrupted()) {
                checkerResponder.bind("tcp://*:" + puertoRep);
                String mensaje = checkerResponder.recvStr();

                if (mensaje.equals("PING")) {
                    System.out.println("Receptor de Replicación de " + idSede + " recibió PING");
                    checkerResponder.send("PONG");
                }

            }


        }

    }
}
