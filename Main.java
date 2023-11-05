import java.net.*;
import java.io.*;
import java.util.*;

public class Main {

    

    public static void main(String args[]) {

        ArrayList<ServerThread> clients = new ArrayList<ServerThread>();
        String input = "";
        try (ServerSocket server = new ServerSocket(1234)){
            
            while(!input.equals("exit")) {
                Socket client = server.accept();
                Thread newUser = new Thread(() -> receiveUserAndPwd(client, clients));
                newUser.start();
                
            }
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e.getStackTrace());
        }
    }

    private static void receiveUserAndPwd(Socket client, ArrayList<ServerThread> clients) {

        try {

            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());

            //abfrage-Kommunikation durch ClientMain
            System.out.println("Ask for username");
            out.writeUTF("Nutzernamen eingeben: \n");
            String userName = in.readUTF();

            out.writeUTF("Passwort eingeben: \n");                
            String pwd = in.readUTF();

            System.out.println("Name: " + userName + " pwd: " + pwd);

            ServerThread comThread = new ServerThread(client, clients, userName, pwd);
            sendServerMessage(clients, userName + " hat sich neu angemeldet\n");
            clients.add(comThread);
            comThread.start();

        } catch (IOException e) {

        }
    }

    private static void sendServerMessage(ArrayList<ServerThread> clients, String msg) {
        try {
            for(ServerThread sT: clients) {
                DataOutputStream out = new DataOutputStream(sT.client.getOutputStream());
                out.writeUTF(msg);
            }
        } catch (IOException e) {
            System.out.println("IOExeption occurred in Main");
        }
        

    }
}
