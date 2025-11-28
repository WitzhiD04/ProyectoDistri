package proyecto.gestor;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import java.util.StringTokenizer;

public class GestorCarga {
    private static String HOST_OPERACION;
    private static int PUERTO_OPERACION;
    private static String ID_SEDE;

    public GestorCarga(String host, int puerto) {
        HOST_OPERACION = host;
        PUERTO_OPERACION = puerto;
        ID_SEDE = (puerto == 5000) ? "sede1" : "sede2";
    }

    public void gestor() {
        String direccion = "tcp://" + HOST_OPERACION + ":" + PUERTO_OPERACION;

        try (ZContext context = new ZContext()) {
            // Socket principal para operaciones
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind(direccion);
            System.out.println("Gestor de Carga (" + ID_SEDE + ") escuchando en " + direccion);

            // Sockets para actores
            ZMQ.Socket actorRenov = context.createSocket(SocketType.PUB);
            ZMQ.Socket actorDev = context.createSocket(SocketType.PUB);
            ZMQ.Socket actorPres = context.createSocket(SocketType.REQ);

            if (PUERTO_OPERACION == 5000) {
                actorRenov.bind("tcp://*:5002");
                actorDev.bind("tcp://*:5003");
                actorPres.connect("tcp://localhost:5006");
            } else if (PUERTO_OPERACION == 5001) {
                actorRenov.bind("tcp://*:5004");
                actorDev.bind("tcp://*:5005");
                actorPres.connect("tcp://localhost:5007");
            }

            // Dar tiempo a los sockets PUB para establecerse
            Thread.sleep(100);

            System.out.println("Gestor de Carga listo para recibir peticiones");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje = socket.recv(0);
                String mensajeString = new String(mensaje, ZMQ.CHARSET).trim();

                // Healthcheck
                if (mensajeString.equals("PING")) {
                    socket.send("PONG " + ID_SEDE);
                    System.out.println("Respondido PING con PONG " + ID_SEDE);
                    continue;
                }

                System.out.println("Mensaje recibido: " + mensajeString);

                try {
                    StringTokenizer tokenizer = new StringTokenizer(mensajeString, " ");
                    String tipo = tokenizer.nextToken();
                    String isbn = tokenizer.nextToken();

                    String mensajeConSede = mensajeString + " " + ID_SEDE;

                    switch (tipo) {
                        case "DEVOLVER":
                            // PUB: enviar y responder inmediatamente (fire and forget)
                            actorDev.send(mensajeConSede);
                            socket.send("Procesando devolución del libro con ISBN: " + isbn);
                            System.out.println("Devolución publicada para procesamiento");
                            break;

                        case "RENOVAR":
                            // PUB: enviar y responder inmediatamente (fire and forget)
                            actorRenov.send(mensajeConSede);
                            socket.send("Procesando renovación del libro con ISBN: " + isbn);
                            System.out.println("Renovación publicada para procesamiento");
                            break;

                        case "PRESTAMO":
                            // REQ-REP: esperar respuesta del actor
                            actorPres.send(mensajeConSede);
                            byte[] mensajePresByte = actorPres.recv();
                            String mensajePres = new String(mensajePresByte, ZMQ.CHARSET).trim();
                            socket.send("PRESTAMO: " + mensajePres);
                            break;

                        default:
                            socket.send("ERROR: Tipo de operación no válido: " + tipo);
                    }
                } catch (Exception e) {
                    socket.send("ERROR: Formato de mensaje inválido");
                    System.err.println("Error procesando mensaje: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fatal en GestorCarga: " + e.getMessage());
            e.printStackTrace();
        }
    }
}