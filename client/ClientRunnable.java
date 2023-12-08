import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ClientRunnable extends Thread {

    private Socket socket;
    private DataInputStream input;
    public boolean clientRun;
    private JTextArea chat;
    // private Scanner scanner;

    public ClientRunnable(Socket socket, JTextArea chat) {        
        this.clientRun = true;
        // this.scanner = scanner;
        this.socket = socket;
        this.chat = chat;
    }
    
    @Override
    public void run() {

        
            try {
                this.input = new DataInputStream(socket.getInputStream());
                while(!socket.isClosed()) {
                    if (input.available() != 0) {
                            String response = input.readUTF();
                            chat.setText(chat.getText() + response + "\n");
                            System.out.println(response);
                        if (response.equals("exit")) {
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                            clientRun = false;
                            out.writeUTF(response);
                            // scanner.close();
                            socket.close();
                        }
                    }
                    sleep(50);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }
}
