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
    private String hostRemoto;
    private int puertoReplicacionRemoto;
    private ZMQ.Socket socketReplicacion;
    private ZContext context;

    public ActorRenov(String host, int puerto, String hostRemoto, int puertoReplicacionRemoto, String sede) {
        this.host = host;
        this.puerto = puerto;
        this.hostRemoto = hostRemoto;
        this.puertoReplicacionRemoto = puertoReplicacionRemoto;
        this.ga = new GestorAlmacenamiento(sede);
    }

    public void renovacion() {
        context = new ZContext();

        ZMQ.Socket socket = context.createSocket(SocketType.SUB);
        socket.connect("tcp://" + host + ":" + puerto);
        socket.subscribe("");
        System.out.println("Actor Renovación en linea");

        // Socket para escuchar cambios de master
        ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
        subscriber.connect("tcp://localhost:8000");
        subscriber.subscribe("");
        subscriber.setReceiveTimeOut(100);

        socketReplicacion = context.createSocket(SocketType.PUSH);
        socketReplicacion.connect("tcp://" + hostRemoto + ":" + puertoReplicacionRemoto);

        ZMQ.Poller poller = context.createPoller(2);
        poller.register(socket, ZMQ.Poller.POLLIN);
        poller.register(subscriber, ZMQ.Poller.POLLIN);

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll(100);

            // Verificar cambios de master
            if (poller.pollin(1)) {
                String mensaje = subscriber.recvStr();
                if (mensaje != null && mensaje.startsWith("MASTER_ACTIVO:GA2:")) {
                    System.out.println("*** FAILOVER - Cambiando replicación a GA2 ***");
                    reconectarReplicacion("localhost", 6006);
                } else if (mensaje != null && mensaje.startsWith("MASTER_ACTIVO:GA1:")) {
                    System.out.println("*** FAILBACK - Restaurando replicación a GA1 ***");
                    reconectarReplicacion(hostRemoto, puertoReplicacionRemoto);
                }
            }

            // Procesar renovaciones
            if (poller.pollin(0)) {
                byte[] mensaje = socket.recv(ZMQ.DONTWAIT);
                if (mensaje != null) {
                    procesarRenovacion(socket, new String(mensaje, ZMQ.CHARSET).trim());
                }
            }
        }
    }

    private void reconectarReplicacion(String nuevoHost, int nuevoPuerto) {
        if (socketReplicacion != null) {
            socketReplicacion.close();
        }
        socketReplicacion = context.createSocket(SocketType.PUSH);
        socketReplicacion.connect("tcp://" + nuevoHost + ":" + nuevoPuerto);
        System.out.println("Replicación reconectada a " + nuevoHost + ":" + nuevoPuerto);
    }

    private void procesarRenovacion(ZMQ.Socket socket, String mensajeString) {
        System.out.println("Mensaje recibido: " + mensajeString);

        StringTokenizer tokenizer = new StringTokenizer(mensajeString, " ");
        tokenizer.nextToken();
        String isbn = tokenizer.nextToken();

        LogOperacion log = ga.registrarRenovacion(isbn, ga.getIdSede());
        String mensajePS;

        if (log == null) {
            mensajePS = "No se pudó realizar la renovación exitosamente";
        } else {
            mensajePS = "Renovación exitosa con id: " + log.getId_operacion();
            String jsonLog = gson.toJson(log);
            socketReplicacion.send(jsonLog.getBytes(ZMQ.CHARSET), 0);
            System.out.println("Log de replicación enviado a Sede Remota.");
        }
        socket.send(mensajePS);
    }
}