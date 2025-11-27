package proyecto.main;

import proyecto.actor.HealthChecker;

public class MainChecker {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java MainChecker <sede> <puerto>");
            System.exit(1);
        }

        String idSede = args[0];
        int puerto = Integer.parseInt(args[1]);

        HealthChecker monitor = new HealthChecker(idSede, puerto);

        new Thread(monitor).start();
    }
}
