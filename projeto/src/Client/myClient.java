package Client;

import Library.FileManager;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Seguranca e Confiabilidade 2022/23
 * Filipa Monteiro: 51015
 * João Aguiar: 47120
 * João Figueiredo: 53524
 */
public class myClient {

    private static Socket cSocket;
    private FileManager fileManager = new FileManager();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Client");
        myClient client = new myClient();
        client.startClient();
    }


    //Socket clientSocket = new Socket(InetAddress.getLocalHost(), 23456);
    //ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
    //ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());


    private void startClient() throws IOException, ClassNotFoundException {
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

        if (serverAddress.contains(":")) {
            String[] serverAddressArr = serverAddress.split(":");
            cSocket = new Socket(String.valueOf(serverAddressArr[0]), Integer.parseInt(serverAddressArr[1]));
        } else {
            cSocket = new Socket(serverAddress, 12345);
        }

        ObjectInputStream in = new ObjectInputStream(cSocket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(cSocket.getOutputStream());
        out.writeObject(userID);

        String respostaCredenciais = String.valueOf(in.readObject());

        if (respostaCredenciais.equals("Autenticado com sucesso")) {
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
        }
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
