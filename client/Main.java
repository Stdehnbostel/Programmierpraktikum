import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
    
        String serverName = "";
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
            String response;
            String clientName = "empty";
            String password = "empty";

            ClientRunnable clientRun = new ClientRunnable(socket);

            new Thread(clientRun).start();
           //loop closes when user enters exit command
           
           do {
               
               if (clientName.equals("empty")) {
                    userInput = scanner.nextLine();
                    clientName = userInput;
                    out.writeUTF(userInput);
                    if (userInput.equals("exit")) {
                        break;
                    }
               } else if (password.equals("empty")) {
                    userInput = scanner.nextLine();
                    password = userInput;
                    out.writeUTF(userInput);
                    if (userInput.equals("exit")) {
                        break;
                    }
               }
               else {
                    userInput = scanner.nextLine();                    
                    out.writeUTF(userInput); 
                    // Nachrichtenlayout [name]: wird jetzt auf der Serverseite implementiert
                    if (userInput.equals("exit")) {
                        //reading the input from server
                        break;
                    }
                }

           } while (!userInput.equals("exit"));
            
        } catch (Exception e) {
            System.out.println("Exception occured in client main: " + e.getStackTrace());
    }
    }

}