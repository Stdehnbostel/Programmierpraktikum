import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputListener extends Thread {

    public Message msg;
    BufferedReader br;

    InputListener(Message msg, BufferedReader br) {
        this.msg = msg;
        this.br = br;
    }

    public void run() {



        while (true) {
            synchronized(this.msg) {

                try {
                    msg.msg = br.readLine();
                    this.msg.notify();
                } catch (IOException e) {
                    System.out.println("Error read line");
                }
            }
            try {
                sleep(500);
            } catch (InterruptedException e) {
        
            }
        }
        
    }


}