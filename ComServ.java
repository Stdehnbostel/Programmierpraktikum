import java.net.*;
import java.io.*;
import java.util.*;


public class ComServ extends Thread {

    private boolean runServer;
    Message msg;
    private Thread sender;
    LinkedList<ComServThread> clients = new LinkedList<ComServThread>();

    ComServ() {
        this.runServer = true;
        this.msg = new Message();
    }

    public void run() {

        

        try {

            ServerSocket server = new ServerSocket(1234);
            System.out.println("Port: 1234");
            sender = new Thread(() -> send());
            sender.start();

            while(runServer) {

                Socket client = server.accept();
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                DataInputStream in = new DataInputStream(client.getInputStream());

                System.out.println("Ask for username");
                out.writeUTF("Nutzernamen eingeben: ");
                String userName = in.readUTF();

                out.writeUTF("Passwort eingeben: ");
                String pwd = in.readUTF();

                System.out.println("Name: " + userName + " pwd: " + pwd);

                ComServThread comThread = new ComServThread(client, msg, userName, pwd);
                clients.push(comThread);
                comThread.start();

            }
            System.out.println("Nach der Schleife");
            server.close();
            
        } catch (IOException e) {

        }
        
    }

    public void stopServer() {
        this.runServer = false;
    }

    public void send() {
                
        while (true) {
            synchronized(this.msg) {
                if (!msg.msg.equals("")) {
                    for (int i = 0; i < clients.size(); i++) {
                        // System.out.println("Send Message: " + this.msg.msg);
                        ComServThread client = clients.get(i);                            
                        client.setMessagein(msg.msg);
                        // System.out.println("An: " + client.name);
                        
                    }
                    this.msg.msg = "";
                }
                this.msg.notifyAll();
            }
            try {
                sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }
}