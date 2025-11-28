
package proyecto.actor;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import proyecto.entidades.LogOperacion;
import proyecto.gestor.GestorAlmacenamiento;

import java.util.StringTokenizer;

public class ActorRenov {

    private int puerto;
    private String host;
    private GestorAlmacenamiento ga;
    private final Gson gson = new Gson();
    private final String hostRemoto;
    private final int puertoReplicacionRemoto;

    public ActorRenov(String host, int puerto, String hostRemoto, int puertoReplicacionRemoto, String sede) {
        this.host = host;
        this.puerto = puerto;
        this.hostRemoto = hostRemoto;
        this.puertoReplicacionRemoto = puertoReplicacionRemoto;
        this.ga = new GestorAlmacenamiento(sede);
    }

    public void renovacion () {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect("tcp://" + host + ":" + puerto);
            socket.subscribe("");
            System.out.println("Actor Renovación en linea");

            ZMQ.Socket socketReplicacion = context.createSocket(SocketType.PUSH);
            socketReplicacion.connect("tcp://" + hostRemoto + ":" + puertoReplicacionRemoto);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje = socket.recv();
                String mensajeString = new String(mensaje, ZMQ.CHARSET).trim();
                System.out.println("Mensaje recibido: " + mensajeString);

                StringTokenizer tokenizer = new StringTokenizer(mensajeString, " ");
                tokenizer.nextToken();
                String isbn = tokenizer.nextToken();

                LogOperacion log = ga.registrarRenovacion(isbn, ga.getIdSede());

                if (log == null) {
                    System.err.println("No se pudo realizar la renovación exitosamente");
                } else {
                    System.out.println("Renovación exitosa con id: " + log.getId_operacion());
                    String jsonLog = gson.toJson(log);
                    socketReplicacion.send(jsonLog.getBytes(ZMQ.CHARSET), 0);
                    System.out.println("Log de replicación enviado a Sede Remota.");
                }

                // SUB no puede enviar respuesta, solo procesa
            }
        }
    }
}