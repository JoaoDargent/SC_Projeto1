package Library;

import java.io.*;

import static Server.myServer.filesPath;

public class User {
    private String id;
    private String pathToPublicKey;

    private int balance;

    public User(String id, String pathToPublicKey) {
        this.id = id;
        this.pathToPublicKey = pathToPublicKey;
    }

    public String getId() { return id; }
    public String getPathToPublicKey() { return pathToPublicKey; }
    public int getBalance() { return balance; }

    public void setBalance(FileManager fileManager, int balance) throws IOException {
        this.balance = balance;
        saveBalance(fileManager);
    }

    @Override
    public String toString() { return id + ":" + pathToPublicKey; }

    private void saveBalance(FileManager fileManager) throws IOException {
        File balanceFile = new File (filesPath + "/Users/" + id + "/balance.txt");
        if (!balanceFile.getParentFile().exists()){
            balanceFile.getParentFile().mkdirs();
            balanceFile.createNewFile();
        }else if (!balanceFile.exists()){
            balanceFile.createNewFile();
        }
        fileManager.writeContentToFile(balanceFile, Integer.toString(balance),false);
    }

    public void loadBalance(String id, FileManager fileManager) throws IOException {
        File balanceFile = new File (filesPath + "/Users/" + id + "/balance.txt");
        //check if file exists
        if (!balanceFile.exists()){
          setBalance(fileManager,200);
        }
        String filebalance = fileManager.readContentFromFile(balanceFile);
        if (filebalance.equals("")){
            setBalance(fileManager,200);
        }
        else setBalance(fileManager,Integer.parseInt(filebalance));
    }
}
