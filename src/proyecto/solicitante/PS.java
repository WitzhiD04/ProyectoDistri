package proyecto.solicitante;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.io.*;

public class PS {
    private String nomArchivo;

    public PS (String nomArchivo) {
        this.nomArchivo = nomArchivo;
    }

    public void servicio () {
        System.out.println("¡Bienvenido al sistema de la universidad Ada Lovelace!");

        try (ZContext context = new ZContext()) {

            ZMQ.Socket socket = context.createSocket(SocketType.REQ);

            File file = new File("resources/" + nomArchivo);
            BufferedReader br = new BufferedReader(new FileReader(file));

            String primera = br.readLine();
            if (primera == null || !primera.startsWith("SEDE")) {
                System.out.println("Archivo inválido: debe comenzar con 'SEDE <número>'");
                return;
            }

            int sede = Integer.parseInt(primera.split(" ")[1]);
            System.out.println("Procesando solicitudes para la SEDE " + sede);

            if (sede == 1) {
                socket.connect("tcp://10.43.102.156:5000"); // GC sede 1
            } else if (sede == 2) {
                socket.connect("tcp://10.43.103.95:5001"); // GC sede 2
            } else {
                System.out.println("Sede no reconocida: " + sede);
                br.close();
                return;
            }
            String line;
            while((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                System.out.println("Enviando solicitud " + line);
                socket.send(line);
                String respuesta = socket.recvStr();
                System.out.println("Respuesta: " + respuesta);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
