package Library;

import java.util.ArrayList;

public class UserManager {
    private ArrayList<User> users;

    public UserManager() {
        this.users = new ArrayList<>();
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public String loginManager(User user){
        for (User u : users){
            if (u.getId().equals(user.getId())){
                if (u.getPassword().equals(user.getPassword())){
                    return "Autenticado com sucesso";
                }
                else{
                    return "Password errada";
                }
            }
        }
        return "Registado com sucesso";
    }
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
