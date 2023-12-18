import java.io.Serializable;

public class UserList implements Serializable {
    
    public String type;
    public Object msg;

    UserList(String type, Object msg) {
        this.type = type;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return this.msg.toString();
    }
}