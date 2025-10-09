package proyecto.main;


import proyecto.actor.ActorRenov;
import java.util.Scanner;

public class ActorRenovMain {


    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Error: Debe proporcionar la dirección del host como argumento.");
            System.exit(1);
        }

        String host = args[0];
        Scanner s = new Scanner(System.in);
        System.out.println("Escoga de que sede es este actor (1 o 2)");
        int sede = s.nextInt();
        int puerto = 0;

        if (sede == 1) {
            puerto = 5002;
        } else if (sede == 2) {
            puerto = 5004;
        } else {
            System.out.println("Error: introduzca una sede");
            System.exit(0);
        }

        ActorRenov actorRenov = new ActorRenov(host, puerto);
        actorRenov.renovacion();
    }
}
