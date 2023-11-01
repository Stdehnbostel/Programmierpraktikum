import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ComServThread extends Thread {

    private Thread sender;
    private Socket client;
    private Message msgout;
    private Message msgin;
    String name;
    private String pwd;

    ComServThread(Socket client, Message msg, String userName, String pwd) {
        this.client = client;
        this.msgout = msg;
        this.msgin = new Message();
        this.name = userName;
        this.pwd = pwd;
    }

    public void run() {

        boolean run = true;
        sender = new Thread(() -> send());
        sender.start();

        try {

            while (run) {
                DataInputStream in = new DataInputStream(client.getInputStream());

                String cliMessage = name + " schreibt: " + in.readUTF();

                synchronized(this.msgout) {
                    try {
                        while (this.msgout.msg.length() > 0) {
                            this.msgout.wait();
                        }
                    } catch (InterruptedException e) {

                    }
                    msgout.msg = cliMessage;
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {

                }
            }

        } catch (IOException e) {

        } finally {
            if(client != null) {
                try {
                    client.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public void setMessagein(String msg) {
        this.msgin.msg = msg;
    }

    public void send() {
        try {
            
            while (true) {
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                if (!msgin.msg.equals("")) {
                    
                    String serMessage = msgin.msg;
                    msgin.msg = "";
                    out.writeUTF(serMessage);
                    System.out.println(serMessage);
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {

                } 
            }       
        } catch (IOException e) {

        }

    }

    public String getPwd() {
        return this.pwd;
    }
}