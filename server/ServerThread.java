import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JTextArea;

public class ServerThread extends Thread implements Serializable {

    public transient Socket client;
    private transient ArrayList<ServerThread> threadList;
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
        String name, 
        String pwd, 
        JTextArea chat, 
        ObjectInputStream input,
        ObjectOutputStream out) {
        this.client = socket;
        this.threadList = threads;
        this.userName = name;
        this.pwd = pwd;
        this.online = true;
        this.chat = chat;
        this.input = input;
        this.out = out;
    }

    @Override
    public void run() {
        ServerMessages msg = new ServerMessages(threadList, null);
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
                    break;
                }
                msg.sendToAllClients("[" + this.userName + "]: " + outputString);
                chat.append("[" + this.userName + "]: " + outputString + "\n");
                System.out.println("Server received " + outputString);
            }


        } catch (Exception e) {
            System.out.println("Error occured " + e.getStackTrace());
            online = false;
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

    public void setChat(JTextArea chat) {
        this.chat = chat;
    }
}
