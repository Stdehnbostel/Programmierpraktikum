import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StartClient {

    public static void main(String args[]) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        String name = "";
        String pwd = "";

        System.out.println("Nutzername: ");

        try {
            input = br.readLine();
            name = input;
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

        if (!name.equals("") && !pwd.equals("")) {
            ComCli client = new ComCli(name, pwd, br);
            client.run();
            System.out.println("...started Client");
        }
    }
}