package proyecto.main;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


public class DetectorFallas {
    private static final String DIR_MASTER_GA1 = "tcp://localhost:5000"; // Puerto de operación (GC)
    private static final String DIR_SLAVE_MONITOR2 = "tcp://localhost:7002"; // Puerto de chequeo de la réplica
    private static final int PUERTO_PUBLICACION = 8000; // Puerto donde se anuncian los estados
    private static final int TIEMPO_ESPERA_MS = 1000; // Máximo 1 segundo para recibir respuesta PONG
    private static final int TIEMPO_CHEQUEO_MS = 3000; // Intervalo de chequeo (3 segundos)

    public static void main(String[] args) throws InterruptedException {

        try (ZContext contexto = new ZContext()) {

            ZMQ.Socket publicador = contexto.createSocket(SocketType.PUB);
            publicador.bind("tcp://*:" + PUERTO_PUBLICACION);
            System.out.println("Detector de Fallas: Publicando estados en puerto " + PUERTO_PUBLICACION);

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

                chequeadorGA1.send("PING_GC");
                String respuestaGA1 = chequeadorGA1.recvStr();

                boolean GA1_estaActivo = (respuestaGA1 != null && respuestaGA1.equals("PONG_GC"));

                if (GA1_estaActivo) {

                    System.out.println("MASTER GA1 ACTIVO. Operación normal.");
                    publicador.send("MASTER_ACTIVO:GA1:5000");
                } else {
                    System.err.println("FALLA DETECTADA: GA1 (Master) NO RESPONDE.");

                    chequeadorGA2.send("PING");
                    String respuestaGA2 = chequeadorGA2.recvStr();

                    boolean GA2_estaActivo = (respuestaGA2 != null && respuestaGA2.equals("OK sede2"));

                    if (GA2_estaActivo) {
                        System.out.println("FAILOVER: GA2 está UP. PROMOVIENDO a nuevo Master.");
                        publicador.send("MASTER_ACTIVO:GA2:5001");

                    } else {
                        System.err.println("FALLO CRÍTICO: Ambas sedes están caídas.");
                        publicador.send("SISTEMA_CAIDO");
                    }
                }
                Thread.sleep(TIEMPO_CHEQUEO_MS);
                System.out.println("------------------------------------------");
            }
        }
    }
}
