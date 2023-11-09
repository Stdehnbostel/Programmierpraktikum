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
    
            // Prompt the client for the username
            out.writeUTF("Nutzernamen eingeben:");
            String userName = in.readUTF();
    
            boolean nameAvailable = true;
         
            ServerThread comThread = null;
            
            // Check if the username is already in use
            for (ServerThread sT : clients) {
                if (sT.userName.equals(userName)) {
                    nameAvailable = false;
                    comThread = sT;
                    // Ich glaube hier müssten die Streams überschrieben werden mit den alten
                    // out = sT.out;
                    // in = sT.in;
                    // (so ungefähr?)
                    break;
                }
            }
    
            if (nameAvailable) {
                // If the username is not in use, prompt the client to register 
                out.writeUTF("Registriere dich mit deinem Passwort:");
                String pwd = in.readUTF();
                comThread = new ServerThread(client, clients, userName, pwd);
                clients.add(comThread);
                comThread.start();
                sendServerMessage(clients, "* " + userName + " hat sich registriert! *");
            } else {
                // If the username is already in use, prompt the client for authentication
                out.writeUTF("Log dich mit deinem Passwort ein:");
                String pwd = in.readUTF();
                if (comThread.getPwd().equals(pwd)) {
                    // Password is correct
                    out.writeUTF("Login erfolgreich!");
                    sendServerMessage(clients, "* " + userName + " hat sich angemeldet! *");
                } else {
                    // Password is incorrect
                    out.writeUTF("Falsches Passwort! Verbindung wird beendet.");
                    // clients.remove(comThread) | dachte zuerst das würde Sinn ergeben, löscht aber natürlich den ganzen User, also müsste sich der Client neu registrieren;
                    client.close();
                }
                
            }
    
            // Provide the list of users
            // todo: Online-Status in Client mit einbauen, und nur auf die userlist schreiben, falls der client wirklich online ist.
            // zur Zeit werden registrierte user, welche gar nicht online sind auch mit aufgeschrieben
            StringBuilder users = new StringBuilder("Auf dem Server:\n");
            int userNumber = 1;
            for (ServerThread sT : clients) {
                users.append(userNumber).append(". [").append(sT.userName).append("]\n");
                userNumber++;
            }
        out.writeUTF(users.toString());
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