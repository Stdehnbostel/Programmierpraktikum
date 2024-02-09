import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
    private LinkedList<byte[]> sounds;
    private HashMap<String, PrivateChat> privateChats;
    private String[] privateMessage;
    private JList<String> chatRoomList;
    private ArrayList<String> currentRoom;

    public ChatClientUI() {
        this.chat = "";
        this.images = new LinkedList<BufferedImage>();
        this.pdfs = new LinkedList<byte[]>();
        this.sounds = new LinkedList<byte[]>();
        this.privateChats = new HashMap<String, PrivateChat>();
        this.privateMessage = new String[2];
        this.socketConnection = new Main(   "localhost", 
                                            this.images, 
                                            this.pdfs, 
                                            this.sounds, 
                                            this.privateMessage);
        this.currentRoom = new ArrayList<String>();
        
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();
        // Prüfe, ob Bilddateien eingegangen sind.
        // Falls ja, rufe Methode auf, die Abfragt, ob das Bild angezeigt werden soll
        Thread waitForImages = new Thread() {
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
        waitForImages.start();

        // Prüfe, ob Pdfs eingegangen sind.
        // Falls ja, rufe Methode auf, die Abfragt, ob das Pdf angezeigt werden soll
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
        
        // Prüfe, ob Sound-Dateien eingegangen sind.
        // Falls ja, rufe Methode auf, die Abfragt, ob die Datei abgespielt werden soll
        Thread waitForSounds = new Thread() {
            @Override
            public void run() {
                while(true) {
                    if(!sounds.isEmpty()) {
                        askToPlaySound(sounds.pop());
                    }
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        waitForSounds.start();
        setVisible(true);
    }
    // initialisiere Komponenten der UI, auf die späer an anderer Stelle zugegriffen wird.
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
        // Das Symbol sollte eigentlich anders aussehen. Wir haben es dann aber dabei belassen.
        String icon = "";
        byte[] bIcon = {(byte) 0x1F, (byte) 0xC4};
        try {
            icon = new String(bIcon, "UTF-8");
        } catch (Exception e) {
            System.out.println("Encoding Exception occurred " + e);
        }
        // Wähle Dateien zum Versenden aus.
        // Die Ausgewählten Dateien werden direkt versendet.
        fileChooser = new JFileChooser();
        fileButton = new JButton(icon);
        fileButton.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(inputField);
            System.out.println(returnVal);
            File f = fileChooser.getSelectedFile();
            inputField.setText(f.toString());
            // Wähle in Abhängigkeit von der Dateiendung die geeignete Methode zum Versenden aus.
            Pattern pdf = Pattern.compile(".*.pdf");
            Pattern wav = Pattern.compile(".*.wav");
            Matcher matcherPdf = pdf.matcher(f.toString());
            Matcher matcherWav = wav.matcher(f.toString());
            if (matcherPdf.matches() || matcherWav.matches()) {
                System.out.println("Send a pdf...");
                socketConnection.sendFile(f.toString());
            } else {
                socketConnection.sendPic(f.toString());
            }
        });

        chooseRoomButton = new JButton("Raum wählen");
    }
    // Das Layout der Haupt-Benutzeroberfläche
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
        // Implementiere Funktionalität des Buttons, zum Wählen eines Raumes
        chooseRoomButton.addActionListener(e -> {
            if (chooseRoomButton.getText().equals("Raum verlassen")) {
                // Sende Message mit type Room und leerer msg, um Raum zu verlassen
                socketConnection.sendMessage(new Message("Room", ""));
                currentRoom.clear();
                // Die Beschriftung des Buttons wird anschließend wieder angepasst
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        chooseRoomButton.setText("Raum wählen");
                    }
                });
            } else if (chatRoomList.getSelectedValue() != null){ // Prüfe, ob tatsächlich ein Raum ausgewählt ist
                String selectedChatRoom = chatRoomList.getSelectedValue();
                // In der Liste wird der Raum mit Info darüber angezeigt, wieviele Nutzer im Raum sind
                // Hier wird dieser Zusatz entfernt.
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
                // Sende Message mit type Room und Namen des Raumes, damit der Server den User zum gewählten Raum hinzufügt.
                socketConnection.sendMessage(new Message("Room", roomName));
                // Und passe die Beschriftung des Buttons an.
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        chooseRoomButton.setText("Raum verlassen");
                    }
                });
            }
        });
        JButton privateChatButton = new JButton("privater Raum");
        privateChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userList.getText().length() > 8) { // Prüfe, ob überhaut eines Userliste erzeugt werden kann
                    // Dann öffne das Fenster mit der Liste der verfügbaren Chatpartner
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
        loginButton.addActionListener(e -> openLoginWindow()); // Öffnet den Dialog zur Anmeldung am Server.
        add(loginButton, BorderLayout.NORTH);
    }
    // Layout des Login-Fensters
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
    // Diaglog zur Abfrage, ob ein empfangenes Bild angezeigt werden soll
    private void askToShowPicture(BufferedImage img) {
        JFrame dialog = new JFrame();
        dialog.setSize(400, 100);
        dialog.setLocationRelativeTo(null);
        JPanel askBtnPanel = new JPanel(new FlowLayout());
        JLabel askForPermission = new JLabel("Ein Bild wurde empfangen. Soll es angezeigt werden?");
        JButton askBtn = new JButton("Bild anzeigen?");
        // Falls Button geklickt wird, zeige das Bild in neuem Frame an.
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
    // Funktion zur Anzeige der Bilder
    public void showPicture(BufferedImage img) {
        JFrame pic = new JFrame();
        pic.getContentPane().setLayout(new FlowLayout());
        pic.getContentPane().add(new JLabel(new ImageIcon(img)));
        pic.pack();
        pic.setVisible(true);
    }

    // Diaglog zur Abfrage, ob ein empfangenes Pdf angezeigt werden soll
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

    // Funktion zur Anzeige der Pdfs 
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
    // Diaglog zur Abfrage, ob eine empfangene Sound-Datei abgespielt werden soll
    private void askToPlaySound(byte[] sound) {
          
        JFrame dialog = new JFrame();
        dialog.setSize(400, 100);
        dialog.setLocationRelativeTo(null);
        JPanel askBtnPanel = new JPanel(new FlowLayout());
        JLabel askForPermission = new JLabel("Eine Sound-Datei wurde empfangen. Soll sie abgespielt werden?");
        JButton askBtn = new JButton("Sound abspielen?");
        askBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound(sound);
            }
        });
        askBtnPanel.add(askForPermission);
        askBtnPanel.add(askBtn);

        dialog.add(askBtnPanel);
        dialog.setVisible(true);
    }
    // Funktion zum Abspielen der Sonds
    private void playSound(byte[] sound) {
        try { 
            File f = new File("../temp.wav");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(sound);
            fos.close();
            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (IOException e) {
            System.out.println("IOException occurred while trying to play sound " + e + e.getStackTrace());
        } catch (LineUnavailableException lue) {
            System.out.println("LineUnavailableException occurred while trying to play sound " + lue + lue.getStackTrace());
        } catch (UnsupportedAudioFileException use) {
            System.out.println("UnsupportedAudioException occurred while trying to play sound " + use + use.getStackTrace());
        }
    }
    // Main-Loop, die auf neuen Chat-Input wartet
    private void getChatInput(ObjectInputStream in) {
        // vor dem ersten Aufruf gibt es noch keine Privaten Nachrichten
        privateMessage[0] = "";
        while (!socketConnection.getSocket().isClosed()) {
            try {
                Object msg = in.readObject();
                // Ist msg vom Typ String, handelt es sich um eine einfache Chatnachricht. ODer um das Server-Signal zum beenden der Verbindung
                if (msg instanceof String) {
                    if (msg.equals("exit")) { // Falls einfacher String mit "exit" empfangen wird, wird der Socket geschlossen.
                        socketConnection.exit((String)msg);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            chatArea.setText(chatArea.getText() + msg + "\n");
                        }
                    });
            // ist die Empfangene msg vom Typ Message, wurde ist ein besonderes Ereignes zu Behandeln.
            // Es könnte eine Datei empfangen worden sein, ein neuer User auf dem Server sein, ein Raum erstellt oder gelöscht worden sein.
                } else if (msg instanceof Message) {
                    socketConnection.decodeMessage((Message)msg, currentRoom);
                    // Anschließend wird die Anzeige der Userliste angepasst.
                    if (currentRoom.isEmpty()) {
                        adjustUserList("");
                        chooseRoomButton.setText("Raum wählen");
                    } else {
                        adjustUserList(currentRoom.get(0));
                    }
                    // Falls eine private Nachricht empfangen wurde, verarbeite diese priavte Nachricht.
                    if (!privateMessage[0].equals("")) {
                        processPrivateChatInput();
                    }
                }
            } catch (ClassNotFoundException ce) {
                System.out.println("" + ce + ce.getStackTrace());
            } catch (IOException e) {
                System.out.println("" + e + e.getStackTrace());
            }
        }
    }
    // in Abhängigkeit, davon, ob der Nutzer sich in einem Raum befindet, wird die angezeigte Userliste dementsprechend angepasst.
    private void adjustUserList(String currentRoom) {
        if (currentRoom != null) {
            String text = userList.getText();
            // Falls User in keinem Raum ist, gibt es nichts zu tun.
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
    // Liefert Liste der User, die derzeit online sind.
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
    // Layout des Dialogs zur Eröffnung eines privaten Chats.
    private void openPrivateChatDialog(String [] userList) {
        JFrame openPrivateChatFrame = new JFrame("privaten Raum eröffnen");
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
                    userName = userName.substring(1, userName.length() - 1); // Entferne Klammern um Usernamen
                    // Aus den Namen der beteiligt User wird der Name des privaten Chats erzeugt.
                    String chatName = createPrivateChatName(socketConnection.getUserName(), userName);  
                    // Mit dem Namen wird zunächst überprüft, ob der entsprechende Chat schon existiert.
                    if (privateChats.containsKey(chatName)) { 
                        PrivateChat privateChat = privateChats.get(chatName);
                        privateChat.show = true;
                    // Falls zu dem Userpaar noch kein privater Chat existiert, wird er zur Liste der Privaten Chats hinzugefügt.
                    } else {
                        privateChats.put(chatName, new PrivateChat(new JTextArea(), true));
                    }
                    openPrivateChat(userName, chatName);
                    openPrivateChatFrame.dispose();
                }
            }
        });
        dialogPanel.add(userLabel, BorderLayout.NORTH);
        dialogPanel.add(scrollUser, BorderLayout.CENTER);
        dialogPanel.add(startChatButton, BorderLayout.SOUTH);
        openPrivateChatFrame.add(dialogPanel);
        openPrivateChatFrame.setSize(500, 300);
        openPrivateChatFrame.setVisible(true);
    }
    // Layout des Fensters zur Anzeige des priavten Chats
    private void openPrivateChat(String userName, String privateChatName) {
        PrivateChat privateChat = privateChats.get(privateChatName);
        privateChat.open = true;
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
                socketConnection.sendMessage(new Message("Private", msg)); // Message mit type Private und "" als msg ist Signal zum Eröffnen des privaten Chats
                chatInput.setText("");
            }
        });
        // Werden Bilder über den priavten Chat versendet, werden diese als privat markiert. 
        // Die urprünglichen MEthoden zum Versenden von Dateien waren auf diese Funktionalität nicht ausgelegt.
        sendFileButton.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(inputField);
            System.out.println(returnVal);
            File f = fileChooser.getSelectedFile();
            inputField.setText(f.toString());
            Pattern pdf = Pattern.compile(".*.pdf");
            Pattern png = Pattern.compile(".*.png");
            Pattern jpg = Pattern.compile(".*.jpg");
            Pattern bmp = Pattern.compile(".*.bmp");
            Matcher matcherPdf = pdf.matcher(f.toString());
            Matcher matcherPng = png.matcher(f.toString());
            Matcher matcherJpg = jpg.matcher(f.toString());
            Matcher matcherBmp = bmp.matcher(f.toString());
            if (matcherPdf.matches()) {
                String message[] = {privateChatName, f.toString()};
                socketConnection.sendPrivatePdf(new Message("PrivatePdf", message));
            } else if (matcherPng.matches()) {
                String message[] = {privateChatName, f.toString()};
                socketConnection.sendPrivatePic(new Message("PrivatePng", message));
            } else if (matcherJpg.matches()) {
                String message[] = {privateChatName, f.toString()};
                socketConnection.sendPrivatePic(new Message("PrivateJpg", message));
            } else if (matcherBmp.matches()) {
                String message[] = {privateChatName, f.toString()};
                socketConnection.sendPrivatePic(new Message("PrivateBmp", message));
            } else {
                socketConnection.sendPic(f.toString());
            }
        });
        leaveChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg[] = {privateChatName, "~//leaveRoom"};
                socketConnection.sendMessage(new Message("Private", msg)); // Message mit type Private und "~//leaveRoom" als msg ist Signal zum Verlassen des priavten Chats
                privateChat.show = false;
                privateChatFrame.dispose();
            }
        });
        // Damit das Fenster zur Anzeige des privaten Chats geschlossen werden kann ohne den Chat zu verlassen, wird in einem Feld gespeichert, ob das Fenster derzeit offen ist.
        privateChatFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                privateChat.open = false;
                System.out.println("privateChat.open: " + privateChat.open);
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                privateChat.open = false;
                System.out.println("privateChat.open: " + privateChat.open);
            }
        });
    }
    // Layout des Diaglogs, der Zustimmung zur Eröffnung eines privaten Chat erfragt
    private boolean openConsentWindow(String userName, String privateChatName) {
        PrivateChat privateChat = privateChats.get(privateChatName);
    
        SwingUtilities.invokeLater(() -> {
            JFrame consentFrame = new JFrame("Chatanfrage");
            consentFrame.setSize(300, 150);
            consentFrame.setLocationRelativeTo(null);
            consentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
            JPanel consentPanel = new JPanel(new BorderLayout());
    
            JLabel consentLabel = new JLabel("<html><center>" + userName + " möchte einen privaten Chat starten</center></html>");
            consentLabel.setHorizontalAlignment(JLabel.CENTER);
    
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            JButton consentButton = new JButton("Ja");
            JButton denyConsentButton = new JButton("Nein");
            buttonPanel.add(denyConsentButton);
            buttonPanel.add(consentButton);
    
            consentPanel.add(consentLabel, BorderLayout.CENTER);
            consentPanel.add(buttonPanel, BorderLayout.SOUTH);
            // Wählt der User "Ja", wird der priavte Chat anzeigt.
            consentButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    privateChat.show = true;
                    openPrivateChat(userName, privateChatName);
                    consentFrame.dispose();
                }
            });
            // Wählt der User nein, wird der Chat nicht angezeigt und der andere Nutzer über die Ablehnung informiert.
            denyConsentButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    privateChat.show = false;
                    String msg[] = {privateChatName, socketConnection.getUserName() + " hat die Anfrage abgelehnt"};
                    socketConnection.sendMessage(new Message("Private", msg));
                    consentFrame.dispose();
                }
            });
    
            consentFrame.add(consentPanel);
            consentFrame.setVisible(true);
        });
    
        return privateChat.show;
    }
    
    
    // Erzeuge den Namen eines privaten Chats aus den Namen der User
    // die beiden Strings werden mit compareTo() verglichen und abhängig von Ergebnis wird der Chatname erzeugt
    private String createPrivateChatName(String thisUser, String otherUser) {
       return thisUser.compareTo(otherUser) > 0 ? 
            thisUser + "\n" + otherUser : otherUser + "\n" + thisUser; 
    }

    private void processPrivateChatInput() {
        if (privateChats.containsKey(privateMessage[0])) {
            PrivateChat privateChat = privateChats.get(privateMessage[0]);
            if (privateMessage[1].equals("~//leaveRoom")) {
                privateChat.show = false;
            } else if (!privateMessage[1].equals("")) {
                privateChat.chat.setText(privateChat.chat.getText() + privateMessage[1] + "\n");
            } else if (!privateChat.show) {
                String chatPartner = getChatPartnerName(); 
                openConsentWindow(chatPartner, privateMessage[0]);
            }
            if (privateChat.show && !privateChat.open) {
                String chatPartner = getChatPartnerName(); 
                openPrivateChat(chatPartner, privateMessage[0]);
            }
        } else {
            String chatPartner = getChatPartnerName(); 
            privateChats.put(privateMessage[0], new PrivateChat(new JTextArea(), false));
            openConsentWindow(chatPartner, privateMessage[0]);
        }
        privateMessage[0] = "";
    }
    // Extrahiere den Namen des anderen Chatpartners aus dem Namen des Chats
    private String getChatPartnerName() {
            String chatPartners[] = privateMessage[0].split("\n");
            return chatPartners[0].equals(socketConnection.getUserName()) ? 
                chatPartners[1] : chatPartners[0];
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
