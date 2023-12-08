import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class LoginHandler extends Thread {

    private Socket client;
    private ArrayList<ServerThread> clients;
    private JTextArea chat;

    public LoginHandler(Socket client, ArrayList<ServerThread> clients, JTextArea chat) {
        this.client = client;
        this.clients = clients;
        this.chat = chat;
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
            String newUser = "";
            ServerThread comThread = searchUser(clients, userName);

            boolean nameAvailable = isAvailable(comThread);
    
            if (nameAvailable) {
                // If the username is not in use, prompt the client to register 
                out.writeUTF("Registriere dich mit deinem Passwort:");
                String pwd = in.readUTF();
                comThread = new ServerThread(client, clients, userName, pwd, chat);
                clients.add(comThread);
                comThread.start();
                newUser = "* " + userName + " hat sich registriert! *";
                chat.append(newUser + "\n");
                msg.sendToAllClients(newUser);
            } else {
                // If the username is already in use, prompt the client for authentication
                out.writeUTF("Log dich mit deinem Passwort ein:");
                String pwd = in.readUTF();
                if (comThread.getPwd().equals(pwd)) {
                    // Password is correct
                    clients.remove(comThread);
                    comThread = new ServerThread(client, clients, userName, pwd, chat);
                    clients.add(comThread);
                    comThread.start();
                    out.writeUTF("Login erfolgreich!");
                    newUser = "* " + userName + " hat sich angemeldet! *";
                    chat.append(newUser + "\n");
                    msg.sendToAllClients(newUser);
                } else {
                    // Password is incorrect
                    out.writeUTF("Falsches Passwort! Verbindung wird beendet.");
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