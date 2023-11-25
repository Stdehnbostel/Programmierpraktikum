import java.net.*;
import java.util.*;

public class Main {

    ServerSocket server;

    public void startServer() {

        boolean run = true;
        ArrayList<ServerThread> clients = new ArrayList<ServerThread>();
        

        try {
            this.server = new ServerSocket(1234);
            ServerMessages msg = new ServerMessages(clients, server);
            msg.start();
            while(run) {
                if (msg.msg.equals("exit")) {
                    run = false;
                    break;
                }
                Socket client = server.accept();
                LoginHandler newUser = new LoginHandler(client, clients);
                newUser.start();
                
            }
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e.getStackTrace());
        }
    }

    public void exitServer() {
        try {
            this.server.close();
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e.getStackTrace());
        }
    }
}