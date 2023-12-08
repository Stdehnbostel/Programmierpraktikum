import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

import javax.swing.JTextArea;

public class Main extends Thread {

    private String serverName;
    private String userName;
    private String pwd;
    private Socket socket;
    private JTextArea chat;
    private JTextArea userList;
    private ObjectOutputStream out;

    public Main(String serverName) {
        this.serverName = serverName;
    }

    public void run() {
        
        try {
            this.socket = new Socket(serverName, 1234);

            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());

            Object msg;

            msg = input.readObject();
            
            String requestUsername = "";

            if (msg instanceof String) {
                requestUsername = msg.toString();
            }
                
            if(requestUsername.equals("Nutzernamen eingeben:")) {
                out.writeObject(this.userName);
                out.flush();
            }

            msg = input.readObject();
            String requestPwd = "";
            
            if (msg instanceof String) {
                requestPwd = msg.toString();
            }

            if(requestPwd.equals("Log dich mit deinem Passwort ein:") ||
                requestPwd.equals("Registriere dich mit deinem Passwort:")) {
                out.writeObject(this.pwd);
                out.flush();
                System.out.println(requestPwd);
            }
           
           while(!socket.isClosed()) {
            
            msg = input.readObject();
            if (msg instanceof String) {
                String response = msg.toString();
                chat.setText(chat.getText() + response + "\n");
                System.out.println(response);

                if (response.equals("exit")) {
                out.writeObject(response);
                socket.close();
                }
            }
            if (msg instanceof UserList) {
                String response = msg.toString();
                userList.setText(response);
            }
        }
            
        } catch (IOException ie) {
            System.out.println("IOException occured in client main: " + ie + ie.getStackTrace());
        } catch (ClassNotFoundException ce) {
            System.out.println("ClassNotFoundException occured in client main: " + ce.getStackTrace());
        }catch (Exception e) {
            System.out.println("Exception occured in client main: " + e.getStackTrace());
        } 
    }

    public void send(String msg) {
        try{
            out.writeObject(msg);
        } catch (IOException e) {
            System.out.println("IOException occurd in Main" + e);
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setChat(JTextArea chat) {
        this.chat = chat;
    }

    public void setUserList(JTextArea userList) {
        this.userList = userList;
    }
}