import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerUI extends JFrame {
    private JTextArea chatArea;
    private JTextArea userList;
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

        chatroomPanel.add(new JScrollPane(chatroomTextArea), BorderLayout.CENTER);
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

        ActionListener toggleServerButtonOnClick = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (serverStateLabel.getText() == "Server State: Off") {
                    Thread serverThread = new Thread(() -> server.startServer());
                    serverThread.start();
                    serverStateLabel.setText("Server State: On");
                } else {
                    server.exitServer();
                    serverStateLabel.setText("Server State: Off");
                }
            }    
        };
    
    toggleServerButton.addActionListener(toggleServerButtonOnClick);
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerUI::new);
    }
}
