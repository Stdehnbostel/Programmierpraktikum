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
            boolean nameAvailable = true;
            System.out.println("Ask for username");
            out.writeUTF("Nutzernamen eingeben:");
            String userName = in.readUTF();

            out.writeUTF("Passwort eingeben:");                
            String pwd = in.readUTF();

            System.out.println("Name: " + userName + " pwd: " + pwd);

            StringBuilder users = new StringBuilder();
            users.append("Auf dem Server:\n");
            ServerThread comThread = new ServerThread(client, clients, userName, pwd);
            
            for(ServerThread sT: clients) {
                if(sT.userName.equals(userName) && !sT.getPwd().equals(pwd)) {
                    nameAvailable = false;
                }
                users.append(sT.userName + "\n");
            }
            if(nameAvailable == false) {
                out.writeUTF("Der Name existiert bereits.");
                receiveUserAndPwd(client, clients);
            } else {
                 sendServerMessage(clients, userName + " hat sich neu angemeldet");
                out.writeUTF(users.toString());
                clients.add(comThread);
                comThread.start();
            }
        } catch (IOException e) {

        }
    }

    private static void sendServerMessage(ArrayList<ServerThread> clients, String msg) {
        try {
            for(ServerThread sT: clients) {
                DataOutputStream out = new DataOutputStream(sT.getSocket().getOutputStream());
                out.writeUTF(msg);
            }
        } catch (IOException e) {
            System.out.println("IOExeption occurred in Main");
        }
    }
}