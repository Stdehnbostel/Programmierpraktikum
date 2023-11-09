import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234)){
            //reading the input from server
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            
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
                   // System.out.println("Enter your name: ");
                    userInput = scanner.nextLine();
                    clientName = userInput;
                    out.writeUTF(userInput);
                    if (userInput.equals("exit")) {
                        break;
                    }
               } else if (password.equals("empty")) {
                   // System.out.println("Enter your password: ");
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