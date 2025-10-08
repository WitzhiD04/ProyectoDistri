package proyecto.gestor;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import java.util.StringTokenizer;

public class GestorCarga {
    private static String HOST_OPERACION;
    private static int PUERTO_OPERACION;

    public GestorCarga(String host, int puerto) {
        HOST_OPERACION = host;
        PUERTO_OPERACION = puerto;
    }

    public void gestor() {

        String direccion = "tcp://" + HOST_OPERACION + ":" + PUERTO_OPERACION;
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind(direccion); // La otra parte se conecta a el
            System.out.println("Gestor de Carga escuchando en " + direccion);

            ZMQ.Socket actorRenov = context.createSocket(SocketType.PUB);
            ZMQ.Socket actorDev = context.createSocket(SocketType.PUB);

            if (PUERTO_OPERACION == 5000) { //Sede 1, ver si dejar los puertos asi
                actorRenov.bind("tcp://*:5002");
                actorDev.bind("tcp://*:5003");
            } else if (PUERTO_OPERACION == 5001) { // sede 2
                actorRenov.bind("tcp://*:5004");
                actorDev.bind("tcp://*:5005");
            }



            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje = socket.recv();
                String mensajeString = new String(mensaje, ZMQ.CHARSET).trim();

                System.out.println("Mensaje recibido: " + mensajeString);
                StringTokenizer tokenizer = new StringTokenizer(mensajeString, " ");
                String tipo = tokenizer.nextToken();
                int isbn = Integer.parseInt(tokenizer.nextToken());

                //String respuestaActor;

                if (tipo.equals("DEVOLVER")) {
                    socket.send("Devolución del libro prestado con isbn: " + isbn);
                    actorDev.send(mensajeString);
                    //respuestaActor = actorDev.recvStr();
                    //System.out.println(respuestaActor);
                } else if (tipo.equals("RENOVAR")) {
                    socket.send("Renovación del libro prestado con isbn: " + isbn);
                    actorRenov.send(mensajeString);
                    //respuestaActor = actorRenov.recvStr();
                    //System.out.println(respuestaActor);
                } else {
                    socket.send("No se pudo encontrar un tipo válido de proceso");
                }
            }
        }

    }
}
