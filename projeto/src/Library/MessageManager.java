package Library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

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
            FileInputStream fis = new FileInputStream(Ureceiver.getId() + ".cer");
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            Certificate cert = cf.generateCertificate(fis);
            PublicKey pk = cert.getPublicKey();
            //obter a chave publica deste user atraves do cer
            File file = new File(filesPath +"/Users/" + Ureceiver.getId() + "/messages.txt");
            //Check if directory exists
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }else if (!file.exists()){
                    file.createNewFile();
            }
            //cifrar a mensagem com a chave publica
            byte [] encmessage = encryptMsg(pk, message);
            fm.writeContentToFile(file, sender.getId() + " : " + encmessage ,true);
            return "Mensagem enviada com sucesso";
        }
    }

    public String read(FileManager fm, User reader) throws FileNotFoundException, KeyStoreException{
        File file = new File(filesPath +"/Users/" + reader.getId() + "/messages.txt");
        FileInputStream kfile = new FileInputStream("keystore" + reader.getId());
        KeyStore kstore = KeyStore.getInstance("JKS");

        PrivateKey privateKey = null;
        String mensagem = fm.readContentFromFile(file);
        
        //decifrar mensagem com a sua chave privada na keystore
       // String decmensagem = decryptMsg(privateKey, mensagem);
       //duvida em como vou buscar a password da key
        if (mensagem.equals("")){
            return "Nao tem mensagens novas";
        }
        fm.writeContentToFile(file, "",false);
        return mensagem;
    }

    private byte[] encryptMsg(PublicKey groupKey, String msg) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException  {
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, groupKey);
		System.out.println(groupKey);
		byte[] msgBytes = msg.getBytes( );
		return c.doFinal(msgBytes);
	}

	private String decryptMsg(PrivateKey groupKey, byte[] encryptedMsg) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, groupKey);
		System.out.println(groupKey);
		byte[] msgBytes = c.doFinal(encryptedMsg);
		return new String(msgBytes);
	}
}
