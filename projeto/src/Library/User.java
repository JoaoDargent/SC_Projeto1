package Library;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class User {
    private String id;
    private String password;

    private int balance;

    public User(String id, String password) {
        this.id = id;
        this.password = password;
    }


    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public int getBalance() {
        loadBalance();
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
        saveBalance();
    }

    @Override
    public String toString() {
        return id + ":" + password;
    }

    private void saveBalance() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("user_" + id + ".dat"));
            oos.writeInt(balance);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBalance() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("user_" + id + ".dat"));
            balance = ois.readInt();
            ois.close();
        } catch (IOException e) {
            // If there's no file for this user, ignore the error
        }
    }
}
