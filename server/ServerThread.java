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
    public Room room;
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
        this.room = null;
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
                } else if (!(in instanceof Message)) {
                    msg.sendToAllClients("[" + this.userName + "]: " + outputString);
                    chat.append("[" + this.userName + "]: " + outputString + "\n");
                }
                System.out.println("Server received " + outputString);

                if (in instanceof Message) {
                    Message incoming = (Message)in;
                    if (incoming.type.equals("Room") && !((String)incoming.msg).equals("")) {
                        for (Room room: roomList) {
                            if (room.getName().equals((String)incoming.msg)) {
                                this.room = room;
                                for (ServerThread client: threadList) {
                                    if (client.userName.equals(this.userName)) {
                                        room.addUser(client);
                                        System.out.println("Add user to room");
                                    }
                                }
                            }
                        } 
                    } else if (incoming.type.equals("Room") && ((String)incoming.msg).equals("")) {
                        for (ServerThread client: this.room.getUserList()) {
                            if (client.userName.equals(this.userName)) {
                                this.room.removeUser(client);
                                System.out.println("remove user from room");
                                this.room = null; 
                            }
                        }
                        this.room = null;
                    } else if (this.room != null) {
                        msg.sendToSomeClients(room.getUserList(), in);
                    } else {
                        msg.sendToAllClients(in);
                    }
                    System.out.println("Room is set? " + this.room != null);
                }
            }


        } catch (Exception e) {
            System.out.println("Error occured " + e.getStackTrace());
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
