package Library;

import java.io.File;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;

public class EncryptionManager {

    public static File encryptFile(String fileToEncrypt, SecretKey secretKey) throws Exception {

        // create a new file object
        File file = new File(fileToEncrypt);
        String fileName = file.getName().replaceAll("\\.txt$", "");

        // read the contents of the file into a byte array
        byte[] fileData = Files.readAllBytes(file.toPath());

        // use server's AES key
        SecretKey key = secretKey;

        // create a cipher object for encryption
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);


        FileInputStream fis = new FileInputStream(fileName + ".txt");
        FileOutputStream fos = new FileOutputStream(fileName + ".cif");
        CipherOutputStream cos = new CipherOutputStream(fos, c);

        byte[] b = new byte[16];
        int i = fis.read(b);
        while (i != -1) {
            cos.write(b, 0, i);
            i = fis.read(b);
        }

        cos.close();
        fis.close();
        fos.close();


        byte[] keyEncoded = key.getEncoded();
        FileOutputStream kos = new FileOutputStream(fileName + ".key");
        ObjectOutputStream oos = new ObjectOutputStream(kos);
        oos.writeObject(keyEncoded);
        oos.close();
        kos.close();

        // create and return the encrypted file object
        File encryptedFile = new File(fileName + ".cif");
        return encryptedFile;
    }


    public static File decryptFile(File file) throws Exception {

        String fileName = file.getName().replaceAll("\\.cif$", "");

        //ler a chave cifrada do ficheiro
        FileInputStream kis = new FileInputStream(fileName + ".key");
        ObjectInputStream ois = new ObjectInputStream(kis);
        byte[] wrappedKey = (byte[]) ois.readObject();
        ois.close();

        //obter a chave privada da keystore
        FileInputStream kfile = new FileInputStream("mykeystore.jks"); // caminho para a keystore
        KeyStore kstore = KeyStore.getInstance("JKS");
        char[] password = "password".toCharArray(); // substitua "password" pela senha da keystore
        kstore.load(kfile, password);

        Key myPrivateKey = kstore.getKey("pardechaves", "password".toCharArray()); // substitua "pardechaves" pelo alias da chave privada na keystore

        //decifrar a chave secreta key com a chave privada
        Cipher c = Cipher.getInstance("RSA");
        c.init(Cipher.UNWRAP_MODE, myPrivateKey);
        SecretKey key = (SecretKey) c.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

        //ler o ficheiro cifrado e decifrar com a chave secreta
        FileInputStream fis = new FileInputStream(fileName + ".cif");
        FileOutputStream fos = new FileOutputStream(fileName + "_decif.txt");

        c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);

        CipherInputStream cis = new CipherInputStream(fis, c);
        byte[] b = new byte[16];
        int i = cis.read(b);
        while (i != -1) {
            fos.write(b, 0, i);
            i = cis.read(b);
        }

        cis.close();
        fis.close();
        fos.close();

        // return the decrypted file
        return new File(fileName + "_decif.txt");
    }

}