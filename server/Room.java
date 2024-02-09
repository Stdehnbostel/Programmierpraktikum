import java.util.ArrayList;

public class Room {
    // Einfache Raum-Klasse, mit jeweils einem Raumname und einer Liste der User, die gerade in dem Raum sind.
    // Enthält verschiedene Methoden, wobei nicht alle in dem jetzigen Build eine konkrete Verwendung haben,
    // jedoch diese unbenutzten methoden eine leichte implementierung von weiteren features ermöglicht. 

    private String name;
    private ArrayList<ServerThread> users;

    public Room(String name) {
        this.name = name;
        this.users = new ArrayList<ServerThread>();
    }

    // Raumname ändern
    public void setName(String name) {
        this.name = name;
    }

    // User in den raum hinzufügen
    public boolean addUser(ServerThread user) {
        if (!users.contains(user)) {
            return this.users.add(user);
        }
        return false;
    }

    // User aus dem Raum entfernen (möglich mittels Username String oder konkretem ServerThread dank Überladung)
    public void removeUser(ServerThread user) {
        System.out.println("trying to remove user...");
        users.removeIf(u -> u.equals(user));
    }
    
    public void removeUser(String userName) {
        System.out.println("trying to remove user...");
        users.removeIf(u -> u.userName.equals(userName));
    }

    // entfernt alle User aus dem Raum
    public void removeAllUsers() {
        users.clear();
    }

    // getter-funktionen:
    public String getName() {
        return this.name;
    }

    public ArrayList<ServerThread> getUserList() {
        return this.users;
    }

    public int size() {
        return users.size();
    }
     
    // Überprüft, ob das übergebene Objekt mit dem aktuellen Raumobjekt übereinstimmt, indem es den Raumnamen vergleicht.
    @Override 
    public boolean equals(Object o) {
        if (o instanceof Room && ((Room)o).getName().equals(this.name)) {
            return true;
        }
        return false;
    }
}