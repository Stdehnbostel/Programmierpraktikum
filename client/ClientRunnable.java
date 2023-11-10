import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientRunnable implements Runnable {

    private Socket socket;
    private DataInputStream input;

    public ClientRunnable(Socket client) throws IOException {
        this.socket = client;
        this.input = new DataInputStream(socket.getInputStream());
    }
    @Override
    public void run() {
        
            try {
                while(true) {
                    String response = input.readUTF();
                    if (response.equals("exit")) {
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
