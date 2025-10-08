package proyecto.main;

import java.util.Scanner;
import proyecto.actor.ActorDev;

public class ActorDevMain {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.println("Escoga de que sede es este actor (1 o 2)");
        int sede = s.nextInt();
        int puerto = 0;

        if (sede == 1) {
            puerto = 5003;
        } else if (sede == 2) {
            puerto = 5005;
        } else {
            System.out.println("Error: introduzca una sede");
        }

        ActorDev actorDev = new ActorDev(puerto);
        actorDev.devolucion();
    }
}
