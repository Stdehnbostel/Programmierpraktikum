import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class LoginHandler extends Thread {

    private Socket client;
    private ArrayList<ServerThread> clients;

    public LoginHandler(Socket client, ArrayList<ServerThread> clients) {
        this.client = client;
        this.clients = clients;
    }

    public void run() {
        processLoginRequest();
    }

    private void processLoginRequest() {
        ServerMessages msg = new ServerMessages(clients, null);
        
        try {
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());
    
            // Prompt the client for the username
            out.writeUTF("Nutzernamen eingeben:");
            String userName = in.readUTF();
    
            ServerThread comThread = searchUser(clients, userName);

            boolean nameAvailable = isAvailable(comThread);
    
            if (nameAvailable) {
                // If the username is not in use, prompt the client to register 
                out.writeUTF("Registriere dich mit deinem Passwort:");
                String pwd = in.readUTF();
                comThread = new ServerThread(client, clients, userName, pwd);
                clients.add(comThread);
                comThread.start();
                msg.sendToAllClients("* " + userName + " hat sich registriert! *");
            } else {
                // If the username is already in use, prompt the client for authentication
                out.writeUTF("Log dich mit deinem Passwort ein:");
                String pwd = in.readUTF();
                if (comThread.getPwd().equals(pwd)) {
                    // Password is correct
                    clients.remove(comThread);
                    comThread = new ServerThread(client, clients, userName, pwd);
                    clients.add(comThread);
                    comThread.start();
                    out.writeUTF("Login erfolgreich!");
                    msg.sendToAllClients("* " + userName + " hat sich angemeldet! *");
                } else {
                    // Password is incorrect
                    out.writeUTF("Falsches Passwort! Verbindung wird beendet.");
                    // clients.remove(comThread) | dachte zuerst das würde Sinn ergeben, löscht aber natürlich den ganzen User, also müsste sich der Client neu registrieren;
                    client.close();
                }
                
            }
    
            String users = msg.generateUserList(clients);
            out.writeUTF(users);

        } catch (IOException e) {
            System.out.println("Exception occured in LoginHandler " + e);
        }
    }

    private ServerThread searchUser(ArrayList<ServerThread> clients, String name) {
        for (ServerThread sT : clients) {
            if (sT.userName.equals(name)) {
                return sT;
            }
        }
        return null;
    }

    private boolean isAvailable(ServerThread client) {
        if (client == null) {
            return true;
        }
        return false;
    }
}