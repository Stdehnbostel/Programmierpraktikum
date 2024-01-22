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

    public boolean addUser(ServerThread user) {
        if (!users.contains(user)) {
            return this.users.add(user);
        }
        return false;
    }

    public void removeUser(ServerThread user) {
        System.out.println("trying to remove user...");
        users.removeIf(u -> u.equals(user));
    }
    
    public void removeUser(String userName) {
        System.out.println("trying to remove user...");
        users.removeIf(u -> u.userName.equals(userName));
    }

    public void removeAllUsers() {
        users.clear();
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<ServerThread> getUserList() {
        return this.users;
    }

    public int size() {
        return users.size();
    }

    @Override 
    public boolean equals(Object o) {
        if (o instanceof Room && ((Room)o).getName().equals(this.name)) {
            return true;
        }
        return false;
    }
}