package proyecto.main;

import java.util.Scanner;
import proyecto.actor.ActorDev;

public class ActorDevMain {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Error: Debe proporcionar la dirección del host del actor y del host remoto como argumento.");
            System.exit(1);
        }

        String host = args[0];
        String hostRemoto = args[1];
        int puertoRemotoReplicacion;
        Scanner s = new Scanner(System.in);
        System.out.println("Escoga de que sede es este actor (1 o 2)");
        int sede = s.nextInt();
        int puerto = 0;
        String idSede;

        if (sede == 1) {
            puerto = 5003;
            idSede = "sede1";
            puertoRemotoReplicacion = 6007;
        } else if (sede == 2) {
            puerto = 5005;
            idSede = "sede2";
            puertoRemotoReplicacion = 6007;
        } else {
            idSede = "0";
            puertoRemotoReplicacion = 0;
            System.exit(0);
            System.out.println("Error: introduzca una sede");
            System.exit(0);
        }

        ActorDev actorDev = new ActorDev(host, puerto, hostRemoto, puertoRemotoReplicacion, idSede);
        actorDev.devolucion();


    }
}