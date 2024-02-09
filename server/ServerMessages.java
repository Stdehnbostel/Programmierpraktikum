import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerMessages extends Thread {

    public String msg;
    private ArrayList<ServerThread> clients;

    // Constructor initializing the message string and the list of connected clients
    ServerMessages(ArrayList<ServerThread> clients) {
        this.msg = "";
        this.clients = clients;
    }

    // Sends a message to a specific client
    public boolean sendToClient(ServerThread client, Object msg) {
        if(client.getOnlineStatus() == true) {
            try {
                ObjectOutputStream out = client.getObjectOutputStream();
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                System.out.println("IOExeption occurred in sendServerMessage()" + e);
            }
            return true;
        }
        return false;
    }

    // Sends a message to a client by their username
    public boolean sendToClient(String usernName, Object msg) {
        ServerThread client = null; 
        for (ServerThread cl: clients) {
            if (cl.userName.equals(usernName)) {
                client = cl;
            }
        }
        if (client == null) {
            return false;
        }
        return sendToClient(client, msg);
    }

    // Sends a message to all connected clients
    public void sendToAllClients(Object msg) {
        if (clients == null) {
            return;
        }
        for(ServerThread sT: clients) {
            sendToClient(sT, msg);
        }
        System.out.println("send to all Clients... done");
    }

    // Sends a message to all clients in a specific room
    public boolean sendToRoom(String roomName, ArrayList<Room> rooms, Object msg) {
        Room room = null;
        for (Room r: rooms) {
            if (r.getName().equals(roomName)) {
                room = r;
            }
        }
        if (room == null) {
            System.out.println("Error: Room not found");
            return false;
        }
        for (ServerThread sT: room.getUserList()) {
            sendToClient(sT, msg);
        }
        return true;
    }

    // Removes a user from a specific room
    public boolean removeUserFromRoom(String userName, String roomName, ArrayList<Room> rooms) {
        Room room = null;
        for (Iterator<Room> it = rooms.iterator(); it.hasNext();) {
            Room r = it.next();
            if (r.getName().equals(roomName)) {
                room = r;
            }
        }
        if (room == null) {
            System.out.println("Error: Room not found");
            return false;
        }
        room.removeUser(userName);
        return true;
    }

    // Adds a user to a specific room by roomname
    public boolean addUserToRoom(String userName, String roomName, ArrayList<Room> rooms) {
        Room room = null;
        for (Room r: rooms) {
            if (r.getName().equals(roomName)) {
                room = r;
            }
        }
        if (room == null) {
            System.out.println("Room not found");
            return false;
        }
        return addUserToRoom(userName, room);
    }

    // Adds a user to a specific room by object room
    public boolean addUserToRoom(String userName, Room room) {
        ServerThread client = null;
        for (ServerThread sT: clients) {
            if (sT.userName.equals(userName)) {
                client = sT;
            }
        }
        if (client == null) {
            System.out.println("User not found");
            return false;
        } else if (client.getOnlineStatus() == false) {
            System.out.println("User Offline");
            return false;
        }
        return room.addUser(client);
    }

    // Generates a list of online and offline users
    public String generateUserList(ArrayList<ServerThread> clients) {
        if (clients == null) {
            return "";
        }
    
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

    // Generates a list of online users with their associated rooms
    public String generateUserListWithRoom(ArrayList<ServerThread> clients) {
        if (clients == null) {
            return "";
        }
    
        StringBuilder users = new StringBuilder("Online:\n");
        int userNumber = 0;
        for (ServerThread sT : clients) {
            if (sT.getOnlineStatus()) {
                userNumber++;
                users.append(userNumber).append(". [").append(sT.userName).append("]");
                if (sT.getRoomName().equals("")) {
                    users.append("\n");
                } else {
                    users.append(" @" + sT.getRoomName() + "\n");
                }
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

    // Generates a list of room names
    public String[] generateRoomList(ArrayList<Room> rooms) {
       
        StringBuilder roomString = new StringBuilder();
        String[] roomList;
        
        for (Room room: rooms) {
            roomString.append(room.getName() + " (" + room.size() + " User)\n");
        }   

        roomList = roomString.toString().split("\n");


        return roomList;
    }

    // Sets the list of connected clients
    public void setClientList(ArrayList<ServerThread> clinets) {
        this.clients = clinets;
    }

}

