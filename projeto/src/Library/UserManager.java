package Library;

import java.util.ArrayList;

public class UserManager {
    private ArrayList<User> users;

    public UserManager() {
        this.users = new ArrayList<>();
    }

    public void addUser(User user) { this.users.add(user); }

    public User getUserById(String id){
        for (User u : users){
            if (u.getId().equals(id)){
                return u;
            }
        }
        return null;
    }

    public boolean checkIfUserExists(String id){
        for (User u : users){
            if (u.getId().equals(id)){
                return true;
            }
        }
        return false;
    }
}