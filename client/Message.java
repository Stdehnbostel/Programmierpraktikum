import java.io.Serializable;

public class Message implements Serializable {
    // Message-Klasse dient zum Verpacken von Nachrichten, die mehr als nur einen String enthalten
   // jede Message hat einen type, der ihre Funktion beschreibt und ein Feld msg für den eigentlichen Inhalt. 
    public String type;
    public Object msg;

    Message(String type, Object msg) {
        this.type = type;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return this.msg.toString();
    }

    public String[] toStringArray() {
        if (msg instanceof String[]) {
            return (String[]) msg;
        }
        else {
            throw new UnsupportedOperationException("msg is not a String[]");
        }
    }
}