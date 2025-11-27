package proyecto.actor;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class HealthChecker implements Runnable {

    private String idSede;
    private int puerto;

    public HealthChecker(String idSede, int puerto) {
        this.idSede = idSede;
        this.puerto = puerto;
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:" + puerto);
            System.out.println("Healthchecker (" + idSede + ") listo en puerto: " + puerto);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensajeBytes = socket.recv(0);
                String mensaje = new String(mensajeBytes, ZMQ.CHARSET);

                if (mensaje.equals("PING")) {
                    String respuesta = "OK " + idSede;
                    socket.send(respuesta.getBytes(ZMQ.CHARSET), 0);
                }
            }
        }
    }


}
