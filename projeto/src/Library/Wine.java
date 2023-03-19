package Library;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

public class Wine {

    private static ArrayList<String> stock;
    private final String name;
    private final String image;

    private int value;
    private float stars;

    public Wine(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String view(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Nome: " + this.name + "\n Classificação: " + this.stars + "\n Valor: " + getStockPrint());
        return stringBuilder.toString();
    }

    public static ArrayList<String> getStock() {
        return stock;
    }

    public String getName() {return name;}

    public float getStars() {return stars;}

    public int getValue() {
        //TODO Vai buscar o valor mais baixo em stock?
        return value;}

    public void setValue(int value) {this.value = value;}

    public void setStars(FileManager fileManager, float stars) {
        int i = 1;
        this.stars += stars/i ;
        i++;
        File classifyFile = new File("files/serverFiles/Wines/" + this.getName() + "/classify.txt");
        fileManager.writeContentToFile(classifyFile, "i:" + stars, false);


    }

    //stockprint

    public String getStockPrint(){
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : stock){
            String [] stockSplit = s.split(":");
            stringBuilder.append("O utilizador " + stockSplit[0] + " está a vender a " + stockSplit[1] + "euros " + stockSplit[2] + " garrafas\n");
        }
        return stringBuilder.toString();
    }

    public static void setStock(ArrayList<String> stk) {
       stock = stk;
    }
}
