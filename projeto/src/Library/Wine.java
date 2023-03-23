package Library;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Wine {

    private static ArrayList<String> stock;
    private final String name;
    private final String image;

    private int value;
    private float stars;
    private String seller;

    public Wine(String name, String image) {
        this.name = name;
        this.image = image;
        this.stock = new ArrayList<String>();
    }

    public String view(String user){
        StringBuilder stringBuilder = new StringBuilder();

        Path source = Paths.get("../files/serverFiles/Wines/" + name + "/" + name + ".jpg");
        Path destination = Paths.get("../files/clientFiles/" + user + "/" + name + ".jpg");

        try {
            Files.copy(source, destination);
            System.out.println("File copied successfully!");
        } catch (IOException e) {
            if(Files.exists(destination)){
                System.out.println(name + "'s image is already in files/clientFiles/" + user + "/");
            } else {
                System.out.println("Error copying file: " + e.getMessage());
            }
            
        }

        stringBuilder.append("Nome: " + this.name + "\nImagem: enviada e guardada em files/clientFiles/" + user 
                                + "\nClassificação média: " + this.stars + "\n" + getStockPrint());
        return stringBuilder.toString();
    }

    public static ArrayList<String> getStock() {
        return stock;
    }

    public String getName() {return name;}

    public String getImage() {return image;}

    public float getStars() {return stars;}

    public int getValue() {
        //TODO Vai buscar o valor mais baixo em stock?
        return value;}

    public void setValue(int value) {this.value = value;}

    public void setStars(FileManager fileManager, float stars) {
        int i = 1;
        this.stars += stars/i ;
        i++;
        File classifyFile = new File("../files/serverFiles/Wines/" + this.getName() + "/classify.txt");
        fileManager.writeContentToFile(classifyFile, "i:" + stars, false);
    }

    //stockprint

    public String getStockPrint(){
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : stock){
            String [] stockSplit = s.split(":");
            this.seller = stockSplit[0];
            stringBuilder.append("O utilizador " + stockSplit[0] + " está a vender a " + stockSplit[1] + " euros " + stockSplit[2] + " garrafas\n");
        }
        return stringBuilder.toString();
    }

    public String getSeller() {return this.seller;}

    public static void setStock(ArrayList<String> stk) {
       stock = stk;
    }
}
