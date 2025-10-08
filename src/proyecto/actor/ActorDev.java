package proyecto.actor;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ActorDev {

    public void devolucion () {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect("tcp://localhost:5003");
            socket.subscribe("");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje = socket.recv();
                String mensajeString = new String(mensaje, ZMQ.CHARSET).trim();
                System.out.println("Mensaje recibido: " + mensajeString);
                System.out.println("Verificando disponibilidad...");
                Thread.sleep(1500);
                System.out.println("Proceso realizado");

                //String respuesta = "Devolución realizada para " + mensajeString;
                //socket.send(respuesta.getBytes());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
