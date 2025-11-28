package proyecto.actor;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import com.google.gson.Gson;
import proyecto.entidades.LogOperacion;
import proyecto.gestor.GestorAlmacenamiento;

import java.util.List;


public class ReceptorReplicacion implements Runnable {

    private final int puerto;
    private final GestorAlmacenamiento ga;
    private final Gson gson = new Gson();
    private final int puertoServidorLogs;

    public ReceptorReplicacion(int puertoEscuchaLogs, int puertoServidorLogs, GestorAlmacenamiento gaCompartido) {
        this.puerto = puertoEscuchaLogs;
        // Se comparte la MISMA instancia del Gestor de Almacenamiento
        this.ga = gaCompartido;
        this.puertoServidorLogs = puertoServidorLogs;
    }

    @Override
    public void run() {
        new Thread(this::ejecutarProceso).start();
        new Thread(this::ejecutarServidorLogs).start();
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

    public void ejecutarServidorLogs() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socketServidor = context.createSocket(SocketType.REP);
            socketServidor.bind("tcp://*:" + puertoServidorLogs);

            System.out.println("Servidor de Logs (" + ga.getIdSede() + ") listo en puerto: " + puertoServidorLogs);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] msg = socketServidor.recv(0);
                String idUltimoLog = new String(msg, ZMQ.CHARSET).trim();

                if (idUltimoLog.startsWith("DAME_LOGS_DESDE")) {
                    String idLog = idUltimoLog.split(" ")[1];

                    List<LogOperacion> logsFaltantes = ga.obtenerLogsDesde(idLog);

                    String jsonLogs = gson.toJson(logsFaltantes);

                    socketServidor.send(jsonLogs.getBytes(ZMQ.CHARSET), 0);
                    System.out.println("Servidor de Logs: Logs faltantes enviados, cantidad: " + logsFaltantes.size());
                } else {
                    socketServidor.send("ERROR: Comando desconocido".getBytes(ZMQ.CHARSET), 0);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en Servidor Logs (REP): " + e.getMessage());
        }
    }

    public void iniciarRecuperacionDeLogs(String hostRemoto, int puertoServidorLogsRemoto) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socketRecuperacion = context.createSocket(SocketType.REQ);
            socketRecuperacion.connect("tcp://" + hostRemoto + ":" + puertoServidorLogsRemoto);
            System.out.println("Intentando conexión de recuperación con el GA remoto...");

            // 1. Obtener mi último Log conocido
            LogOperacion ultimoLog = ga.obtenerUltimoLog();
            String idLogLocal = ultimoLog != null ? ultimoLog.getId_operacion() : "N/A";

            // 2. Enviar solicitud
            String mensajePeticion = "DAME_LOGS_DESDE " + idLogLocal;
            socketRecuperacion.send(mensajePeticion.getBytes(ZMQ.CHARSET), 0);
            System.out.println("Solicitando logs a la Sede Remota: " + mensajePeticion);

            // 3. Recibir la respuesta (bloque JSON de logs)
            String jsonLogs = socketRecuperacion.recvStr();

            // ... (Falta la lógica para parsear el JSON y aplicar los logs) ...

        } catch (Exception e) {
            System.err.println("Error en la recuperación de logs: " + e.getMessage());
        }
    }
}