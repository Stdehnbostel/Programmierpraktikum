import javax.swing.JTextArea;

public class PrivateChat {

    public JTextArea chat;
    public boolean show;
    public boolean open;

    PrivateChat(JTextArea chat, boolean show) {
        this.chat = chat;
        this.show = show;
        this.open = false;
    }
}