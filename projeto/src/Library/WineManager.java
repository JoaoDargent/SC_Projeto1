package Library;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class WineManager {

    FileManager fileManager = new FileManager();
    public final static String winesFolder = "../files/serverFiles/Wines/";
    private ArrayList<Wine> wines;

    public WineManager() {
        this.wines = new ArrayList<>();
    }

    public void addWine(Wine wine) throws IOException {
        //Caso já exista um vinho com o mesmo nome, não é adicionado e é retornado false um erro
        for (Wine w : wines){
            if (w.getName().equals(wine.getName())){
                return;
            }
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
    }

    public String viewWineByName(String name){
        for (Wine w : wines){
            if (w.getName().equals(name)){
                return w.view();
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

    public String classifyWine(FileManager fm, String wine, int stars) {
        //Check if wine exists
        if (!checkIfWineExists(wine)){
            return "O vinho não existe";
        }
        //Check if rating is between 1 and 5
        if (stars < 1 || stars > 5){
            return "A classificação tem de estar entre 1 e 5";
        }
        //Classify wine
        Wine wineToClassify = getWineByName(wine);
        wineToClassify.setStars(fileManager,stars);
        File classifyFile = new File(winesFolder + "/" + wine + "/classify.txt");
        fileManager.writeContentToFile(classifyFile, stars + "", false);
        return "Classificação adicionada com sucesso";
    }
}
