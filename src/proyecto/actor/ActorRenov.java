package proyecto.actor;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ActorRenov {

    public void renovacion () {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect("tcp://localhost:5002");
            socket.subscribe("");

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
