import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {

    public Socket client;
    private ArrayList<ServerThread> threadList;
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
            DataInputStream input = new DataInputStream(client.getInputStream());

            //inifite loop for server
            while(true) {
                String outputString = input.readUTF();
                //if user types exit command
                if(outputString.equals("exit")) {
                    break;
                }
                printToALlClients("[" + this.userName + "]: " + outputString);
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
                System.out.println(outputString);
                out.writeUTF(outputString);
            } catch (IOException e) {
                System.out.println("IOException occured in: ServerThread");
            }

        }

    }

    public Socket getSocket() {
        return this.client;
    }

    public String getPwd() {
        return this.pwd;
    }
}
