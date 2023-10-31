import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StartClient {

    public static void main(String args[]) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String serverName = "";
        String input;
        String userName = "";
        String pwd = "";

        System.out.println("Verbinden zu Server: ");

        try {
            input = br.readLine();
            serverName = input;
        } catch (IOException e) {
            System.out.println("Fehler bei der Eingabe");
        }

        System.out.println("Nutzername: ");

        try {
            input = br.readLine();
            userName = input;
        } catch (IOException e) {
            System.out.println("Fehler bei der Eingabe");
        }

        System.out.println("pwd: ");

        try {
            input = br.readLine();
            pwd = input;
        } catch (IOException e) {
            System.out.println("Fehler bei der Eingabe");
        }

        if (!userName.equals("") && !pwd.equals("")) {
            ComCli client = new ComCli(serverName, userName, pwd, br);
            client.run();
            System.out.println("...started Client");
        }
    }
}