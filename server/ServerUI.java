import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerUI extends JFrame {
    private JTextArea chatArea;
    private JTextArea userList;
    private JTextArea roomList;
    private JTextField inputField;

    public ServerUI() {
        setTitle("Server Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();

        setVisible(true);
    }

    private void initComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);

        userList = new JTextArea();
        userList.setEditable(false);
        userList.setBackground(getForeground());
        
        roomList = new JTextArea();
        roomList.setEditable(false);
        roomList.setBackground(getForeground());

        inputField = new JTextField();
    }

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

    private void openModerationWindow() {
        JFrame moderationFrame = new JFrame("Server Moderation");
        moderationFrame.setSize(700, 500);
        moderationFrame.setLocationRelativeTo(null);
    
        JSplitPane splitPaneVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane splitPaneHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    
        JPanel chatroomPanel = new JPanel(new BorderLayout());
        chatroomPanel.add(new JLabel("Chatrooms"), BorderLayout.NORTH);
        JTextArea chatroomTextArea = new JTextArea();
        JTextField newChatroomField = new JTextField();
        JButton createChatroomButton = new JButton("Create Chatroom");
        JButton deleteChatroomButton = new JButton("Delete Chatroom");

        JPanel chatroomButtonsPanel = new JPanel(new GridLayout(1, 2));
        JPanel chatroomInputFieldsPanel = new JPanel(new GridLayout(1, 2));

        chatroomButtonsPanel.add(createChatroomButton);
        chatroomButtonsPanel.add(deleteChatroomButton);
        chatroomInputFieldsPanel.add(newChatroomField);
        chatroomInputFieldsPanel.add(chatroomButtonsPanel);

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
                String roomName = newChatroomField.getText();
                if (!roomName.equals("") && !server.isRoom(roomName)) {
                    Room room = new Room(roomName);
                    server.addRoom(room);   
                } // else { implement error warning }
            }
        };

        ActionListener deleteRoom = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.deleteRoom(newChatroomField.getText());
                chatroomTextArea.setText(server.getRoomList());
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
    
        toggleServerButton.addActionListener(toggleServerButtonOnClick);
        createChatroomButton.addActionListener(createRoom);
        deleteChatroomButton.addActionListener(deleteRoom);
        warnButton.addActionListener(showWarnUserWindow);
    }

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
    
    private void updateRoomList(RunServer server) {
        Thread roomListThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            roomList.setText(server.getRoomList());
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
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerUI::new);
    }
}
