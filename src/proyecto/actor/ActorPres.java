package proyecto.actor;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ActorPres {

    private int puerto;
    private String host;

    public ActorPres(String host, int puerto) {
        this.puerto  = puerto;
        this.host = host;
    }

    public void prestamo () {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.connect("tcp://" + host + ":" + puerto);
            socket.subscribe("");
            System.out.println("Actor Prestamo en linea");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje = socket.recv();
                String mensajeString = new String(mensaje, ZMQ.CHARSET).trim();
                System.out.println("Mensaje recibido: " + mensajeString);
                System.out.println("Verificando disponibilidad...");
                Thread.sleep(1500);
                System.out.println("Proceso realizado");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
