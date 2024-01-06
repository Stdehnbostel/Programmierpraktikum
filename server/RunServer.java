import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

public class RunServer {

    private ServerSocket server;
    private ServerMessages msg;
    private JTextArea chat;
    private ArrayList<ServerThread> clients;
    private ArrayList<Room> rooms;

    RunServer(JTextArea chat) {
        this.chat = chat;
        this.rooms = new ArrayList<Room>();
    }

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
                }
            } else {
                this.clients = new ArrayList<ServerThread>();
            }
        } catch (IOException e) {

        } catch (ClassNotFoundException ce) {
            System.out.println("ClassNotFoundException: " + ce);
        }

        System.out.println(this.clients == null);
        
        try {
            this.server = new ServerSocket(1234);
            this.msg = new ServerMessages(clients, server);
            msg.start();
            while(runServer) {
                if (msg.msg.equals("exit")) {
                    runServer = false;
                    break;
                }
                Socket client = server.accept();
                LoginHandler newUser = new LoginHandler(client, clients, chat, getRoomList());
                newUser.start();
                
            }
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e.getStackTrace());
        }
    }

    public void exitServer() {
        try {
            this.msg.sendToAllClients("exit");
            this.server.close();
            PrintWriter toFile = new PrintWriter("Protokoll.txt");
            String log = chat.getText();
            toFile.write(log);
            toFile.flush();
            toFile.close();
            saveUserData();
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e.getStackTrace());
        }
    }

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

    public void addRoom(Room room) {
        this.rooms.add(room);
        sendRoomList();
    }

    public String getRoomList() {
        StringBuilder roomList = new StringBuilder();
        for (Room room: rooms) {
            roomList.append(room.getName() + "\n");
        }
        return roomList.toString();
    }

    public boolean isRoom(String name) {
        for (Room room: rooms) {
            if (room.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // return type boolean allows for notification of succes
    public boolean deleteRoom(String name) {
        for (Room room: rooms) {
            if(room.getName().equals(name)) {
                this.rooms.remove(room);
                return true;
            }
        }
 
        sendRoomList();

        return false;
    }

    private void sendRoomList() {

        if (server != null && !server.isClosed()) {
        msg.sendToAllClients(new Message("Rooms", getRoomList()));
        }
    }
}