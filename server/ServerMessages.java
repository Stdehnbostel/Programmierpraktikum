import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ServerMessages extends Thread {

    private String msg;
    private ArrayList<ServerThread> clients;

    ServerMessages(ArrayList<ServerThread> clients) {
        this.clients = clients;
    }

    public void run() {

        while (true) {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                this.msg = input.readLine();    
                sendServerMessage(clients, msg);            
            } catch (IOException e) {
                System.out.println("Error read line");
            }
        }
    }

    private void sendServerMessage(ArrayList<ServerThread> clients, String msg) {
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