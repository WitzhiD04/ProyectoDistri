package proyecto.main;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class DetectorFallas {
    private static final String DIR_MASTER_GA1 = "tcp://localhost:5000";
    private static final String DIR_SLAVE_GA2 = "tcp://localhost:5001";
    private static final int PUERTO_PUBLICACION = 8000;
    private static final int TIEMPO_ESPERA_MS = 2000;
    private static final int TIEMPO_CHEQUEO_MS = 5000;

    public static void main(String[] args) throws InterruptedException {
        try (ZContext contexto = new ZContext()) {
            ZMQ.Socket publicador = contexto.createSocket(SocketType.PUB);
            publicador.bind("tcp://*:" + PUERTO_PUBLICACION);
            System.out.println("Detector de Fallas: Publicando estados en puerto " + PUERTO_PUBLICACION);

            // Dar tiempo al publicador para establecerse
            Thread.sleep(500);

            while (!Thread.currentThread().isInterrupted()) {
                // Crear socket fresco para cada chequeo
                ZMQ.Socket chequeadorGA1 = contexto.createSocket(SocketType.REQ);
                chequeadorGA1.setReceiveTimeOut(TIEMPO_ESPERA_MS);
                chequeadorGA1.connect(DIR_MASTER_GA1);

                boolean GA1_estaActivo = false;
                try {
                    chequeadorGA1.send("PING");
                    String respuestaGA1 = chequeadorGA1.recvStr();
                    GA1_estaActivo = (respuestaGA1 != null && respuestaGA1.contains("PONG"));

                    if (GA1_estaActivo) {
                        System.out.println("✓ MASTER GA1 ACTIVO - Operación normal");
                        publicador.send("MASTER_ACTIVO:GA1:5000");
                    }
                } catch (Exception e) {
                    System.err.println("✗ Error al contactar GA1: " + e.getMessage());
                } finally {
                    chequeadorGA1.close();
                }

                if (!GA1_estaActivo) {
                    System.err.println("⚠ FALLA DETECTADA: GA1 (Master) NO RESPONDE");

                    // Verificar GA2
                    ZMQ.Socket chequeadorGA2 = contexto.createSocket(SocketType.REQ);
                    chequeadorGA2.setReceiveTimeOut(TIEMPO_ESPERA_MS);
                    chequeadorGA2.connect(DIR_SLAVE_GA2);

                    boolean GA2_estaActivo = false;
                    try {
                        chequeadorGA2.send("PING");
                        String respuestaGA2 = chequeadorGA2.recvStr();
                        GA2_estaActivo = (respuestaGA2 != null && respuestaGA2.contains("PONG"));

                        if (GA2_estaActivo) {
                            System.out.println("⚡ FAILOVER: Promoviendo GA2 a Master");
                            publicador.send("MASTER_ACTIVO:GA2:5001");
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Error al contactar GA2: " + e.getMessage());
                    } finally {
                        chequeadorGA2.close();
                    }

                    if (!GA2_estaActivo) {
                        System.err.println("💀 CRÍTICO: Ambas sedes están caídas");
                        publicador.send("SISTEMA_CAIDO");
                    }
                }

                Thread.sleep(TIEMPO_CHEQUEO_MS);
                System.out.println("─────────────────────────────────────────");
            }
        }
    }
}
