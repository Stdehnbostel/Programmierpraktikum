import java.net.*;
import java.io.*;

public class ComCli extends Thread{

    private String serverName;

    Message msg;
    private BufferedReader br;
    private Thread incoming;

    ComCli(String sn, BufferedReader br) {
        
        this.serverName = sn;

        this.msg = new Message();
        this.br = br;
    }

    public void run() {

        InputListener ip = new InputListener(msg, br);
        ip.start();

        boolean runClient = true;
        
        try {
            Socket server = new Socket(serverName, 1234);
            DataInputStream in = new DataInputStream(server.getInputStream());
            DataOutputStream out = new DataOutputStream(server.getOutputStream());

            incoming = new Thread(() -> read(in));
            incoming.start();

            while (runClient) {
                synchronized (this.msg) {
                    try {
                        while (this.msg.msg.length() < 1) {
                            this.msg.wait();
                        }
                    } catch (InterruptedException e) {

                    }
                    String message = msg.msg;
                    out.writeUTF(message);
                    msg.msg = "";
                }

            }

            server.close();
        } catch (UnknownHostException e) {
            System.out.println("Can't find server");
        } catch (IOException e) {
            System.out.println("Error connecting to host");
        }
    }

    public void read(DataInputStream in) {
                
        while (true ) {
            try {
                String incoming = in.readUTF();
                System.out.println(incoming);  
            } catch (IOException e) {

            }
            try {
                sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }
}