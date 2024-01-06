import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
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
                sendToAllClients(msg);
                if (this.msg.equals("exit")) {
                    server.close();
                }            
            } catch (IOException e) {
                System.out.println("IOExeption occurred in sendServerMessage()");
            }
        }
    }

    public void sendToClient(ServerThread client, Object msg) {
        if(client.getOnlineStatus() == true) {
            try {
                ObjectOutputStream out = client.getObjectOutputStream();
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                System.out.println("IOExeption occurred in sendServerMessage()" + e);
            }
        }
    }

    public void sendToAllClients(Object msg) {

        for(ServerThread sT: clients) {
            sendToClient(sT, msg);
        }
    }

    public void sendToSomeClients(ArrayList<ServerThread> clients, Object msg) {
        
        for (ServerThread sT: clients) {
            sendToClient(sT, msg);
        }
    }

    public String generateUserList(ArrayList<ServerThread> clients) {
    
        StringBuilder users = new StringBuilder("Online:\n");
        int userNumber = 0;
        for (ServerThread sT : clients) {
            if (sT.getOnlineStatus()) {
                userNumber++;
                users.append(userNumber).append(". [").append(sT.userName).append("]\n");
            }
        }
        if (userNumber != clients.size()) {
            users.append("Offline:\n");
            for (ServerThread sT : clients) {
                if (!sT.getOnlineStatus()) {
                    userNumber++;
                    users.append(userNumber).append(". [").append(sT.userName).append("]\n");
                }
            }
        }
        return users.toString();    
    }    
}
