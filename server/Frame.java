import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;

public class Frame {
    public static void main(String[] args) {

        JFrame frame = new JFrame("Chat Server");
        frame.setSize(480, 640);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.RED);
        frame.setVisible(true);
        frame.setLayout(new GridLayout(2, 4));
        JButton startButton = new JButton("Server starten");
        JButton exitButton = new JButton("Server schließen");
        startButton.setLayout(null);
        startButton.setBounds(100, 0, 200, 30);
        exitButton.setBounds(100, 100, 100, 30);
        

        JLabel serverStatus = new JLabel("Server offline");
        JTextArea chat = new JTextArea();
        
        frame.add(startButton);
        frame.add(exitButton);
        frame.add(serverStatus);
        frame.add(chat);

        RunServer server = new RunServer(chat);

        ActionListener startButtonOnClick = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Thread serverThread = new Thread(() -> server.startServer());
                serverThread.start();
                serverStatus.setText("Server online");
            }
        };

        ActionListener exitButtonOnClick = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                server.exitServer();
                serverStatus.setText("Server offline");
            }
        };

        startButton.addActionListener(startButtonOnClick);
        exitButton.addActionListener(exitButtonOnClick);
    }
}