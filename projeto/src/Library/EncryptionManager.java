package Library;

import Server.myServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
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

    public boolean checkSignedNonce (byte[] nonceAssinado, PublicKey chave, long nonceOriginal ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, SignatureException {
        Signature s = Signature.getInstance("MD5withRSA");
        s.initVerify(chave);
        s.update(longToBytes(nonceOriginal));
        if (s.verify(nonceAssinado))
            return true;
        else
            return false;
    }

    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public byte[] encryptUsersTxt(SecretKey key) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Path path = Paths.get(myServer.filesPath + "users.txt");
        byte[] fileContents = Files.readAllBytes(path);

        Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        c.init(Cipher.ENCRYPT_MODE, key);

        byte[] enc = c.doFinal(fileContents);
        byte[] params = c.getParameters().getEncoded();

        Path encryptedPath = Paths.get(myServer.filesPath + "users.cif");
        Files.write(encryptedPath, enc);

        File usersFile = new File(myServer.filesPath + "users.txt");
        usersFile.delete();

        return params;
    }

    public void decryptUsersTxt(SecretKey key, byte[] params) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
        p.init(params);
        Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        d.init(Cipher.DECRYPT_MODE, key, p);
        byte [] dec = d.doFinal(Files.readAllBytes(Paths.get(myServer.filesPath + "users.cif")));

        File usersFile = new File(myServer.filesPath + "users.txt");
        usersFile.createNewFile();
        Files.write(Paths.get(myServer.filesPath + "users.txt"), dec);
    }

    public byte[] encryptMsg(Key key, String msg) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException  {
		Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		c.init(Cipher.ENCRYPT_MODE, key);
		System.out.println(key);
		byte[] msgBytes = msg.getBytes( );
		return c.doFinal(msgBytes);
	}

    public String decryptMsg(Key key, byte[] encryptedMsg) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		c.init(Cipher.DECRYPT_MODE, key);
		System.out.println(key);
		byte[] msgBytes = c.doFinal(encryptedMsg);
		return new String(msgBytes);
	}

}