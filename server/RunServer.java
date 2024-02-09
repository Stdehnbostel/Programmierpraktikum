import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import javax.swing.JTextArea;

public class RunServer extends Thread {

    private ServerSocket server;
    private ServerMessages msg;
    private JTextArea chat;
    private ArrayList<ServerThread> clients;
    private ArrayList<Room> rooms;
    private ArrayList<Room> privateRooms;

    // Constructor initializing the server with a chat area and lists for clients, rooms, and private rooms
    RunServer(JTextArea chat) {
        this.chat = chat;
        this.rooms = new ArrayList<Room>();
        this.privateRooms = new ArrayList<Room>();
        this.msg = new ServerMessages(this.clients);
    }

    // Starts the server, loading user data if available, and waits for client connections
    public void startServer() {

        boolean runServer = true;
        try {
            File f = new File("./UserData.ser");
            if (f.isFile()) {
                FileInputStream fin = new FileInputStream("./UserData.ser");
                ObjectInputStream oin = new ObjectInputStream(fin);
                ArrayList<ServerThread> userList = (ArrayList<ServerThread>) oin.readObject();
                this.clients = userList;
                oin.close();
                fin.close();
                for (ServerThread sT: this.clients) {
                    sT.setThreadList(clients);
                    sT.setChat(chat);
                    sT.setOnlineStatus(false);
                    sT.setRoomList(rooms);
                    sT.setBanStatus(sT.getBanStatus());
                }
            } else {
                this.clients = new ArrayList<ServerThread>();
            }
        } catch (IOException e) {

        } catch (ClassNotFoundException ce) {
            System.out.println("ClassNotFoundException: " + ce);
        }

        System.out.println(this.clients == null);
        this.msg.setClientList(clients);
        
        try {
            this.server = new ServerSocket(1234);
            while(runServer) {
                if (msg.msg.equals("exit")) {
                    runServer = false;
                    break;
                }
                Socket client = server.accept();
                LoginHandler newUser = new LoginHandler(client, clients, chat, rooms, privateRooms);
                newUser.start();
                
            }
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e + e.getStackTrace());
        }
    }

    // Shuts down the server, sends exit message to all clients, saves chat log, user data, and closes the server socket
    public void exitServer() {
        try {
            this.msg.sendToAllClients("exit");
            PrintWriter toFile = new PrintWriter("Protokoll.txt");
            String log = chat.getText();
            toFile.write(log);
            toFile.flush();
            System.out.println("should have saved: \n" + log);
            toFile.close();
            saveUserData();
            this.server.close();
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e + e.getStackTrace());
        }
    }

    // Saves user data (ServerThreads) to a serialized file
    public void saveUserData() {
        try {
            FileOutputStream fout = new FileOutputStream("./UserData.ser");
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeObject(clients);
            oout.close();
            fout.close();
        } catch (IOException e) {
            System.out.println("IOExeption occured in RunServer" + e);
        }
    }

    // Adds a room to the server's room list and notifies clients
    public void addRoom(Room room) {
        this.rooms.add(room);
        sendRoomList();
    }

    // Renames a room in the server's room list and notifies clients
    public void renameRoom(String oldRoomName, String newRoomName) {
        for (Room room : rooms) {
            if (room.getName().equals(oldRoomName)) {
                room.setName(newRoomName);
                sendRoomList();
                break; // Stop searching once the room is found and renamed
            }
        }
    }

    // Generates a list of room names
    public String[] getRoomList() {
        return msg.generateRoomList(rooms);
    }

    // Checks if a room with a given name exists
    public boolean isRoom(String name) {
        for (Room room: rooms) {
            if (room.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // Deletes a room from the server's room list, notifies clients, and updates user lists
    // Return type boolean allows for notification of succes
    public boolean deleteRoom(String name) {
        for (Room room: rooms) {
            if(room.getName().equals(name)) {
                msg.sendToRoom(name, rooms, new Message("Room", ""));
                this.rooms.remove(room);
                sendRoomList();
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("InterruptedExeption" + e + e.getStackTrace());
                }
                msg.sendToAllClients(new Message("String", getUserList()));
                return true;
            }
        }
        return false;
    }

    // Generates a list of online users with their associated rooms
    public String getUserList() {
        if (clients == null) {
            return "";
        }
        return msg.generateUserListWithRoom(clients);
    }

    // Retrieves the list of connected clients
    public ArrayList<ServerThread> getClients() {
        return this.clients;
    }

    // Sends a room list update to all connected clients
    private void sendRoomList() {
        if (server != null && !server.isClosed()) {
        msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(rooms)));
        }
    }

    // Retrieves a client by their username
    public ServerThread getClient(String name) {
        ServerThread client = null;
        for (ServerThread c: clients) {
            if (c.userName.equals(name)) {
                client = c;
            }
        }
        return client;
    }

    // Sends a message to a specific client
    public void sendToUser(ServerThread client, String message) {
        msg.sendToClient(client, message);
    }

    // Sends a message to a client by their username
    public void sendToUser(String userName, String message) {
        msg.sendToClient(userName, message);
    }
}