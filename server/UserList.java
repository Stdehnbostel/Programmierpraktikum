import java.io.Serializable;

public class UserList implements Serializable {
    public String msg;

    UserList(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return this.msg;
    }
}