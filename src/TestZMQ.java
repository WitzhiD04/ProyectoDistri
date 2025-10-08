import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class TestZMQ {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
            socket.connect("tcp://localhost:5555");
            socket.send("Hola desde PS");
            System.out.println("Mensaje enviado correctamente ✅");
        }
    }
}

