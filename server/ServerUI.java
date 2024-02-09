import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

// Main class representing the Server UI
public class ServerUI extends JFrame {
    private JTextArea chatArea;
    private JTextArea userList;
    private JList<String> roomList;
    private JTextField inputField;

    // Constructor for initializing the Server UI
    public ServerUI() {
        setTitle("Server Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();

        setVisible(true);
    }

    // Method to initialize GUI components
    private void initComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);

        userList = new JTextArea();
        userList.setEditable(false);
        userList.setBackground(getForeground());
        
        String[] rooms = {};
        roomList = new JList<>(rooms);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        inputField = new JTextField();
    }

    // Method to layout GUI components
    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JLabel("Chat"), BorderLayout.NORTH);
        JScrollPane scrollChat = new JScrollPane(chatArea);
        chatPanel.add(scrollChat, BorderLayout.CENTER);
        chatPanel.add(inputField, BorderLayout.SOUTH);

        JPanel userListPanel = new JPanel(new BorderLayout());
        userListPanel.add(new JLabel("User List"), BorderLayout.NORTH);
        JScrollPane scrollUsers = new JScrollPane(userList);
        userListPanel.add(scrollUsers, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPanel, userListPanel);
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);

        JButton serverModerationButton = new JButton("Server Moderation");
        serverModerationButton.addActionListener(e -> openModerationWindow());
        add(serverModerationButton, BorderLayout.NORTH);
    }

    // Method to open the moderation window
    private void openModerationWindow() {
        JFrame moderationFrame = new JFrame("Server Moderation");
        moderationFrame.setSize(700, 500);
        moderationFrame.setLocationRelativeTo(null);
    
        JSplitPane splitPaneVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane splitPaneHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    
        JPanel chatroomPanel = new JPanel(new BorderLayout());
        chatroomPanel.add(new JLabel("Chatrooms"), BorderLayout.NORTH);
        JList<String> chatroomList = new JList<String>();
        JTextField newChatroomField = new JTextField();
        JButton createChatroomButton = new JButton("Create Chatroom");
        JButton deleteChatroomButton = new JButton("Delete Chatroom");

        JPanel chatroomButtonsPanel = new JPanel(new GridLayout(1, 2));
        JPanel chatroomInputFieldsPanel = new JPanel(new GridLayout(1, 2));

        chatroomButtonsPanel.add(createChatroomButton);
        chatroomButtonsPanel.add(deleteChatroomButton);
        chatroomInputFieldsPanel.add(newChatroomField);
        chatroomInputFieldsPanel.add(chatroomButtonsPanel);
        roomList.addListSelectionListener(e -> {
            String selectedChatRoom = roomList.getSelectedValue();
            if (selectedChatRoom == "null") {
                createChatroomButton.setText("Create Chatroom");
            } else {
                createChatroomButton.setText("Edit Chatroom");
            }
        });

        chatroomPanel.add(new JScrollPane(roomList), BorderLayout.CENTER);
        chatroomPanel.add(chatroomInputFieldsPanel, BorderLayout.SOUTH);
    
        JPanel moderationPanel = new JPanel(new BorderLayout());
        moderationPanel.add(new JLabel("Moderation"), BorderLayout.NORTH);
        JTextArea userModerationTextArea = new JTextArea();
        JTextField userField = new JTextField();
        JButton warnButton = new JButton("Warn User");
        JButton banButton = new JButton("Ban User");
        
        JPanel moderationButtonsPanel = new JPanel(new GridLayout(1, 2));
        JPanel moderationInputFieldsPanel = new JPanel(new GridLayout(1, 2));
    
        moderationButtonsPanel.add(warnButton);
        moderationButtonsPanel.add(banButton);

        moderationInputFieldsPanel.add(userField);
        moderationInputFieldsPanel.add(moderationButtonsPanel);
        
        moderationPanel.add(new JScrollPane(userModerationTextArea), BorderLayout.CENTER);
        moderationPanel.add(moderationInputFieldsPanel, BorderLayout.SOUTH);

    
        JPanel serverStatePanel = new JPanel(new BorderLayout());
        JButton toggleServerButton = new JButton("Turn Server On/Off");
        JLabel serverStateLabel = new JLabel("Server State: Off");
        
        serverStatePanel.add(serverStateLabel, BorderLayout.NORTH);
        serverStatePanel.add(toggleServerButton, BorderLayout.SOUTH);
    
        splitPaneVertical.setTopComponent(splitPaneHorizontal);
        splitPaneVertical.setBottomComponent(serverStatePanel);
        splitPaneHorizontal.setLeftComponent(chatroomPanel);
        splitPaneHorizontal.setRightComponent(moderationPanel);
    
        splitPaneVertical.setDividerLocation(350);
        splitPaneHorizontal.setDividerLocation(350);
    
        moderationFrame.add(splitPaneVertical);
        moderationFrame.setVisible(true);

        RunServer server = new RunServer(chatArea);
        updateRoomList(server); 

        Thread showUserList = new Thread() {
            @Override
            public void run() {
                while (serverStateLabel.getText().equals("Server State: On")) {
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            userList.setText(server.getUserList());
                        }
                    });
                    try{
                        sleep(50);
                    } catch (InterruptedException e) {
                            System.out.println("Interrupted Exception orrured in ServerUI: " + e + e.getStackTrace());
                    }
                }

            }
        };

        ActionListener toggleServerButtonOnClick = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (serverStateLabel.getText() == "Server State: Off") {
                    Thread serverThread = new Thread(() -> server.startServer());
                    serverThread.start();
                    serverStateLabel.setText("Server State: On");
                    showUserList.start();
                } else {
                    server.exitServer();
                    serverStateLabel.setText("Server State: Off");
                    userList.setText("");
                }
            }    
        };

        ActionListener createRoom = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (createChatroomButton.getText() ==  "Create Chatroom") {
                    String roomName = newChatroomField.getText();
                    if (!roomName.equals("") && !server.isRoom(roomName)) {
                        Room room = new Room(roomName);
                        server.addRoom(room);   
                    } // else { implement error warning }
                } else {
                    String selectedChatRoom = roomList.getSelectedValue();
                    ArrayList<String> breakDownRoomName = new ArrayList<String>(Arrays.asList(selectedChatRoom.split(" "))); 
                    breakDownRoomName.remove(breakDownRoomName.size() - 1);
                    breakDownRoomName.remove(breakDownRoomName.size() - 1);
                    selectedChatRoom = "";
                    for (String s: breakDownRoomName) {
                        selectedChatRoom += s;
                    }
                    final String roomName = selectedChatRoom;  
                    server.renameRoom(roomName, newChatroomField.getText());
                    roomList.setListData(server.getRoomList());
                    createChatroomButton.setText("Create Chatroom");

                }
                
            }
        };

        ActionListener deleteRoom = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedChatRoom = roomList.getSelectedValue();
                ArrayList<String> breakDownRoomName = new ArrayList<String>(Arrays.asList(selectedChatRoom.split(" "))); 
                breakDownRoomName.remove(breakDownRoomName.size() - 1);
                breakDownRoomName.remove(breakDownRoomName.size() - 1);
                selectedChatRoom = "";
                for (String s: breakDownRoomName) {
                    selectedChatRoom += s;
                }
                final String roomName = selectedChatRoom;  
                server.deleteRoom(roomName);
                roomList.setListData(server.getRoomList());
                createChatroomButton.setText("Create Chatroom");
            }
        };

        ActionListener showWarnUserWindow = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (serverStateLabel.getText().equals("Server State: On")) {
                    ServerThread user = server.getClient(userField.getText());
                    if (user == null) {
                        userModerationTextArea.setText(userModerationTextArea.getText() + "User not found\n");
                        return;
                    }
                    warnUser(server, user, userModerationTextArea);    
                }
            }
        };

        ActionListener showBanUserWindow = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (serverStateLabel.getText().equals("Server State: On")) {
                    ServerThread user = server.getClient(userField.getText());
                    if (user == null) {
                        userModerationTextArea.setText(userModerationTextArea.getText() + "User not found\n");
                        return;
                    }
                    banUser(server, user, userModerationTextArea);    
                }
            }
        };
    
        toggleServerButton.addActionListener(toggleServerButtonOnClick);
        createChatroomButton.addActionListener(createRoom);
        deleteChatroomButton.addActionListener(deleteRoom);
        warnButton.addActionListener(showWarnUserWindow);
        banButton.addActionListener(showBanUserWindow);
    }

    // Method to open a "warn user" moderation panel (warned user will receive a warning text)
    private void warnUser(RunServer server, ServerThread user, JTextArea userModeration) {
        JFrame warnFrame = new JFrame("Warn: " + user.userName);
        warnFrame.setSize(400, 100);
        warnFrame.setLocationRelativeTo(null);
        JPanel warnPanel= new JPanel(new GridLayout(2, 1));
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("Message");
        JTextArea message = new JTextArea();
        dialogPanel.add(messageLabel, BorderLayout.WEST);
        dialogPanel.add(message, BorderLayout.CENTER);
        JButton askBtn = new JButton("send warning");
        askBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.sendToUser(user, "[VERWARNUNG]: " + message.getText());
                userModeration.setText(user.userName + "[WARNED]: " + message.getText());
                warnFrame.dispose();
            }
        });
        warnPanel.add(dialogPanel);
        warnPanel.add(askBtn);

        warnFrame.add(warnPanel);
        warnFrame.setVisible(true);
    }

    // Method to open a "ban user" moderation panel
    // If the user in question is already banned, he can be unbanned/pardon'ed 
    private void banUser(RunServer server, ServerThread user, JTextArea userModeration) {
        JFrame banFrame = new JFrame("ban: " + user.userName);
        banFrame.setSize(400, 100);
        banFrame.setLocationRelativeTo(null);
        JPanel banPanel= new JPanel(new GridLayout(2, 1));
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("Message");
        JTextArea message = new JTextArea();
        dialogPanel.add(messageLabel, BorderLayout.WEST);
        dialogPanel.add(message, BorderLayout.CENTER);
        JButton askBtn = new JButton();
        if (user.getBanStatus() == false) {
            askBtn = new JButton("ban user");
            askBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.sendToUser(user, "[VERBANNUNG]: " + message.getText());
                    userModeration.setText(user.userName + "[BANNED]: " + message.getText());
                    banFrame.dispose();
                    user.setBanStatus(true);
                    server.sendToUser(user, "exit");
                    user.logout();
            }
        });
        } else {
            askBtn = new JButton("pardon ban");
            askBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.sendToUser(user, "[ENTBANNUNG]: " + message.getText());
                    userModeration.setText(user.userName + "[PARDON]: " + message.getText());
                    banFrame.dispose();
                    user.setBanStatus(false);
            }
        });
        }
        
        banPanel.add(dialogPanel);
        banPanel.add(askBtn);

        banFrame.add(banPanel);
        banFrame.setVisible(true);
    }
    
    // Updates the room list
    private void updateRoomList(RunServer server) {
        Thread roomListThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            int selectedIndex = roomList.getSelectedIndex();
                            roomList.setListData(server.getRoomList());
                            roomList.setSelectedIndex(selectedIndex);
                        }
                    });
                    try{
                        sleep(50);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted Exception orrured in ServerUI: " + e + e.getStackTrace());
                    }
                }
            }
        }; 
        roomListThread.start();
    }
    

    // Main method to start the Server UI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerUI::new);
    }
}
