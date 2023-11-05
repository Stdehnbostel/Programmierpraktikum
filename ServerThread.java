import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {

    private Socket client;
    private ArrayList<ServerThread> threadList;
    private PrintWriter output;
    public String userName;
    private String pwd;

    public ServerThread(Socket socket, ArrayList<ServerThread> threads, String name, String pwd) {
        this.client = socket;
        this.threadList = threads;
        this.userName = name;
        this.pwd = pwd;
    }

    @Override
    public void run() {
        try {
            //Reading the input from Client
            BufferedReader input = new BufferedReader( new InputStreamReader(client.getInputStream()));
            
            //returning the output to the client : true statement is to flush the buffer otherwise
            //we have to do it manually
             output = new PrintWriter(client.getOutputStream(),true);


            //inifite loop for server
            while(true) {
                String outputString = input.readLine();
                //if user types exit command
                if(outputString.equals("exit")) {
                    break;
                }
                printToALlClients("[" + userName + "]: " + outputString);
                //output.println("Server says " + outputString);
                System.out.println("Server received " + outputString);

            }


        } catch (Exception e) {
            System.out.println("Error occured " +e.getStackTrace());
        }
    }

    private void printToALlClients(String outputString) {
    
        for( ServerThread sT: threadList) {
            String name = sT.userName;
            try {
                DataOutputStream out = new DataOutputStream(sT.client.getOutputStream());
                System.out.println(outputString + "name: " + name);
                out.writeUTF(outputString + "\n");
            } catch (IOException e) {
                System.out.println("IOException occured in: ServerThread");
            }

        }

    }
}
