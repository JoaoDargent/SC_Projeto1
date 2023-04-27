package Client;

import Library.FileManager;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Scanner;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Seguranca e Confiabilidade 2022/23
 * Filipa Monteiro: 51015
 * João Aguiar: 47120
 * João Figueiredo: 53524
 */
public class myClient {

    private FileManager fileManager = new FileManager();
    private static String truststorePwd = "truststorepw";
    private static KeyStore kstore;
    private static PrivateKey privateKey;

    public static void main(String[] args) throws IOException, ClassNotFoundException, SignatureException, Exception {
        System.out.println("Client");
        myClient client = new myClient();
        client.startClient();
    }


    //Socket clientSocket = new Socket(InetAddress.getLocalHost(), 23456);
    //ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
    //ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());


    private void startClient() throws IOException, ClassNotFoundException, SignatureException, Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduza os seguintes parâmetros");
        System.out.println("Tintolmarket <serverAddress> <truststore> <keystore> <password-keystore> <userID>");
        System.out.println("Note que serveraddress tem o seguinte formato: <IP/hostname>[:Port]. Caso não introduza a porta, será utilizada a 12345.");

        String fromUser = scanner.nextLine();
        //If para caso nao seja passada a password
        if (fromUser.split(" ").length == 3) {
            System.out.println("Olá " + fromUser.split(" ")[2] + "! Por favor introduza a password: ");
            fromUser = fromUser + " " + scanner.nextLine();
        }

        String serverAddress = fromUser.split(" ")[1];
        String truststore = fromUser.split(" ")[2];
        String keystore = fromUser.split(" ")[3];
        String passwordKeystore = fromUser.split(" ")[4];
        String userID = fromUser.split(" ")[5];

        System.setProperty("javax.net.ssl.trustStore", truststore);
        System.setProperty("javax.net.ssl.trustStorePassword", truststorePwd);
        System.setProperty("javax.net.ssl.keyStore", keystore);
		System.setProperty("javax.net.ssl.keyStoreType", "JCEKS");
		System.setProperty("javax.net.ssl.keyStorePassword", passwordKeystore);

        try {
            FileInputStream kfile = new FileInputStream(keystore);
            kstore = KeyStore.getInstance("JCEKS");
            kstore.load(kfile, passwordKeystore.toCharArray());

            // Get the aliases in the keystore
            Enumeration<String> aliases = kstore.aliases();
            // Iterate through the aliases and find the correct one
            String alias = null;
            while (aliases.hasMoreElements()) {
                String currentAlias = aliases.nextElement();
                if (kstore.isKeyEntry(currentAlias)) {
                    alias = currentAlias;
                    break;
                }
            }
            privateKey = (PrivateKey) kstore.getKey(alias, passwordKeystore.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }

        SocketFactory sf = SSLSocketFactory.getDefault( );
        SSLSocket cSocket;
        if (serverAddress.contains(":")) {
            String[] serverAddressArr = serverAddress.split(":");
            cSocket = (SSLSocket) sf.createSocket(String.valueOf(serverAddressArr[0]), Integer.parseInt(serverAddressArr[1]));
        } else {
            cSocket = (SSLSocket) sf.createSocket(serverAddress, 12345);
        }

        ObjectInputStream in = new ObjectInputStream(cSocket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(cSocket.getOutputStream());
        //Envia userID
        out.writeObject(userID);
        //Recebe nonce
        long nonce = (long) in.readObject();
        //Verifica se esta registado
        Boolean registered = (Boolean) in.readObject();

        Signature s;
        byte[] signedNonce = null;
        try {
            s = Signature.getInstance("MD5withRSA");
            s.initSign(privateKey);
            s.update(this.longToBytes(nonce));
            //Assinar nonce e enviar nonce assinado
            signedNonce = s.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }


        if (registered) {
            //Envia nonce assinado com chave privada
            out.writeObject(signedNonce);
            while (true) {
                System.out.println("Insira um comando! caso queira ver a lista de comandos insira L");
                recebeComandos(cSocket, scanner, in, out, userID);
            }
        } else {
            /*
            Envia nonce original
            Envia nonce assinado com chave privada
            Envia certificado com chave publica
             */
            out.writeObject(nonce);
            out.writeObject(signedNonce);
            
            FileInputStream fis = new FileInputStream("../src/cert" + userID + ".cer");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
            out.writeObject(cert);

            while (true) {
                System.out.println("Insira um comando! caso queira ver a lista de comandos insira L");
                recebeComandos(cSocket, scanner, in, out, userID);
            }

        }


        /*if (respostaCredenciais.equals("Autenticado com sucesso")) {
            while (true) {
                System.out.println("Insira um comando! caso queira ver a lista de comandos insira L");
                recebeComandos(cSocket, scanner, in, out, userID);
            }
        } else if (respostaCredenciais.equals("Registado com sucesso")) {
            System.out.println(respostaCredenciais);
            while (true) {
                System.out.println("Insira um comando! caso queira ver a lista de comandos insira L");
                recebeComandos(cSocket, scanner, in, out, userID);
            }

        } else if (respostaCredenciais.equals("Password errada")) {
            System.out.println("Password errada.");
            System.out.println("Programa vai terminar");
            out.close();
            in.close();
            scanner.close();
            cSocket.close();
            System.exit(0);
        }*/
    }

    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private void recebeComandos(Socket cSocket, Scanner scanner, ObjectInputStream in, ObjectOutputStream out, String clientId) throws IOException, ClassNotFoundException {
        String comando = scanner.next();
        String[] comandoSplit = comando.split(" ");


        switch (comando) {
            case "a":
            case "add":
                String wine = scanner.next();
                String image = scanner.next();
                out.writeObject("add " + wine + " " + image);
                boolean wasAdded = (boolean) in.readObject();
                if(!wasAdded){
                    System.out.println("O vinho indicado já existe.");
                } else {
                    fileManager.sendFile(out, "../", image);
                }
                break;
            case "s":
            case "sell":
                String wineS = scanner.next();
                int value = Integer.parseInt(scanner.next());
                int quantity = Integer.parseInt(scanner.next());
                out.writeObject("sell " + wineS + " " + value + " " + quantity);
                System.out.println(in.readObject());
                break;
            case "v":
            case "view":
                String wineV = scanner.next();
                out.writeObject("view " + wineV);
                String view = (String) in.readObject();
                System.out.println(view);
                Boolean exists = (Boolean) in.readObject();
                if(exists)
                    fileManager.receiveFile(in, "../files/clientFiles/" + clientId + "/");
                //Limitação do cliente: fica pendurado após receber o ficheiro
                break;
            case "b":
            case "buy":
                String wineB = scanner.next();
                String seller = scanner.next();
                int quantityB = Integer.parseInt(scanner.next());
                out.writeObject("buy " + wineB + " " + seller + " " + quantityB);
                String resposta = (String) in.readObject();
                System.out.println(resposta);
                break;
            case "w":
            case "wallet":
                out.writeObject("wallet");
                int wallet = (int) in.readObject();
                System.out.println("O seu saldo é: " + wallet);
                break;
            case "c":
            case "classify":
                String wineC = scanner.next();
                String stars = scanner.next();
                out.writeObject("classify " + wineC + " " + stars);
                String classify  = (String) in.readObject();
                System.out.println(classify);
                break;
            case "t":
            case "talk":
                String comandoT = scanner.nextLine();
                String user = comandoT.split(" ")[1];
                //I want to slice comandoT from the 3rd element to the end
                String message = comandoT.substring(comandoT.indexOf(" ", comandoT.indexOf(" ") + 1) + 1);

                out.writeObject("talk " + user + " " + message);
                System.out.println(in.readObject());
                break;
            case "r":
            case "read":
                out.writeObject("read");
                String read = (String) in.readObject();
                System.out.println(read);
                break;
            case "l":
            case "L":
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("A lista de comandos eh a seguinte:\n");
                stringBuilder.append("add <wine> <image>\n");
                stringBuilder.append("sell <wine> <value> <quantity>\n");
                stringBuilder.append("view <wine>\n");
                stringBuilder.append("buy <wine> <seller> <quantity>\n");
                stringBuilder.append("wallet\n");
                stringBuilder.append("classify <wine> <stars>\n");
                stringBuilder.append("talk <user> <message>\n");
                stringBuilder.append("read\n");
                stringBuilder.append("exit - caso queira encerrar de forma segura o client\n");
                String texto = stringBuilder.toString();

                System.out.println(texto);

                break;
            case "e":
            case "exit":
                System.out.println("Programa vai terminar");
                out.writeObject("exit");
                out.close();
                in.close();
                scanner.close();
                cSocket.close();
                System.exit(0);
        }


    }

}
