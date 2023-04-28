package Library;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;


public class WineManager {

    FileManager fileManager = new FileManager();
    public final static String winesFolder = "../files/serverFiles/Wines/";
    private final static String wineDataFile = "../files/serverFiles/wineData.txt";
    private ArrayList<Wine> wines;

    public WineManager() {
        this.wines = new ArrayList<>();
        //loadData();
    }

    public boolean addWine(Wine wine) throws IOException {
        if (checkIfWineExists(wine.getName())) {
            System.out.println("O vinho indicado já existe.");
            return false;
        }
        this.wines.add(wine);
        
        Path path = Paths.get(winesFolder + wine.getName());
        File wineFile =  new File(winesFolder + wine.getName());
        if (!wineFile.exists()){
            Files.createDirectories(path);
        }
        
        //Criar ficheiro de classificação
        File classify = new File(winesFolder + wine.getName() +  "/classify.txt");
        classify.createNewFile();

        //Criar ficheiro de stock
        File stock = new File(winesFolder + wine.getName() + "/stock.txt");
        stock.createNewFile();

        // Adicionar o vinho aos dados persistentes
        try (PrintWriter writer = new PrintWriter(new FileWriter(wineDataFile, true))) {
            writer.println(wine.getName() + "," + wine.getImage());
        }
        return true;
    }

    public String viewWineByName(String name, String user) throws ClassNotFoundException, IOException{
        for (Wine w : wines){
            if (w.getName().equals(name)){
                return w.view(user);
            }
        }
        return "Não existe vinho com esse nome";
    }

    //add to stock

    public String addWineToStock(FileManager fm, User seller, String name, int value, int quantity){
        Wine wine = getWineByName(name);
        ArrayList<String> stock = wine.getStock();
        for (String s : stock){
            String [] stockSplit = s.split(":");
            if (stockSplit[0].equals(seller.getId())){
                return ("Ja tem vinho a venda");
            }
        }
        stock.add(seller.getId() + ":" + value + ":" + quantity);
        wine.setStock(stock);
        File stockFile = new File (winesFolder + "/" + name + "/stock.txt");
        fm.writeContentToFile(stockFile, seller.getId() + ":" + value + ":" + quantity, true);
        return "Vinho adicionado ao stock com sucesso";
    }
    public String addWineToStockOnLoad(String seller, Wine wine, int value, int quantity){
        ArrayList<String> stock = wine.getStock();
        stock.add(seller + ":" + value + ":" + quantity);
        wine.setStock(stock);
        return "Vinho adicionado ao stock com sucesso";
    }

    //sell wine

    //Caso não existam unidades suficientes, ou o comprador não tenha
    //saldo suficiente, deverá ser devolvido e assinalado o erro correspondente.
    public String buyWine(UserManager um, String wine, String userSeller, String userBuyer, int quantity) throws IOException {
        Wine wineObj = getWineByName(wine);
        //verificar se existe o vinho
        if (!checkIfWineExists(wine)){
            return "O vinho não existe";
        }
        //verificar se existe o utilizador seller
        if (!um.checkIfUserExists(userSeller)){
            return "O utilizador não existe";
        }

        //verifica se o seller tem o vinho no stock
        if (!checkIfSellerExists(wineObj, userSeller)){
            return "Esse utilizador não tem o vinho à venda no stock";
        }
        //verificar se o utilizador tem saldo suficiente
        if (um.getUserById(userBuyer).getBalance() < getWineByName(wine).getValue(userSeller)){
            return "O utilizador não tem saldo suficiente";
        }

        //verificar se o seller tem unidades suficientes
        if (getWineByName(wine).getQuantity(userSeller) < quantity){
            return "O utilizador não tem unidades suficientes";
        }

        // efetuar a compra
        //atualiza balance do comprador e do vendedor
        User buyer = um.getUserById(userBuyer);
        User seller = um.getUserById(userSeller);

        double buyerbalance = buyer.getBalance();
        double sellerbalance = seller.getBalance();

        double transacao = wineObj.getValue(userSeller)*quantity;

        buyer.setBalance(fileManager, buyerbalance - transacao);
        seller.setBalance(fileManager, sellerbalance + transacao);

        //atualiza stock do vendedor
        getWineByName(wine).setQuantity(fileManager, userSeller, getWineByName(wine).getQuantity(userSeller) - quantity);
        return "Compra efetuada com sucesso";
    }
    public Wine getWineByName(String name){
        for (Wine w : wines){
            if (w.getName().equals(name)){
                return w;
            }
        }
        return null;
    }

    //check if wine exists

    public boolean checkIfWineExists(String name){
        for (Wine w : wines){
            if (w.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    public boolean checkIfSellerExists(Wine wine,String seller){
        ArrayList<String> stock = wine.getStock();
        for (String s : stock){
            String [] stockSplit = s.split(":");
            if (stockSplit[0].equals(seller)){
                return true;
            }
        }
        return false;
    }

    public String classifyWine(FileManager fm, String wine, int stars) {
        //Check if wine exists
        if (!checkIfWineExists(wine)){
            return "Vinho não existe.";
        }
        //Check if rating is between 1 and 5
        if (stars < 1 || stars > 5){
            return "A classificação tem que ser entre 1 e 5.";
        }
        //Classify wine
        Wine wineToClassify = getWineByName(wine);
        wineToClassify.setStars(stars);
        File classifyFile = new File(winesFolder + "/" + wine + "/classify.txt");
        fm.writeContentToFile(classifyFile, stars + "", true);
        return "Vinho classificado com sucesso!";
    }

    private void loadData() {
        try (Scanner scanner = new Scanner(new File(wineDataFile))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split(",");
                if (tokens.length == 2) {
                    Wine wine = new Wine(tokens[0], tokens[1]);
                    wines.add(wine);
                }
            }
        } catch (FileNotFoundException e) {
            // Ignore if the file does not exist yet
        }
    }
}
