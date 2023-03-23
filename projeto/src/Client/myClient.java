package Client;

import Library.FileManager;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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
        System.out.println("Tintolmarket <serverAddress> <userID> [password]");
        System.out.println("Note que serveraddress tem o seguinte formato: <IP/hostname>[:Port]. Caso não introduza a porta, será utilizada a 12345.");

        String fromUser = scanner.nextLine();
        //If para caso nao seja passada a password
        if (fromUser.split(" ").length == 3) {
            System.out.println("Olá " + fromUser.split(" ")[2] + "! Por favor introduza a password: ");
            fromUser = fromUser + " " + scanner.nextLine();
        }

        String clientUser = fromUser.split(" ")[2] + ":" + fromUser.split(" ")[3];
        String serverAddress = fromUser.split(" ")[1];


        if (serverAddress.contains(":")) {
            String[] serverAddressArr = serverAddress.split(":");
            cSocket = new Socket(String.valueOf(serverAddressArr[0]), Integer.parseInt(serverAddressArr[1]));
        } else {
            cSocket = new Socket(serverAddress, 12345);
        }

        ObjectInputStream in = new ObjectInputStream(cSocket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(cSocket.getOutputStream());
        out.writeObject(clientUser);


        String respostaCredenciais = String.valueOf(in.readObject());

        if (respostaCredenciais.equals("Autenticado com sucesso")) {
            while (true) {
                System.out.println("Autenticado com sucesso!");
                System.out.println("Insira um comando! caso queira ver a lista de comandos insira L");
                recebeComandos(cSocket, scanner, in, out, clientUser);
            }
        } else if (respostaCredenciais.equals("Registado com sucesso")) {
            System.out.println(respostaCredenciais);
            while (true) {
                System.out.println("Registado com sucesso!");
                System.out.println("Insira um comando! caso queira ver a lista de comandos insira L");
                recebeComandos(cSocket, scanner, in, out, clientUser);
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

    private void recebeComandos(Socket cSocket, Scanner scanner, ObjectInputStream in, ObjectOutputStream out, String clientUser) throws IOException, ClassNotFoundException {
        String comando = scanner.next();
        String[] comandoSplit = comando.split(" ");


        switch (comando) {
            case "a":
            case "add":
                String wine = scanner.next();
                String image = scanner.next();
                out.writeObject("add " + wine + " " + image);
                fileManager.sendFile(out, "../", image);
                break;
            case "s":
            case "sell":

                break;
            case "v":
            case "view":

                break;
            case "b":
            case "buy":

                break;
            case "w":
            case "wallet":
                out.writeObject("wallet");
                int wallet = (int) in.readObject();
                System.out.println("O seu saldo é: " + wallet);
                break;
            case "c":
            case "classify":

                break;
            case "t":
            case "talk":

                break;
            case "r":
            case "read":

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
