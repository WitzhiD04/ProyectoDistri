package proyecto.actor;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ActorRenov {

    private int puerto;
    private String host;

    public ActorRenov(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public void renovacion () {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect("tcp://" + host + ":" + puerto);
            socket.subscribe("");
            System.out.println("Actor Renovación en linea");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje = socket.recv();
                String mensajeString = new String(mensaje, ZMQ.CHARSET).trim();
                System.out.println("Mensaje recibido: " + mensajeString);

                //String respuesta = "Renovación realizada para " + mensajeString;
                //socket.send(respuesta.getBytes());

            }
        }
    }
}
