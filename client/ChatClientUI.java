import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatClientUI extends JFrame {
    private JTextArea chatArea;
    private JTextArea userList;
    private JTextField inputField;
    private JButton sendButton;
    private JButton fileButton;
    private Main socketConnection;
    private JFileChooser fileChooser;
    private String chat;
    private LinkedList<BufferedImage> images;
    private LinkedList<byte[]> pdfs;
    private JList<String> chatRoomList;

    public ChatClientUI() {
        this.chat = "";
        this.images = new LinkedList<BufferedImage>();
        this.pdfs = new LinkedList<byte[]>();
        this.socketConnection = new Main("localhost", this.images, this.pdfs);
        
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();
        Thread waitForImges = new Thread() {
            @Override
            public void run() {
                while(true) {
                    if(!images.isEmpty()) {
                        askToShowPicture(images.pop());
                    }
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
            }
        };
        waitForImges.start();

        Thread waitForPdfs = new Thread() {
            @Override
            public void run() {
                while(true) {
                    if(!pdfs.isEmpty()) {
                        askToShowPdf(pdfs.pop());
                    }
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
            }
        };
        waitForPdfs.start();

        setVisible(true);
    }

    private void initComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setText(chat);
        chatArea.setVisible(true);

        userList = new JTextArea();
        userList.setEditable(false);
        userList.setVisible(true);
        userList.setBackground(getForeground());

        String[] chatRooms = {"Room 1", "Room 2", "Room 3", "Room 4"};
        chatRoomList = new JList<>(chatRooms);
        chatRoomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatRoomList.addListSelectionListener(e -> {
            String selectedChatRoom = chatRoomList.getSelectedValue();
            // Handle selected chat room
        });
        socketConnection.setUserList(userList);
        socketConnection.setRoomList(chatRoomList);

        inputField = new JTextField();
        inputField.addActionListener(e -> socketConnection.send(inputField.getText()));

        sendButton = new JButton("Senden");
        sendButton.addActionListener(e -> {
            socketConnection.send(inputField.getText());
            inputField.setText("");
        });

        String icon = "";
        byte[] bIcon = {(byte) 0x1F, (byte) 0xC4};
        try {
            icon = new String(bIcon, "UTF-8");
        } catch (Exception e) {
            System.out.println("Encoding Exception occurred " + e);
        }

        fileChooser = new JFileChooser();
        fileButton = new JButton(icon);
        fileButton.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(inputField);
            System.out.println(returnVal);
            File f = fileChooser.getSelectedFile();
            inputField.setText(f.toString());
            Pattern pdf = Pattern.compile(".*.pdf");
            Matcher matcher = pdf.matcher(f.toString());
            if (matcher.matches()) {
                System.out.println("Send a pdf...");
                socketConnection.sendPdf(f.toString());
            } else {
                socketConnection.sendPic(f.toString());
            }
        });
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JLabel("Chat"), BorderLayout.NORTH);
        JScrollPane scrollChat = new JScrollPane();
        scrollChat.setViewportView(chatArea);
        scrollChat.setVerticalScrollBar(scrollChat.createVerticalScrollBar());
        chatPanel.add(scrollChat, BorderLayout.CENTER);
        JPanel userInputs = new JPanel(new BorderLayout());
        userInputs.add(inputField, BorderLayout.CENTER);
        userInputs.add(sendButton, BorderLayout.EAST);
        userInputs.add(fileButton, BorderLayout.WEST);
        chatPanel.add(userInputs, BorderLayout.SOUTH);

        JPanel roomsAndUsers = new JPanel(new BorderLayout());
        JPanel userListPanel = new JPanel(new BorderLayout());
        userListPanel.add(new JLabel("User List"), BorderLayout.NORTH);
        JScrollPane scrollUsers = new JScrollPane();
        scrollUsers.setViewportView(userList);
        scrollUsers.setVerticalScrollBar(scrollUsers.createVerticalScrollBar());
        userListPanel.add(scrollUsers, BorderLayout.CENTER);
        roomsAndUsers.add(userListPanel, BorderLayout.NORTH);

        JPanel roomListPanel = new JPanel(new BorderLayout());
        roomListPanel.add(new JLabel("Räume"), BorderLayout.NORTH);
        JScrollPane scrollRooms = new JScrollPane();
        scrollRooms.setViewportView(chatRoomList);
        scrollRooms.setVerticalScrollBar(scrollRooms.createVerticalScrollBar());
        roomListPanel.add(scrollRooms, BorderLayout.CENTER);
        roomsAndUsers.add(roomListPanel, BorderLayout.SOUTH);

        JButton chooseRoomButton = new JButton("Raum\n wählen");
        //chooseRoomButton.addActionListener(e -> openChooseRoomWindow(roomList.getText()));
        roomListPanel.add(chooseRoomButton, BorderLayout.SOUTH);

        JSplitPane splitUsersAndRooms = new JSplitPane(JSplitPane.VERTICAL_SPLIT, userListPanel, roomListPanel);
        splitUsersAndRooms.setResizeWeight(0.6);
        roomsAndUsers.add(splitUsersAndRooms, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPanel, roomsAndUsers);
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);

        JButton loginButton = new JButton("Anmelden/Registrieren");
        loginButton.addActionListener(e -> openLoginWindow());
        add(loginButton, BorderLayout.NORTH);
        initializeChatRoomList();
    }

    private void initializeChatRoomList() {



    }

    private void openLoginWindow() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(300, 200);
        loginFrame.setLocationRelativeTo(null);

        JPanel loginPanel = new JPanel(new GridLayout(3, 2));

        JLabel usernameLabel = new JLabel("Benutzername:");
        JTextField usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Passwort:");
        JPasswordField passwordField = new JPasswordField();

        JLabel invisLabel = new JLabel(""); // dient zum verschieben vom login button nach rechts, kann bestimmt schöner programmiert werden
        JButton loginButton = new JButton("Anmelden");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //tba
                socketConnection.setUserName(usernameField.getText());
                socketConnection.setPwd(new String(passwordField.getPassword()));
                ObjectInputStream in = socketConnection.login();
                Thread updateChat = new Thread(() -> getChatInput(in));
                updateChat.start();
                loginFrame.dispose();
            }
        });

        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(invisLabel);
        loginPanel.add(loginButton);

        loginFrame.add(loginPanel);
        loginFrame.setVisible(true);
    }

    /*private void openChooseRoomWindow(String roomList) {

        ArrayList<String> rooms = new ArrayList<String>(Arrays.asList(roomList.split("\n")));
        JFrame chooseFrame = new JFrame("Raum wählen");
        chooseFrame.setSize(400, 500);
        
        JPanel chooseRoomPanel = new JPanel(new BorderLayout());
        chooseRoomPanel.add(new JLabel("Räume"), BorderLayout.NORTH);
        JPanel roomListPanel = new JPanel(new BorderLayout());
        JScrollPane scrollRooms = new JScrollPane();
        JTextArea roomListArea = new JTextArea(roomList);
        roomListArea.setEditable(false);
        scrollRooms.setViewportView(roomListArea);
        scrollRooms.setVerticalScrollBar(scrollRooms.createVerticalScrollBar());
        roomListPanel.add(scrollRooms, BorderLayout.CENTER);
        JTextArea roomName = new JTextArea();
        roomListPanel.add(roomName, BorderLayout.SOUTH);
        chooseRoomPanel.add(roomListPanel, BorderLayout.CENTER); 
        JButton chooseRoomButton = new JButton("Raum\n wählen");

        chooseRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String room = roomName.getText();
                if (rooms.contains(room)) {
                    System.out.println("Raum gefunden");
                    socketConnection.sendMessage(new Message("Room", room));
                } else if (room.equals("")) {
                    socketConnection.sendMessage(new Message("Room", room));
                    System.out.println("Verlasse Raum");
                }
            }
        });
        chooseRoomPanel.add(chooseRoomButton, BorderLayout.SOUTH);

        chooseFrame.add(chooseRoomPanel);
        chooseFrame.setVisible(true);
    }*/
    private void askToShowPicture(BufferedImage img) {
        JFrame dialog = new JFrame();
        dialog.setSize(400, 100);
        dialog.setLocationRelativeTo(null);
        JPanel askBtnPanel = new JPanel(new FlowLayout());
        JLabel askForPermission = new JLabel("Ein Bild wurde empfangen. Soll es angezeigt werden?");
        JButton askBtn = new JButton("Bild anzeigen?");
        askBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            showPicture(img);
            }
        });
        askBtnPanel.add(askForPermission);
        askBtnPanel.add(askBtn);

        dialog.add(askBtnPanel);
        dialog.setVisible(true);
    }

    public void showPicture(BufferedImage img) {
        JFrame pic = new JFrame();
        pic.getContentPane().setLayout(new FlowLayout());
        pic.getContentPane().add(new JLabel(new ImageIcon(img)));
        pic.pack();
        pic.setVisible(true);
    }

    private void askToShowPdf(byte[] pdf) {
          
        JFrame dialog = new JFrame();
        dialog.setSize(400, 100);
        dialog.setLocationRelativeTo(null);
            JPanel askBtnPanel = new JPanel(new FlowLayout());
        JLabel askForPermission = new JLabel("Ein PDF wurde empfangen. Soll es angezeigt werden?");
        JButton askBtn = new JButton("PDF anzeigen?");
        askBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPdf(pdf);
            }
        });
        askBtnPanel.add(askForPermission);
        askBtnPanel.add(askBtn);

        dialog.add(askBtnPanel);
        dialog.setVisible(true);
    }

    public void showPdf(byte[] pdf) {
        try {
            File f = new File("../temp.pdf");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(pdf);
            fos.close();

            if (Desktop.isDesktopSupported()) {
            
                Desktop.getDesktop().open(f);
            }
        } catch (IOException e) {
            System.out.println("IOExeption occured in Main" + e + e.getStackTrace());
        }
    }

    private void getChatInput(ObjectInputStream in) {
        while (!socketConnection.getSocket().isClosed()) {
            try {
                Object msg = in.readObject();
                if (msg instanceof String) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            chatArea.setText(chatArea.getText() + "\n" + msg);
                        }
                    });
                } else if (msg instanceof Message) {
                    socketConnection.decodeMessage((Message)msg);
                }
            } catch (ClassNotFoundException ce) {
                System.out.println("" + ce + ce.getStackTrace());
            } catch (IOException e) {
                System.out.println("" + e + e.getStackTrace());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClientUI();
            }
        });
    }
}
