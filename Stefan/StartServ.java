import java.io.*;

public class StartServ extends Thread {

    public static void main(String args[]) {

        // boolean run = true;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Server starten it 'start', 'quit' zum Verlassen: ");
        String input = "";

        String start = "start";
        String quit = "quit";

        while (!input.equals(start) && !input.equals(quit)) {
            try {
                input = br.readLine();
            } catch (IOException e) {
                System.out.println(e);
            }
            input.replaceAll("\\s+", "");
        }

        ComServ server = new ComServ();
        if (input.equals(start)) {
            System.out.println("start server...");
            server.start();
        }
    }
}