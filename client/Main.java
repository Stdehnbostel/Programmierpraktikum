import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
    
        String serverName = "";
        System.out.println("Verbinde zu Server: ");
        try {
            BufferedReader input = new BufferedReader( new InputStreamReader(System.in) );
            serverName = input.readLine();

        } catch (IOException e) {
            System.out.println("IOException occured in ClientMain");
        }
        
        try (Socket socket = new Socket(serverName, 1234)){
            //reading the input from server

            
            // use DataOutputStream instead of PrintWriter
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //taking the user input
            Scanner scanner = new Scanner(System.in);
            String userInput;
            // String response;

            ClientRunnable clientRun = new ClientRunnable(socket, scanner);

            new Thread(clientRun).start();
           //loop closes when user enters exit command
           
           do {
               
                userInput = scanner.nextLine();                    
                out.writeUTF(userInput); 
                    // Nachrichtenlayout [name]: wird jetzt auf der Serverseite implementiert
                if (userInput.equals("exit")) {
                    clientRun.clientRun = false;
                    out.flush();
                    //reading the input from server                        
                    // break;
                }

           } while (clientRun.clientRun);

           scanner.close();
            
        } catch (Exception e) {
            System.out.println("Exception occured in client main: " + e.getStackTrace());
        } 
    }

}