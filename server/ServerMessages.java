import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ServerMessages extends Thread {

    public String msg;
    private ArrayList<ServerThread> clients;
    private ServerSocket server;

    ServerMessages(ArrayList<ServerThread> clients, ServerSocket server) {
        this.msg = "";
        this.clients = clients;
        this.server = server;
    }

    public void run() {

        while (!this.msg.equals("exit")) {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                this.msg = input.readLine();    
                sendServerMessage(clients, msg);
                if (this.msg.equals("exit")) {
                    server.close();
                }            
            } catch (IOException e) {
                System.out.println("Error read line");
            }
        }
    }

    public void sendServerMessage(ArrayList<ServerThread> clients, String msg) {
        try {
            for(ServerThread sT: clients) {
                DataOutputStream out = new DataOutputStream(sT.getSocket().getOutputStream());
                out.writeUTF(msg);
            }
        } catch (IOException e) {
            System.out.println("IOExeption occurred in Main");
        }
    }

}