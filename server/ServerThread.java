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
        ArrayList<Room> privateRoomList,
        String name, 
        String pwd, 
        JTextArea chat, 
        ObjectInputStream input,
        ObjectOutputStream out) {
        this.client = socket;
        this.threadList = threads;
        this.roomList = roomList;
        this.privateRoomList = privateRoomList;
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
        try {
            //Reading the input from Client

            //inifite loop for server
            while(true) {

                Object in = input.readObject();
                
                if (in instanceof String) {
                    online = processString((String)in);
                    if (!online) {
                        String userList = msg.generateUserListWithRoom(threadList);
                        msg.sendToAllClients(new Message("Users", userList));
                        this.out = null;
                        break;
                    }
                } else if (in instanceof Message) {
                    decodeMessage((Message)in);
                }
            }
        } catch (Exception e) {
            System.out.println("Error occured " + e + e.getStackTrace());
            online = logout();
            String userList = msg.generateUserListWithRoom(threadList);
            msg.sendToAllClients(new Message("Users", userList));
            this.out = null;
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

    private void decodeMessage(Message in) {
        if (in.type.equals("Room") && !((String)in.msg).equals("")) {
            joinRoom((String)in.msg);
        } else if (in.type.equals("Room") && ((String)in.msg).equals("")) {
            leaveRoom();
        } else if (in.type.equals("Private")) {
            processPrivateMessage(in);
        } else if (in.type.equals("PrivatePdf")) {
            processPrivateMessage(in);
        } else if (in.type.equals("PrivatePng")) {
            processPrivateMessage(in);
        } else if (in.type.equals("PrivateJpg")) {
            processPrivateMessage(in);
        } else if (in.type.equals("PrivateBmp")) {
            processPrivateMessage(in);
        } else if (!this.room.equals("")) {
            msg.sendToRoom(room, roomList, in);
        } else {
            msg.sendToAllClients(in);
        }
        if (in.type.equals("Room")) {
            msg.sendToAllClients(new Message("Users", msg.generateUserListWithRoom(threadList)));
        }                
    }

    private boolean joinRoom(String roomName) {
        this.room = roomName;
        if (msg.addUserToRoom(userName, room, roomList)) {
            msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(roomList)));
            System.out.println("Add user to room");
            msg.sendToAllClients(new Message("Users", msg.generateUserListWithRoom(threadList)));
            return true;
        }
        return false;
    }

    private boolean leaveRoom() {
        if (msg.removeUserFromRoom(userName, room, roomList)) {
            msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(roomList)));
            this.room = "";
            return true;
        }
        return false;
    }

    private void processPrivateMessage(Message in) {

        if (!(in.msg instanceof String[])) {
        return;
        }
        String[] privateMsg = (String[])in.msg;
        String users[] = privateMsg[0].split("\n");
        if (in.type.equals("PrivatePdf")) {
            System.out.println("Priavtes Pdf empfangen");
            String user = users[0].equals(userName) ? users[1] : users[0];
            msg.sendToClient(user, new Message("private.pdf", privateMsg[1]));
        } else if (in.type.equals("PrivatePng")) {
            System.out.println("Priavtes Png empfangen");
            String user = users[0].equals(userName) ? users[1] : users[0];
            msg.sendToClient(user, new Message("private.png", privateMsg[1]));
        } else if (in.type.equals("PrivateJpg")) {
            System.out.println("Priavtes Jpg empfangen");
            String user = users[0].equals(userName) ? users[1] : users[0];
            msg.sendToClient(user, new Message("private.jpg", privateMsg[1]));
        } else if (in.type.equals("PrivateBmp")) {
            System.out.println("Priavtes Bmp empfangen");
            String user = users[0].equals(userName) ? users[1] : users[0];
            msg.sendToClient(user, new Message("private.bmp", privateMsg[1]));
        } else if (privateMsg[1].equals("~//leaveRoom")) {
            String leaveMsg[] = {privateMsg[0], userName + " hat den privaten Chat verlassen"};
            msg.sendToRoom(privateMsg[0], privateRoomList, new Message("Private", leaveMsg));
        } else {
            System.out.println("privateMsg[1]: " + privateMsg[1]);
        }
        boolean roomFound = msg.sendToRoom(privateMsg[0], privateRoomList, in);
        if (!roomFound) {
            Room privateRoom = new Room(privateMsg[0]);
            privateRoomList.add(privateRoom);
            if (!msg.addUserToRoom(users[0], privateRoom) || !msg.addUserToRoom(users[1], privateRoom)) {
                privateMsg[1] = "User ist Offline";
                msg.sendToClient(userName, new Message("Private", privateMsg));
                privateRoomList.remove(privateRoom);
                System.out.println("User not found");
            }
            msg.sendToRoom(privateMsg[0], privateRoomList, in);
        }
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
        msg.sendToAllClients("* " + this.userName + " hat sich abgemeldet! *");
        chat.append("* " + this.userName + " hat sich abgemeldet! *" + "\n");
        msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(roomList)));
        privateRoomList.removeIf(room -> room.getName().contains(userName));
        return false;
    }
}
