package Library;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static Library.WineManager.winesFolder;

public class Wine {

    private ArrayList<String> stock;
    //seller:price:quantity
    private final String name;
    private final String image;

    private int value;
    private float stars;
    private String seller;

    public Wine(String name, String image) {
        this.name = name;
        this.image = image;
        this.stock = new ArrayList<String>();
        this.stars = 0;
    }

    public String view(String user) throws ClassNotFoundException, IOException{
        StringBuilder stringBuilder = new StringBuilder();

        if (getStockPrint().equals("")) {
            stringBuilder.append("Nome: " + this.name + "\nImagem: enviada e guardada em files/clientFiles/" + user
                                + "\nClassificação média: " + this.stars + "\n" + "Não existem vinhos a venda");
        } else{

            stringBuilder.append("Nome: " + this.name + "\nImagem: enviada e guardada em files/clientFiles/" + user
                    + "\nClassificação média: " + this.stars + "\n" + getStockPrint());

        }
        return stringBuilder.toString();
    }

    public ArrayList<String> getStock() {
        return stock;
    }

    public String getName() {return name;}

    public String getImage() {return image;}

    public float getStars() {return stars;}

    public int getValue(String seller) {
        //seller:price:quantity
        for (String s : stock){
            String [] stockSplit = s.split(":");
            if (stockSplit[0].equals(seller)){
                //valor por garrafa
                pricePerBottle += (double) priceForAll / quantity;
            }
        }
        return 0;
    }

    public void setValue(int value) {this.value = value;}

    private int i = 1;
    //private double averageStars = 0.0;
    public void setStars(int stars) {
        this.stars = (this.stars * (i - 1) + stars) / i;
        i++;
    }

    public String getStockPrint(){
        StringBuilder stringBuilder = new StringBuilder();
        if (stock.isEmpty())
            return "";
        for (String s : stock){
            String [] stockSplit = s.split(":");
            this.seller = stockSplit[0];
            stringBuilder.append("O utilizador " + stockSplit[0] + " está a vender a " + stockSplit[1] + " euros " + stockSplit[2] + " garrafas\n");
        }
        return stringBuilder.toString();
    }

    public String getSeller() {return this.seller;}

    public void setStock(ArrayList<String> stk) {
       this.stock = stk;
    }

    public int getQuantity(String userSeller) {
        for (String s : stock){
            String [] stockSplit = s.split(":");
            if (stockSplit[0].equals(userSeller)){
                return Integer.parseInt(stockSplit[2]);
            }
        }
        return 0;
    }

    public void setQuantity(FileManager fileManager, String userSeller, int newQuantity) {
        //Function has to change the arrayList stock and replace the line in stock.txt
        //If the quantity is 0, the line has to be deleted

        if (newQuantity == 0){
            for (String s : stock){
                String [] stockSplit = s.split(":");
                if (stockSplit[0].equals(userSeller)){
                    stock.remove(s);
                }
            }
        } else {
            for (String s : stock){
                String [] stockSplit = s.split(":");
                if (stockSplit[0].equals(userSeller)){
                    stock.remove(s);
                    stock.add(stockSplit[0] + ":" + stockSplit[1] + ":" + newQuantity);
                }
            }
        }

        File stockFile = new File (winesFolder + "/" + name + "/stock.txt");
        //delete the content of the file stockFile

        fileManager.writeContentToFile(stockFile, "", false);
        for (String s : stock) {
            fileManager.writeContentToFile(stockFile, s, true);
        }
    }
}