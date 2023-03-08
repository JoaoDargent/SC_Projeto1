package Library;

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
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return id + ":" + password;
    }
}
