import java.io.Serializable;

public class Message implements Serializable {
    
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
}