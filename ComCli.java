import java.net.*;
import java.io.*;

public class ComCli extends Thread{

    String name;
    String pass;
    Message msg;
    BufferedReader br;
    private Thread incoming;

    ComCli(String name, String pwd, BufferedReader br) {
        this.name = name;
        this.pass = pwd;
        this.msg = new Message();
        this.br = br;
    }

    public void run() {

        InputListener ip = new InputListener(msg, br);
        ip.start();

        boolean runClient = true;
        
        try {
            Socket server = new Socket("localhost", 1234);
            DataInputStream in = new DataInputStream(server.getInputStream());
            DataOutputStream out = new DataOutputStream(server.getOutputStream());

            out.writeUTF(name);
            out.writeUTF(pass);
            incoming = new Thread(() -> read(in));
            incoming.start();
            while (in.available() != 0) {
                String incoming = in.readUTF();
                System.out.println(incoming);   
            }

            while (runClient) {
                synchronized (this.msg) {
                    try {
                        while (this.msg.msg.length() < 1) {
                            this.msg.wait();
                        }
                    } catch (InterruptedException e) {

                    }
                    String message = msg.msg;
                    out.writeUTF(name + " schreibt: " + message);
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