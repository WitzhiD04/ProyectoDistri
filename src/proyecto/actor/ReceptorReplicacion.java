package proyecto.actor;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import com.google.gson.Gson;
import proyecto.entidades.LogOperacion;
import proyecto.gestor.GestorAlmacenamiento;


public class ReceptorReplicacion implements Runnable {

    private final int puerto;
    private final GestorAlmacenamiento ga;
    private final Gson gson = new Gson();

    public ReceptorReplicacion(int puertoEscuchaLogs, GestorAlmacenamiento gaCompartido) {
        this.puerto = puertoEscuchaLogs;
        // Se comparte la MISMA instancia del Gestor de Almacenamiento
        this.ga = gaCompartido;
    }

    @Override
    public void run() {
        ejecutarProceso();
    }
    public void ejecutarProceso() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socketLogs = context.createSocket(SocketType.PULL);

            socketLogs.bind("tcp://*:" + puerto);

            System.out.println("Receptor Replicación (" + ga.getIdSede() + ") listo en puerto: " + puerto);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] msg = socketLogs.recv(0);
                String jsonLog = new String(msg, ZMQ.CHARSET);

                LogOperacion log = gson.fromJson(jsonLog, LogOperacion.class);

                boolean aplicado = ga.aplicarLogRemoto(log);

                if (aplicado) {
                    System.out.println("Log Remoto " + log.getId_operacion() + " aplicado correctamente.");
                } else {
                    System.err.println("Error: Fallo al aplicar Log Remoto " + log.getId_operacion());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fatal en ReceptorReplicacion: " + e.getMessage());
        }
    }
}