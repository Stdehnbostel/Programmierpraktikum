import javax.swing.JTextArea;

public class PrivateChat {

    public JTextArea chat;
    public boolean show;

    PrivateChat(JTextArea chat, boolean show) {
        this.chat = chat;
        this.show = show;
    }
}