package proyecto.main;

import proyecto.actor.ActorPres;

import java.util.Scanner;

public class ActorPresMain {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Error: Debe proporcionar el puerto del host remoto como argumento.");
            System.exit(1);
        }

        String hostRemoto = args[0];
        int puertoRemotoReplicacion;

        Scanner s = new Scanner(System.in);
        System.out.println("Escoga de que sede es este actor (1 o 2)");
        int sede = s.nextInt();
        int puerto = 0;
        String idSede;

        if (sede == 1) {
            puerto = 5006;
            idSede = "sede1";
            puertoRemotoReplicacion = 6007;
        } else if (sede == 2) {
            puerto = 5007;
            idSede = "sede2";
            puertoRemotoReplicacion = 6006;
        } else {
            System.out.println("Error: introduzca una sede");
            idSede = "0";
            puertoRemotoReplicacion = 0;
            System.exit(0);
        }

        ActorPres actorPres = new ActorPres(puerto, idSede, hostRemoto, puertoRemotoReplicacion);
        actorPres.prestamo();
    }
}