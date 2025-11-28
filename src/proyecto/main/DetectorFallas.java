package proyecto.main;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class DetectorFallas {
    private static final String DIR_MASTER_GA1 = "tcp://localhost:6009"; // Puerto Master
    private static final String DIR_SLAVE_MONITOR2 = "tcp://localhost:6010"; // Puerto Réplica
    private static final int PUERTO_PUBLICACION = 8000; // Puerto donde se anuncian los estados
    private static final int TIEMPO_ESPERA_MS = 1000; // Timeout para recibir PONG
    private static final int TIEMPO_CHEQUEO_MS = 3000; // Intervalo de chequeo (3s)

    public static void main(String[] args) throws InterruptedException {
        try (ZContext contexto = new ZContext()) {

            ZMQ.Socket publicador = contexto.createSocket(SocketType.PUB);
            publicador.bind("tcp://*:" + PUERTO_PUBLICACION);
            System.out.println("Detector de Fallas: Publicando estados en puerto " + PUERTO_PUBLICACION);

            // Esperar a que los suscriptores se conecten
            Thread.sleep(500);

            ZMQ.Socket chequeadorGA1 = contexto.createSocket(SocketType.REQ);
            chequeadorGA1.setLinger(0);
            chequeadorGA1.setReceiveTimeOut(TIEMPO_ESPERA_MS);
            chequeadorGA1.connect(DIR_MASTER_GA1);
            System.out.println("Conectado al Master GA1 en " + DIR_MASTER_GA1);

            ZMQ.Socket chequeadorGA2 = contexto.createSocket(SocketType.REQ);
            chequeadorGA2.setLinger(0);
            chequeadorGA2.setReceiveTimeOut(TIEMPO_ESPERA_MS);
            chequeadorGA2.connect(DIR_SLAVE_MONITOR2);
            System.out.println("Conectado al Monitor Slave GA2 en " + DIR_SLAVE_MONITOR2);

            while (!Thread.currentThread().isInterrupted()) {
                boolean GA1_estaActivo = false;
                boolean GA2_estaActivo = false;

                // Chequeo GA1
                try {
                    chequeadorGA1.send("PING");
                    String respuestaGA1 = chequeadorGA1.recvStr();
                    GA1_estaActivo = "PONG sede1".equals(respuestaGA1);
                } catch (ZMQException e) {
                    System.err.println("Error al chequear GA1: " + e.getMessage());
                }

                if (GA1_estaActivo) {
                    System.out.println("MASTER GA1 ACTIVO. Operación normal.");
                    try { publicador.send("MASTER_ACTIVO:GA1:5000"); } catch (ZMQException ignored) {}
                } else {
                    System.err.println("FALLA DETECTADA: GA1 (Master) NO RESPONDE.");

                    // Chequeo GA2
                    try {
                        chequeadorGA2.send("PING");
                        String respuestaGA2 = chequeadorGA2.recvStr();
                        GA2_estaActivo = "PONG sede2".equals(respuestaGA2);
                    } catch (ZMQException e) {
                        System.err.println("Error al chequear GA2: " + e.getMessage());
                    }

                    if (GA2_estaActivo) {
                        System.out.println("FAILOVER: GA2 está UP. PROMOVIENDO a nuevo Master.");
                        try { publicador.send("MASTER_ACTIVO:GA2:5001"); } catch (ZMQException ignored) {}
                    } else {
                        System.err.println("FALLO CRÍTICO: Ambas sedes están caídas.");
                        try { publicador.send("SISTEMA_CAIDO"); } catch (ZMQException ignored) {}
                    }
                }

                Thread.sleep(TIEMPO_CHEQUEO_MS);
                System.out.println("------------------------------------------");
            }
        }
    }
}

