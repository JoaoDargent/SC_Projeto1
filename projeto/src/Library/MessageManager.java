package Library;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.cert.CertificateException;
import static Server.myServer.filesPath;

public class MessageManager {


    /***
     * talk <user> <message> - permite enviar uma mensagem privada ao utilizador user com o conteúdo message.
     * Caso o utilizador não exista, deve ser devolvido um erro.
     * @throws CertificateException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */

    public String talk(FileManager fm,UserManager um, User sender, String receiver, String message) throws IOException, CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
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

    public String read(FileManager fm, User reader) throws FileNotFoundException, KeyStoreException{
        File file = new File(filesPath +"/Users/" + reader.getId() + "/messages.txt");
       

        String mensagem = fm.readContentFromFile(file);
        
        if (mensagem.equals("")){
            return "Nao tem mensagens novas";
        }
        fm.writeContentToFile(file, "",false);
        return mensagem;
    }

   
}
