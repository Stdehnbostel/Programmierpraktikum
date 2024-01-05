import java.util.ArrayList;

public class Room {

    private String name;
    private ArrayList<ServerThread> users;

    public Room(String name) {
        this.name = name;
        this.users = new ArrayList<ServerThread>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addUser(ServerThread user) {
        this.users.add(user);
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<ServerThread> getUserList() {
        return this.users;
    }
}