package Library;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public String talk(FileManager fm,UserManager um, User sender, String receiver, byte[] encryptedMessage) throws IOException, CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (!um.checkIfUserExists(receiver)){
            return "Utilizador para quem pretende enviar nao existe";
        } else {
            User Ureceiver = um.getUserById(receiver);

            File file = new File(filesPath + "/Users/" + Ureceiver.getId() + "/messages.txt");
            //Check if directory exists
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else if (!file.exists()) {
                file.createNewFile();
            }

            /*Object sndr = new Object();
            sndr = sender.getId();
            Object encmsg = new Object();
            encmsg = encryptedMessage;

            FileOutputStream fout = new FileOutputStream(file, true);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(sndr);
            out.writeObject(encmsg);
            out.flush();
            out.close();
            fout.close();*/
            fm.writeContentToFile(file, sender.getId() + " : " + Arrays.toString(encryptedMessage) ,true);
            return "Mensagem enviada com sucesso";
        }
    }

    public String read(FileManager fm, User reader) throws IOException, KeyStoreException, ClassNotFoundException {
        File file = new File(filesPath +"/Users/" + reader.getId() + "/messages.txt");

        String mensagem = fm.readContentFromFile(file);

        if (mensagem.equals("")){
            return "Nao tem mensagens novas";
        }
        fm.writeContentToFile(file, "",false);
        return mensagem;

        /*
        List<Object[]> messages = new ArrayList<>();

        if (!file.exists() || file.length() == 0) {
            System.out.println("No new messages");
            return messages;
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(fis)) {

            while (true) {
                try {
                    Object[] linha = new Object[2];
                    linha[0] = in.readObject();
                    linha[1] = in.readObject();
                    messages.add(linha);
                } catch (EOFException e) {
                    break;
                }
            }
        }


        // Clear the file contents
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(new byte[0]);
            fos.close();
        }
         */

        //return messages;
    }
}