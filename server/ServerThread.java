import java.io.DataInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {

    public Socket client;
    private ArrayList<ServerThread> threadList;
    public String userName;
    private String pwd;
    private boolean online;

    public ServerThread(Socket socket, ArrayList<ServerThread> threads, String name, String pwd) {
        this.client = socket;
        this.threadList = threads;
        this.userName = name;
        this.pwd = pwd;
        this.online = true;
    }

    @Override
    public void run() {
        ServerMessages msg = new ServerMessages(threadList, null);
        try {
            //Reading the input from Client
            DataInputStream input = new DataInputStream(client.getInputStream());

            //inifite loop for server
            while(true) {
                String outputString = input.readUTF();
                //if user types exit command
                if(outputString.equals("exit")) {
                    online = false;
                    msg.sendServerMessage(threadList, "* " + this.userName + " hat sich abgemeldet! *");
                    break;
                }
                msg.sendServerMessage(threadList, "[" + this.userName + "]: " + outputString);
                //output.println("Server says " + outputString);
                System.out.println("Server received " + outputString);
            }


        } catch (Exception e) {
            System.out.println("Error occured " + e.getStackTrace());
            online = false;
            msg.sendServerMessage(threadList, "* " + this.userName + " hat sich abgemeldet! *");
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
}
