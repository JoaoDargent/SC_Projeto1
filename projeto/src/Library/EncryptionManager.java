package Library;

import java.io.File;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.nio.ByteBuffer;

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


    public static File decryptFile(File file, Key key) throws Exception {

        String fileName = file.getName().replaceAll("\\.cif$", "");

        //ler o ficheiro cifrado e decifrar com a chave secreta
        FileInputStream fis = new FileInputStream(fileName + ".cif");
        FileOutputStream fos = new FileOutputStream(fileName + "_decif.txt");

        Cipher c = Cipher.getInstance("AES");
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

    public Long decryptNonce (byte[] nonce, PublicKey chave ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        // Decrypt the signed nonce
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, chave);
        byte[] decryptedNonce = cipher.doFinal(nonce);
        // Convert bytes to long using ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(decryptedNonce);
        long nonceDecrypted = buffer.getLong();
        return nonceDecrypted;
    }

}