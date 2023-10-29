import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ComServThread extends Thread {

    private Thread sender;
    Socket client;
    Message msgout;
    Message msgin;
    String name;
    String pwd;

    ComServThread(Socket client, Message msg) {
        this.client = client;
        this.msgout = msg;
        this.msgin = new Message();
        this.name = "";
        this.pwd = "";
    }

    public void run() {

        boolean run = true;
        sender = new Thread(() -> send());
        sender.start();

        while (this.name.equals("") || this.pwd.equals("")) {
            try {
                DataInputStream in = new DataInputStream(client.getInputStream());
                String cliMessage = in.readUTF();
                if (this.name.equals("")) {
                    this.name = cliMessage;
                } else if (this.pwd.equals("")) {
                    this.pwd = cliMessage;
                }
            } catch (IOException e) {

            }
        }

        try {

            while (run) {
                DataInputStream in = new DataInputStream(client.getInputStream());

                String cliMessage = in.readUTF();
                System.out.println(cliMessage);

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
}