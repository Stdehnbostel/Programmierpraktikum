import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTextArea;

public class ServerThread extends Thread implements Serializable {

    public transient Socket client;
    public transient ArrayList<ServerThread> threadList;
    private transient ArrayList<Room> roomList;
    private transient ArrayList<Room> privateRoomList;
    public String userName;
    public String room;
    private String pwd;
    private boolean online;
    private boolean banStatus;
    private transient JTextArea chat;
    private transient ObjectInputStream input;
    private transient ObjectOutputStream out;
    private ServerMessages msg;

    public ServerThread(
        Socket socket, 
        ArrayList<ServerThread> threads, 
        ArrayList<Room> roomList,
        String name, 
        String pwd, 
        JTextArea chat, 
        ObjectInputStream input,
        ObjectOutputStream out) {
        this.client = socket;
        this.threadList = threads;
        this.roomList = roomList;
        this.userName = name;
        this.pwd = pwd;
        this.online = true;
        this.chat = chat;
        this.input = input;
        this.out = out;
        this.room = "";
        this.banStatus = false;
    }

    @Override
    public void run() {
        msg = new ServerMessages(threadList);
        privateRoomList = new ArrayList<Room>();
        try {
            //Reading the input from Client

            //inifite loop for server
            while(true) {

                Object in = input.readObject();
                
                if (!(in instanceof Message) && in instanceof String) {
                    online = processString((String)in);
                    if (!online) {
                        break;
                    }
                }

                if (in instanceof Message) {
                    Message incoming = (Message)in;
                    if (incoming.type.equals("Room") && !((String)incoming.msg).equals("")) {
                        this.room = (String)incoming.msg;
                        msg.addUserToRoom(userName, room, roomList);
                        msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(roomList)));
                        System.out.println("Add user to room");
                        msg.sendToAllClients(new Message("Users", msg.generateUserListWithRoom(threadList)));
                    } else if (incoming.type.equals("Room") && ((String)incoming.msg).equals("")) {
                        msg.removeUserFromRoom(userName, room, roomList);
                        msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(roomList)));
                        this.room = "";
                    } else if (incoming.type.equals("Private")) {
                        if (incoming.msg instanceof String[]) {
                            String[] privateMsg = (String[])incoming.msg;
                            boolean roomFound = msg.sendToRoom(privateMsg[0], privateRoomList, incoming);
                            if (!roomFound) {
                                Room privateRoom = new Room(privateMsg[0]);
                                privateRoomList.add(privateRoom);
                                String users[] = privateMsg[0].split("\n");
                                msg.addUserToRoom(users[0], privateRoom);
                                msg.addUserToRoom(users[1], privateRoom);
                                msg.sendToRoom(privateMsg[0], privateRoomList, incoming);
                            }
                        }

                    } else if (!this.room.equals("")) {
                        msg.sendToRoom(room, roomList, in);
                    } else {
                        msg.sendToAllClients(in);
                    }
                    if (incoming.type.equals("Room")) {
                        msg.sendToAllClients(new Message("Users", msg.generateUserListWithRoom(threadList)));
                    }
                }
            }


        } catch (Exception e) {
            System.out.println("Error occured " + e + e.getStackTrace());
            online = logout();
        }
    }

    private boolean processString(String inputString) {
        if(inputString.equals("exit")) {
            return logout();
        } else if (room.equals("")) {
            msg.sendToAllClients("[" + this.userName + "]: " + inputString);
            chat.append("[" + this.userName + "]: " + inputString + "\n");
        } else {
            msg.sendToRoom(room, roomList, "[Raum: " + room + "] [" + this.userName + "]: " + inputString);
            chat.append("[Raum: " + room + "] [" + this.userName + "]: " + inputString + "\n");
        }
        return true;
    }

    public Socket getSocket() {
        return this.client;
    }

    public String getPwd() {
        return this.pwd;
    }

    public boolean getOnlineStatus() {
        return online;
    }

    public ObjectInputStream getObjectInputStream() {
        return this.input;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return this.out;
    }

    public void setOnlineStatus(boolean online) {
        this.online = online;
    }

    public void setThreadList(ArrayList<ServerThread> tl) {
        this.threadList = tl;
    }

    public void setRoomList(ArrayList<Room> roomList) {
        this.roomList = roomList;
    }

    public void setRoom(String roomName) {
        this.room = roomName;
    }

    public void setChat(JTextArea chat) {
        this.chat = chat;
    }

    public String getRoomName() {
        return this.room;
    }

    public boolean getBanStatus() {
        return this.banStatus;
    }

    public void setBanStatus(boolean banStatus) {
        this.banStatus = banStatus;
    }

    public String getUserName() {
        return this.userName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ServerThread) {
            if (((ServerThread)o).userName.compareTo(this.userName) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean logout() {
        if (!room.equals("")) {
            msg.removeUserFromRoom(userName, room, roomList);
        }
        String userList = msg.generateUserListWithRoom(threadList);
        Message users = new Message("String", userList);
        msg.sendToAllClients(users);
        msg.sendToAllClients("* " + this.userName + " hat sich abgemeldet! *");
        chat.append("* " + this.userName + " hat sich abgemeldet! *" + "\n");
        msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(roomList)));
        this.out = null;
        return false;
    }
}
