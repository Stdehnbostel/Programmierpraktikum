import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatClientUI extends JFrame {
    private JTextArea chatArea;
    private JTextArea userList;
    private JTextField inputField;
    private JButton sendButton;
    private JButton fileButton;
    private JButton chooseRoomButton;
    private Main socketConnection;
    private JFileChooser fileChooser;
    private String chat;
    private LinkedList<BufferedImage> images;
    private LinkedList<byte[]> pdfs;
    private HashMap<String, PrivateChat> privateChats;
    private String[] privateMessage;
    private JList<String> chatRoomList;
    private ArrayList<String> currentRoom;

    public ChatClientUI() {
        this.chat = "";
        this.images = new LinkedList<BufferedImage>();
        this.pdfs = new LinkedList<byte[]>();
        this.privateChats = new HashMap<String, PrivateChat>();
        this.privateMessage = new String[2];
        this.socketConnection = new Main("localhost", this.images, this.pdfs, this.privateMessage);
        this.currentRoom = new ArrayList<String>();
        
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
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

        String[] chatRooms = {};
        chatRoomList = new JList<>(chatRooms);
        chatRoomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatRoomList.addListSelectionListener(e -> {
            String selectedChatRoom = chatRoomList.getSelectedValue();
            System.out.println("selected: " + selectedChatRoom);
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

        chooseRoomButton = new JButton("Raum wählen");
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

        JPanel roomOptionsPanel = new JPanel(new BorderLayout());
        JButton privateChatButton = new JButton("privater Raum");
        chooseRoomButton.addActionListener(e -> {
            if (chooseRoomButton.getText().equals("Raum verlassen")) {
                socketConnection.sendMessage(new Message("Room", ""));
                currentRoom.clear();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        chooseRoomButton.setText("Raum wählen");
                    }
                });
            } else if (chatRoomList.getSelectedValue() != null){
                String selectedChatRoom = chatRoomList.getSelectedValue();
                ArrayList<String> breakDownRoomName = new ArrayList<String>(Arrays.asList(selectedChatRoom.split(" "))); 
                breakDownRoomName.remove(breakDownRoomName.size() - 1);
                breakDownRoomName.remove(breakDownRoomName.size() - 1);
                selectedChatRoom = "";
                for (int i = 0; i < breakDownRoomName.size(); i++) {
                    if (i > 0) {
                        selectedChatRoom += " ";
                    }
                    selectedChatRoom += breakDownRoomName.get(i);
                    System.out.println(selectedChatRoom);
                }
                final String roomName = selectedChatRoom;
                currentRoom.add(roomName);
                socketConnection.sendMessage(new Message("Room", roomName));
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        chooseRoomButton.setText("Raum verlassen");
                    }
                });
            }
        });
        privateChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userList.getText().length() > 8) {
                    openPrivateChatDialog(getOnlineList());
                }
            }
        });
        roomOptionsPanel.add(privateChatButton, BorderLayout.SOUTH);
        roomOptionsPanel.add(chooseRoomButton, BorderLayout.NORTH);
        roomListPanel.add(roomOptionsPanel, BorderLayout.SOUTH);

        JSplitPane splitUsersAndRooms = new JSplitPane(JSplitPane.VERTICAL_SPLIT, userListPanel, roomListPanel);
        splitUsersAndRooms.setResizeWeight(0.6);
        roomsAndUsers.add(splitUsersAndRooms, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPanel, roomsAndUsers);
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);

        JButton loginButton = new JButton("Anmelden/Registrieren");
        loginButton.addActionListener(e -> openLoginWindow());
        add(loginButton, BorderLayout.NORTH);
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
        privateMessage[0] = "";
        while (!socketConnection.getSocket().isClosed()) {
            try {
                Object msg = in.readObject();
                if (msg instanceof String) {
                    if (msg.equals("exit")) {
                        socketConnection.exit((String)msg);
                }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            chatArea.setText(chatArea.getText() + msg + "\n");
                        }
                    });
                } else if (msg instanceof Message) {
                    socketConnection.decodeMessage((Message)msg, currentRoom);
                    if (currentRoom.isEmpty()) {
                        adjustUserList("");
                        chooseRoomButton.setText("Raum wählen");
                    } else {
                        adjustUserList(currentRoom.get(0));
                    }
                    if (!privateMessage[0].equals("")) {
                        if (privateChats.containsKey(privateMessage[0])) {
                            PrivateChat privateChat = privateChats.get(privateMessage[0]);
                            if (privateMessage[1].equals("~//leaveRoom")) {
                                privateChat.show = false;
                            }
                            privateChat.chat.setText(privateChat.chat.getText() + privateMessage[1] + "\n");
                            if ((!privateChat.show) && privateMessage[1].equals("")) {
                                String chatPartners[] = privateMessage[0].split("\n");
                                String chatPartner = chatPartners[0].equals(socketConnection.getUserName()) ? 
                                    chatPartners[1] : chatPartners[0];
                                openConsentWindow(chatPartner, privateMessage[0]);
                            }
                        } else {
                            String chatPartners[] = privateMessage[0].split("\n");
                            String chatPartner = chatPartners[0].equals(socketConnection.getUserName()) ? 
                                chatPartners[1] : chatPartners[0];
                            privateChats.put(privateMessage[0], new PrivateChat(new JTextArea(), false));
                            openConsentWindow(chatPartner, privateMessage[0]);
                        }
                        privateMessage[0] = "";
                    }
                }
            } catch (ClassNotFoundException ce) {
                System.out.println("" + ce + ce.getStackTrace());
            } catch (IOException e) {
                System.out.println("" + e + e.getStackTrace());
            }
        }
    }

    private void adjustUserList(String currentRoom) {
        if (currentRoom != null) {
            String text = userList.getText();

            if (currentRoom.isEmpty()) {
                return;
            }

            int offlineIndex = text.indexOf("Offline:");

            String onlineSection = (offlineIndex != -1) ? text.substring(0, offlineIndex) : text;

            String userPattern = "\\[.*?\\]" + Pattern.quote(" @") + Pattern.quote(currentRoom);
            Matcher matcher = Pattern.compile(userPattern).matcher(onlineSection);
            StringBuilder filteredUsers = new StringBuilder("Online:");

            while (matcher.find()) {
                filteredUsers.append("\n").append(matcher.group());
            }

            userList.setText(filteredUsers.toString());
        }
         
    }

    private String[] getOnlineList() {
        String text = userList.getText();
        int offlineIndex = text.indexOf("Offline:");
        String onlineSelection = (offlineIndex != -1) ? text.substring(8, offlineIndex) : text.substring(8, text.length());
        String onlineList[] = onlineSelection.split("\n");
        Pattern userPattern = Pattern.compile("\\[.*\\]");
        for (int i = 0; i < onlineList.length; i++) {
            Matcher matcher = userPattern.matcher(onlineList[i]);
            if (matcher.find()) {
                onlineList[i] = matcher.group();
            }
        }   
        return onlineList;
    }

    private void openPrivateChatDialog(String [] userList) {
        JFrame openPrivateChat = new JFrame("privaten Raum eröffnen");
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JLabel userLabel = new JLabel("Verfügbare User");
        JList<String> selectableUserList = new JList<>(userList);
        selectableUserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollUser = new JScrollPane();
        scrollUser.setViewportView(selectableUserList);
        scrollUser.setVerticalScrollBar(scrollUser.createVerticalScrollBar());
        JButton startChatButton = new JButton("Raum eröffnen");
        startChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = selectableUserList.getSelectedValue();
                if (userName != null && !userName.equals("[" + socketConnection.getUserName() + "]")) {
                    userName = userName.substring(1, userName.length() - 1);
                    String chatName = createPrivateChatName(socketConnection.getUserName(), userName); 
                    privateChats.put(chatName, new PrivateChat(new JTextArea(), true));
                    openPrivateChat(userName, chatName);
                    openPrivateChat.dispose();
                }
            }
        });
        dialogPanel.add(userLabel, BorderLayout.NORTH);
        dialogPanel.add(scrollUser, BorderLayout.CENTER);
        dialogPanel.add(startChatButton, BorderLayout.SOUTH);
        openPrivateChat.add(dialogPanel);
        openPrivateChat.setSize(500, 300);
        openPrivateChat.setVisible(true);
    }

    private void openPrivateChat(String userName, String privateChatName) {
        PrivateChat privateChat = privateChats.get(privateChatName);
        JFrame privateChatFrame = new JFrame("privater Chat mit " + userName);
        JPanel chatPanel = new JPanel(new BorderLayout());
        JButton leaveChatButton = new JButton("privaten Chat verlassen");
        JScrollPane scrollPrivateChat = new JScrollPane();
        scrollPrivateChat.setViewportView(privateChat.chat);
        scrollPrivateChat.setVerticalScrollBar(scrollPrivateChat.createVerticalScrollBar());
        JTextField chatInput = new JTextField();
        JButton sendButton = new JButton("senden");
        JButton sendFileButton = new JButton("f");
        JPanel sendButtons = new JPanel(new BorderLayout());
        sendButtons.add(sendFileButton, BorderLayout.WEST);
        sendButtons.add(sendButton, BorderLayout.EAST);
        sendButtons.add(chatInput, BorderLayout.CENTER);
        chatPanel.add(leaveChatButton, BorderLayout.NORTH);
        chatPanel.add(scrollPrivateChat, BorderLayout.CENTER);
        chatPanel.add(sendButtons, BorderLayout.SOUTH);
        privateChatFrame.add(chatPanel);
        privateChatFrame.setSize(500, 300);
        privateChatFrame.setVisible(privateChat.show);
        String msg[] = {privateChatName, ""};
        socketConnection.sendMessage(new Message("Private", msg));
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg[] = {privateChatName, "[" + socketConnection.getUserName() + "]: " + chatInput.getText()};
                socketConnection.sendMessage(new Message("Private", msg));
                chatInput.setText("");
            }
        });
        sendFileButton.addActionListener(e -> {
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
        leaveChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg[] = {privateChatName, "~//leaveRoom"};
                socketConnection.sendMessage(new Message("Private", msg));
                privateChat.show = false;
                privateChatFrame.dispose();
            }
        });
    }
    
    private boolean openConsentWindow(String userName, String privateChatName) {
        PrivateChat privateChat = privateChats.get(privateChatName);
        JFrame consentFrame= new JFrame("Chatanfrage");
        consentFrame.setSize(300, 200);
        consentFrame.setLocationRelativeTo(null);

        JPanel consentPanel = new JPanel(new GridLayout(2, 1));

        JLabel consentLabel = new JLabel(userName + " möchte einen privaten Chat starten");
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton consentButton = new JButton("Ja");
        JButton denyConsentButton = new JButton("Nein");
        buttonPanel.add(denyConsentButton);
        buttonPanel.add(consentButton);

        consentPanel.add(consentLabel);
        consentPanel.add(buttonPanel);
        consentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                privateChat.show = true;
                openPrivateChat(userName, privateChatName); 
                consentFrame.dispose();
            }
        });

        denyConsentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                privateChat.show = false;
                consentFrame.dispose();
            }
        });
        consentFrame.add(consentPanel);
        consentFrame.setVisible(true);
        return privateChat.show;
    }

    private String createPrivateChatName(String thisUser, String otherUser) {
       return thisUser.compareTo(otherUser) > 1 ? 
            thisUser + "\n" + otherUser : otherUser + "\n" + thisUser; 
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
