package proyecto.actor;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import proyecto.entidades.LogOperacion;
import proyecto.gestor.GestorAlmacenamiento;

import java.util.StringTokenizer;

public class ActorPres {

    private int puerto;
    private GestorAlmacenamiento ga;
    private final Gson gson = new Gson();
    private final String hostRemoto;
    private final int puertoReplicacionRemoto;

    public ActorPres(int puerto, String sede, String hostRemoto, int puertoReplicacionRemoto) {
        this.puerto  = puerto;
        this.ga = new GestorAlmacenamiento(sede);
        this.hostRemoto = hostRemoto;
        this.puertoReplicacionRemoto = puertoReplicacionRemoto;
    }

    public void prestamo () {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:" + puerto);
            System.out.println("Actor Prestamo en linea");

            ZMQ.Socket socketReplicacion = context.createSocket(SocketType.PUSH);
            socketReplicacion.connect("tcp://" + hostRemoto + ":" + puertoReplicacionRemoto);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje = socket.recv();
                String mensajeString = new String(mensaje, ZMQ.CHARSET).trim();
                System.out.println("Mensaje recibido: " + mensajeString);

                StringTokenizer tokenizer = new StringTokenizer(mensajeString, " ");
                tokenizer.nextToken();
                String isbn = tokenizer.nextToken();

                LogOperacion log = ga.registrarPrestamo(isbn, ga.getIdSede());
                String mensajePS;

                if (log == null) {
                    mensajePS = "No se pudó realizar el préstamo exitosamente";
                } else {
                    mensajePS = "Prestamo exitoso con id: " + log.getId_operacion();
                    String jsonLog = gson.toJson(log);
                    socketReplicacion.send(jsonLog.getBytes(ZMQ.CHARSET), 0);
                    System.out.println("Log de replicación enviado a Sede Remota.");
                }
                socket.send(mensajePS);
            }
        }
    }
}