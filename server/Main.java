import java.net.*;
import java.util.*;

public class Main {

    public static void main(String args[]) {

        boolean run = true;
        ArrayList<ServerThread> clients = new ArrayList<ServerThread>();
        

        try (ServerSocket server = new ServerSocket(1234)){
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
}