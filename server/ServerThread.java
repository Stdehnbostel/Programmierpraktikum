import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class ServerThread extends Thread implements Serializable {

    public transient Socket client;
    private transient ArrayList<ServerThread> threadList;
    private transient ArrayList<Room> roomList;
    public String userName;
    public String room;
    private String pwd;
    private boolean online;
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
                    String userList = msg.generateUserList(threadList);
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
                                for (ServerThread client: threadList) {
                                    if (client.userName.equals(this.userName)) {
                                        room.addUser(client);
                                        System.out.println("Add user to room");
                                    }
                                }
                            }
                        } 
                    } else if (incoming.type.equals("Room") && ((String)incoming.msg).equals("")) {
                        msg.removeUserFromRoom(userName, room, roomList);
                        this.room = "";
                    } else if (!this.room.equals("")) {
                        msg.sendToRoom(room, roomList, in);
                    } else {
                        msg.sendToAllClients(in);
                    }
                }
            }


        } catch (Exception e) {
            System.out.println("Error occured " + e + e.getStackTrace());
            online = false;
            String userList = msg.generateUserList(threadList);
            Message users = new Message("String", userList);
            msg.sendToAllClients(users);
            msg.sendToAllClients("* " + this.userName + " hat sich abgemeldet! *");
            chat.append("* " + this.userName + " hat sich abgemeldet! *" + "\n");
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

    public void setChat(JTextArea chat) {
        this.chat = chat;
    }

    public String getRoomName() {
        return this.room;
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
}
