import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
    
            // Prompt the client for the username
            out.writeObject("Nutzernamen eingeben:");
            out.flush();
            Object uName = in.readObject();
            
            String userName = "";
            if (uName instanceof String) {
                userName = uName.toString();
                System.out.println(userName + " enters the server");
            }

            String newUser = "";
            ServerThread comThread = searchUser(clients, userName);

            boolean nameAvailable = isAvailable(comThread);
    
            if (nameAvailable) {
                // If the username is not in use, prompt the client to register 
                out.writeObject("Registriere dich mit deinem Passwort:");
                out.flush();
                Object pw = in.readObject();
                String pwd = "";
                if (pw instanceof String) {
                    pwd = pw.toString();
                }
                comThread = new ServerThread(client, clients, userName, pwd, chat, in, out);
                clients.add(comThread);
                comThread.start();
                newUser = "* " + userName + " hat sich registriert! *";
                chat.append(newUser + "\n");
                String userList = msg.generateUserList(clients);
                Message users = new Message("String", userList);
                System.out.println("send User List: \n");
                System.out.println(users);
                msg.sendToAllClients(users);
                out.flush();
                msg.sendToAllClients(newUser);
            } else {
                // If the username is already in use, prompt the client for authentication
                out.writeObject("Log dich mit deinem Passwort ein:");
                out.flush();
                Object pw = in.readObject();
                String pwd = "";
                if (pw instanceof String) {
                    pwd = pw.toString();
                }
                if (comThread.getPwd().equals(pwd)) {
                    System.out.println("Passwort korrekt");
                    // Password is correct
                    clients.remove(comThread);
                    comThread = new ServerThread(client, clients, userName, pwd, chat, in, out);
                    clients.add(comThread);
                    comThread.start();
                    out.writeObject("Login erfolgreich!");
                    out.flush();
                    newUser = "* " + userName + " hat sich angemeldet! *";
                    String userList = msg.generateUserList(clients);
                    Message users = new Message("String", userList);
                    System.out.println("send User List: \n");
                    System.out.println(users);
                    msg.sendToAllClients(users);
                    out.writeObject(users);
                    out.flush();
                    chat.append(newUser + "\n");
                    msg.sendToAllClients(newUser);
                } else {
                    // Password is incorrect
                    out.writeObject("Falsches Passwort! Verbindung wird beendet.");
                    out.flush();
                    client.close();
                }
                
            }
    
            String userList = msg.generateUserList(clients);
            Message users = new Message("String", userList);
            System.out.println("send User List: \n");
            System.out.println(users);
            out.writeObject(users);
            out.flush();

        } catch (IOException e) {
            System.out.println("Exception occured in LoginHandler " + e);
        } catch (ClassNotFoundException ce) {
            System.out.println("ClassNotFoundExeption occurd: " + ce);
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