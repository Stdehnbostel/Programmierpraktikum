import javax.swing.JTextArea;

public class PrivateChat {
    // Klasse zur Verwaltung der privaten Chats.
    // Bevor der Empfänger einer priavten Chatanfrage dem Chat zugestimmt hat, können Nachrichten gespeichert werden,
    // Sobald dem Chat zugestimmt wird, wird das Feld "show" auf true gesetzt und die Nachrichten werden angeziegt.
    // Das Feld "open" Speichert, ob das Chatfenster aktuell geöffnet ist.
    public JTextArea chat;
    public boolean show;
    public boolean open;

    PrivateChat(JTextArea chat, boolean show) {
        this.chat = chat;
        this.show = show;
        this.open = false;
    }
}