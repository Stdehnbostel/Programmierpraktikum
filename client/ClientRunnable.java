import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientRunnable implements Runnable {

    private Socket socket;
    private DataInputStream input;
    public boolean clientRun;
    private Scanner scanner;

    public ClientRunnable(Socket client, Scanner scanner) throws IOException {
        this.socket = client;
        this.input = new DataInputStream(socket.getInputStream());
        this.clientRun = true;
        this.scanner = scanner;
    }
    @Override
    public void run() {
        
            try {
                while(clientRun) {
                    String response = input.readUTF();
                    if (response.equals("exit")) {
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        clientRun = false;
                        out.writeUTF(response);
                        scanner.close();
                        socket.close();
                    }
                    System.out.println(response);
                }
            } catch (IOException e) {
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
