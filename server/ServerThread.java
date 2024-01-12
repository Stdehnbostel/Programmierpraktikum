import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class ServerThread extends Thread implements Serializable {

    public transient Socket client;
    public transient ArrayList<ServerThread> threadList;
    private transient ArrayList<Room> roomList;
    public String userName;
    public String room;
    private String pwd;
    private boolean online;
    private boolean banStatus;
    private transient JTextArea chat;
    private transient ObjectInputStream input;
    private transient ObjectOutputStream out;

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
        ServerMessages msg = new ServerMessages(threadList);
        try {
            //Reading the input from Client

            String outputString = "";
            //inifite loop for server
            while(true) {

                Object in = input.readObject();

                if (in instanceof String) {
                    outputString = in.toString();
                }
                //if user types exit command
                if(outputString.equals("exit")) {
                    online = false;
                    msg.sendToAllClients("* " + this.userName + " hat sich abgemeldet! *");
                    chat.append("* " + this.userName + " hat sich abgemeldet! *" + "\n");
                    String userList = msg.generateUserListWithRoom(threadList);
                    Message users = new Message("String", userList);
                    msg.sendToAllClients(users);
                    break;
                } else if (!(in instanceof Message) && room.equals("")) {
                    msg.sendToAllClients("[" + this.userName + "]: " + outputString);
                    chat.append("[" + this.userName + "]: " + outputString + "\n");
                } else if (!(in instanceof Message) && !room.equals("")) {
                    msg.sendToRoom(room, roomList, "[Raum: " + room + "] [" + this.userName + "]: " + outputString);
                    chat.append("[Raum: " + room + "] [" + this.userName + "]: " + outputString + "\n");
                }
                System.out.println("Server received " + outputString);

                if (in instanceof Message) {
                    Message incoming = (Message)in;
                    if (incoming.type.equals("Room") && !((String)incoming.msg).equals("")) {
                        for (Room room: roomList) {
                            if (room.getName().equals((String)incoming.msg)) {
                                this.room = room.getName();
                                ArrayList<ServerThread> userList = room.getUserList();
                                for (ServerThread client: threadList) {
                                    if (client.userName.equals(this.userName)) {
                                        room.addUser(client);
                                        msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(roomList)));
                                        System.out.println("Add user to room");
                                        msg.sendToAllClients(new Message("Users", msg.generateUserListWithRoom(threadList)));
                                    }

                                }
                                

                            }
                        } 
                    } else if (incoming.type.equals("Room") && ((String)incoming.msg).equals("")) {
                        msg.removeUserFromRoom(userName, room, roomList);
                        msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(roomList)));
                        this.room = "";
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
            logout(msg);
        }
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof ServerThread) {
            if (((ServerThread)o).userName.compareTo(this.userName) == 0) {
                return true;
            }
        }
        return false;
    }

    public void logout(ServerMessages msg) {
            if (!room.equals("")) {
                msg.removeUserFromRoom(userName, room, roomList);
            }
            online = false;
            String userList = msg.generateUserListWithRoom(threadList);
            Message users = new Message("String", userList);
            msg.sendToAllClients(users);
            msg.sendToAllClients("* " + this.userName + " hat sich abgemeldet! *");
            chat.append("* " + this.userName + " hat sich abgemeldet! *" + "\n");
            msg.sendToAllClients(new Message("Rooms", msg.generateRoomList(roomList)));
            this.out = null;
    }


}
