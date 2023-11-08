import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StartClient {

    public static void main(String args[]) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String serverName = "";
        String input;

        System.out.println("Verbinden zu Server: ");

        try {
            input = br.readLine();
            serverName = input;
        } catch (IOException e) {
            System.out.println("Fehler bei der Eingabe");
        }

        if (!serverName.equals("")) {
            ComCli client = new ComCli(serverName, br);
            client.run();
            System.out.println("...started Client");
        }
    }
}