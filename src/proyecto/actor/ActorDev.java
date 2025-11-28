package proyecto.actor;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import proyecto.entidades.LogOperacion;
import proyecto.gestor.GestorAlmacenamiento;

import java.util.StringTokenizer;

public class ActorDev {

    private int puerto;
    private String host;
    private GestorAlmacenamiento ga;
    private final Gson gson = new Gson();
    private final String hostRemoto;
    private  int puertoReplicacionRemoto;

    public ActorDev(String host, int puerto, String hostRemoto, int puertoReplicacionRemoto, String sede) {
        this.puerto  = puerto;
        this.host = host;
        this.hostRemoto = hostRemoto;
        this.puertoReplicacionRemoto = puertoReplicacionRemoto;
        this.ga = new GestorAlmacenamiento(sede);
    }



    public void devolucion () {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect("tcp://" + host + ":" + puerto);
            socket.subscribe("");
            System.out.println("Actor Devolucion en linea");
            escucharDetector();
            ZMQ.Socket socketReplicacion = context.createSocket(SocketType.PUSH);
            socketReplicacion.connect("tcp://" + hostRemoto + ":" + puertoReplicacionRemoto);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje = socket.recv();
                String mensajeString = new String(mensaje, ZMQ.CHARSET).trim();
                System.out.println("Mensaje recibido: " + mensajeString);

                StringTokenizer tokenizer = new StringTokenizer(mensajeString, " ");
                tokenizer.nextToken();
                String isbn = tokenizer.nextToken();

                LogOperacion log = ga.registrarDevolucion(isbn, ga.getIdSede());

                if (log == null) {
                    System.err.println("No se pudo realizar la devolución exitosamente");
                } else {
                    System.out.println("Devolución exitosa con id: " + log.getId_operacion());
                    String jsonLog = gson.toJson(log);
                    socketReplicacion.send(jsonLog.getBytes(ZMQ.CHARSET), 0);
                    System.out.println("Log de replicación enviado a Sede Remota.");
                }

                // SUB no puede enviar respuesta, solo procesa
            }
        }
    }


    public void escucharDetector() {

        try (ZContext context = new ZContext()) {

            ZMQ.Socket subDetector = context.createSocket(SocketType.SUB);
            subDetector.connect("tcp://localhost:8000");
            subDetector.subscribe("");
            System.out.println("ActorDev escuchando al DetectorFallas en puerto 8000...");

            String mensaje = subDetector.recvStr().trim();
            System.out.println("Mensaje detector → " + mensaje);

            if (mensaje.startsWith("MASTER_ACTIVO:GA1")) {
                puertoReplicacionRemoto = 6007;
            }

            if (mensaje.startsWith("MASTER_ACTIVO:GA2")) {
                puertoReplicacionRemoto = 6006;
            }

        }
    }
}
