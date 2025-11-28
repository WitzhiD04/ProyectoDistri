package proyecto.actor;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import com.google.gson.Gson;
import proyecto.entidades.LogOperacion;
import proyecto.gestor.GestorAlmacenamiento;

public class ReceptorReplicacion implements Runnable {

    private final int puertoPull;
    private final int puertoRep;
    private final GestorAlmacenamiento ga;
    private final Gson gson = new Gson();

    public ReceptorReplicacion(int puertoPull, int puertoRep, GestorAlmacenamiento gaCompartido) {
        this.puertoPull = puertoPull;
        this.puertoRep = puertoRep;
        this.ga = gaCompartido;
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext()) {
            // Socket PULL para replicación
            ZMQ.Socket socketPull = context.createSocket(SocketType.PULL);
            socketPull.bind("tcp://*:" + puertoPull);
            System.out.println("Receptor Replicación (" + ga.getIdSede() + ") listo en puerto: " + puertoPull);

            // Socket REP para responder PING
            ZMQ.Socket socketRep = context.createSocket(SocketType.REP);
            socketRep.bind("tcp://*:" + puertoRep);
            System.out.println("Checker REP (" + ga.getIdSede() + ") listo en puerto: " + puertoRep);

            while (!Thread.currentThread().isInterrupted()) {
                // Procesar replicación (si hay logs)
                byte[] msgPull = socketPull.recv(ZMQ.DONTWAIT);
                if (msgPull != null) {
                    String jsonLog = new String(msgPull, ZMQ.CHARSET);
                    LogOperacion log = gson.fromJson(jsonLog, LogOperacion.class);

                    boolean aplicado = ga.aplicarLogRemoto(log);
                    if (aplicado) {
                        System.out.println("Log Remoto " + log.getId_operacion() + " aplicado correctamente.");
                    } else {
                        System.err.println("Error: Fallo al aplicar Log Remoto " + log.getId_operacion());
                    }
                }

                // Procesar PING
                byte[] msgRep = socketRep.recv(ZMQ.DONTWAIT);
                if (msgRep != null) {
                    String mensaje = new String(msgRep, ZMQ.CHARSET).trim();
                    if (mensaje.equals("PING")) {
                        // Responde con PONG + id de sede
                        socketRep.send(("PONG " + ga.getIdSede()).getBytes(ZMQ.CHARSET), 0);
                        System.out.println("Recibido PING, enviado PONG " + ga.getIdSede());
                    } else {
                        socketRep.send("ERROR: Comando desconocido".getBytes(ZMQ.CHARSET), 0);
                    }
                }

                Thread.sleep(10); // pequeño sleep para no bloquear 100%
            }

        } catch (Exception e) {
            System.err.println("Error fatal en ReceptorReplicacion: " + e.getMessage());
        }
    }
}
