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
        loadData();
    }

    public boolean addWine(Wine wine) throws IOException {
        //Caso já exista um vinho com o mesmo nome, não é adicionado e é retornado false um erro
        /*for (Wine w : wines){
            if (w.getName().equals(wine.getName())){
                return;
            }
        }*/
        if (checkIfWineExists(wine.getName())) {
            System.out.println("O vinho indicado já existe.");
            return false;
        }
        this.wines.add(wine);

        //Criar folder do vinho
        Path path = Paths.get(winesFolder + wine.getName());
        Files.createDirectories(path);

        //Criar ficheiro de classificação
        File classify = new File(path + "/classify.txt");
        classify.createNewFile();

        //Criar ficheiro de stock
        File stock = new File(path + "/stock.txt");
        stock.createNewFile();

        // Adicionar o vinho aos dados persistentes
        try (PrintWriter writer = new PrintWriter(new FileWriter(wineDataFile, true))) {
            writer.println(wine.getName() + "," + wine.getImage());
        }
        return true;
    }

    public String viewWineByName(String name, String user){
        for (Wine w : wines){
            if (w.getName().equals(name)){
                return w.view(user);
            }
        }
        return "Não existe vinho com esse nome";
    }

    //add to stock

    public String addWineToStock(FileManager fm, User seller, String name, int value, int quantity){
        ArrayList<String> stock = Wine.getStock();
        for (String s : stock){
            String [] stockSplit = s.split(":");
            if (stockSplit[0].equals(seller.getId())){
                return ("Ja tem vinho a venda");
            }
        }
        stock.add(seller.getId() + ":" + value + ":" + quantity);
        Wine.setStock(stock);
        File stockFile = new File (winesFolder + "/" + name + "/stock.txt");
        fm.writeContentToFile(stockFile, seller.getId() + ":" + value + ":" + quantity, true);
        return "Vinho adicionado ao stock com sucesso";
    }

    //sell wine

    //Caso não existam unidades suficientes, ou o comprador não tenha
    //saldo suficiente, deverá ser devolvido e assinalado o erro correspondente.
    public String buyWine(UserManager um, String wine, String user, int quantity){
        //verificar se existe o vinho
        if (!checkIfWineExists(wine)){
            return "O vinho não existe";
        }
        //verificar se existe o utilizador
        if (!um.checkIfUserExists(user)){
            return "O utilizador não existe";
        }
        //verificar se o utilizador tem saldo suficiente
        if (um.getUserById(user).getBalance() < getWineByName(wine).getValue()){
            return "O utilizador não tem saldo suficiente";
        }

    return null;
    }

    //getwinebyname

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

    public int classifyWine(FileManager fm, String wine, int stars) {
        //Check if wine exists
        if (!checkIfWineExists(wine)){
            return -1;
        }
        //Check if rating is between 1 and 5
        if (stars < 1 || stars > 5){
            return 0;
        }
        //Classify wine
        Wine wineToClassify = getWineByName(wine);
        wineToClassify.setStars(fm,stars);
        File classifyFile = new File(winesFolder + "/" + wine + "/classify.txt");
        fm.writeContentToFile(classifyFile, stars + "", false);
        return 1;
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
