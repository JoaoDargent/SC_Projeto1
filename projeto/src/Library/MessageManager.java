package Library;

import java.io.File;
import java.io.IOException;

import static Server.myServer.filesPath;
import static Server.myServer.usersP;

public class MessageManager {


    /***
     * talk <user> <message> - permite enviar uma mensagem privada ao utilizador user com o conteúdo message.
     * Caso o utilizador não exista, deve ser devolvido um erro.
     */

    public String talk(FileManager fm,UserManager um, User sender, String receiver, String message) throws IOException {
        if (!um.checkIfUserExists(receiver)){
            return "Utilizador para quem pretende enviar nao existe";
        }
        else{
            User Ureceiver = um.getUserById(receiver);
            File file = new File(filesPath +"/Users/" + Ureceiver.getId() + "/messages.txt");
            //Check if directory exists
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }else if (!file.exists()){
                    file.createNewFile();
            }

            fm.writeContentToFile(file, sender.getId() + " : " + message ,true);
            return "Mensagem enviada com sucesso";
        }
    }

    public String read(FileManager fm, User reader){
        File file = new File(filesPath +"/Users/" + reader.getId() + "/messages.txt");
        String mensagem = fm.readContentFromFile(file);
        if (mensagem.equals("")){
            return "Nao tem mensagens novas";
        }
        fm.writeContentToFile(file, "",false);
        return mensagem;
    }
}
