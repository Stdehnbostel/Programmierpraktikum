import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ServerMessages extends Thread {

    public String msg;
    private ArrayList<ServerThread> clients;

    ServerMessages(ArrayList<ServerThread> clients) {
        this.msg = "";
        this.clients = clients;
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
        System.out.println(clients != null);
        System.out.println(clients.isEmpty());
        for(ServerThread sT: clients) {
            sendToClient(sT, msg);
        }
        System.out.println("send to all Clients... done");
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

    public String generateRoomList(ArrayList<Room> rooms) {
       
        StringBuilder roomList = new StringBuilder();
        
        for (Room room: rooms) {
            roomList.append(room.getName() + "\n");
        }

        return roomList.toString();
    }

    public void setClientList(ArrayList<ServerThread> clinets) {
        this.clients = clinets;
    }
}

