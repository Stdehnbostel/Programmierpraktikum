import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234)){
            //reading the input from server
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            
            //returning the output to the server : true statement is to flush the buffer otherwise
            //we have to do it manuallyy
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //taking the user input
            Scanner scanner = new Scanner(System.in);
            String userInput;
            String response;
            String clientName = "empty";

            ClientRunnable clientRun = new ClientRunnable(socket);


            new Thread(clientRun).start();
           //loop closes when user enters exit command
           
           do {
               
               if (clientName.equals("empty")) {
                    System.out.println("Enter your name ");
                    userInput = scanner.nextLine();
                    clientName = userInput;
                    out.writeUTF(userInput);
                    //output.println(userInput);
                    if (userInput.equals("exit")) {
                        break;
                    }
               } 
               else {
                    String message = ( "[" + clientName + "]" + ": " );
                    //System.out.println(message);
                    userInput = scanner.nextLine();
                    out.writeUTF(userInput);
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