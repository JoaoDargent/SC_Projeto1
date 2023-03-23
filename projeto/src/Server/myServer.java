package Server;

/***************************************************************************
*   Seguranca e Confiabilidade 2020/21
*
*
***************************************************************************/

import Library.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


public class myServer{
	public static final String filesPath = "../files/serverFiles/";
	public static final String usersPath = "users.txt";

	private UserManager userManager = new UserManager();
	private FileManager fileManager = new FileManager();
	private WineManager wineManager = new WineManager();

	public static void main(String[] args) throws IOException {
		System.out.println("servidor: main");
		myServer server = new myServer();
		server.startServer();
	}

	public void startServer () throws IOException {
		ServerSocket sSoc = null;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Introduza os seguintes parâmetros");
		System.out.println("TintolmarketServer <port>");
		System.out.println("Caso omita port, será utilizado 12345.");
		String fromUser = inFromUser.readLine();
		String[] fromUserSplitted = fromUser.split(" ");

		//Arranca socket com a port passada como argumento ou com a port 12345 caso nao seja passada nenhuma
		if (fromUserSplitted.length > 1){
			sSoc = new ServerSocket(Integer.parseInt(fromUserSplitted[1])); //Inicia ss com a port passada como argumento
			System.out.println("TintolmarketServer Iniciado");
		}else{
			sSoc = new ServerSocket(12345); //Inicia ss com a port 12345
			System.out.println("TintolmarketServer Iniciado");
		}


		//File usersTxt = new File(usersPath);

/*
		TODO load users from file to memory (if file exists)
		if (usersTxt.exists()){
			onLoad(fileManager, userManager);
		}*/

		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		//sSoc.close();
	}


	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {
		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}
 
		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String credenciais = (String) inStream.readObject();
				User user = new User(credenciais.split(":")[0], credenciais.split(":")[1]);
				String login = user.getId() + ":" + user.getPassword();

				File files = new File(filesPath);
				if(!files.exists()) files.mkdirs();

				File usersFile = new File(filesPath + usersPath);
				Scanner reader;
				BufferedWriter writer;
			
				if (usersFile.exists()) { //Caso exista users.txt
					reader = new Scanner(usersFile);
					writer = new BufferedWriter(new FileWriter(usersFile, true));
					boolean contains = false;
					boolean wrongLogin = false;

					while(reader.hasNextLine()){
						String account = reader.nextLine();
						//utilizador existe
						if(account.contains(login)){
							contains = true;
						//utilizador enviou uma das credenciais errada
						} else if((account.contains(user.getId()) && !account.contains(user.getPassword()))) {
							wrongLogin = true;
						}
					}
					//se credenciais estão ok
					if (contains){
						outStream.writeObject("Autenticado com sucesso");
					} else {
						//se utilizador não exite
						if(wrongLogin == false){
							userRegister(user);
							outStream.writeObject("Registado com sucesso");
						//se utilizador existe mas o par clientID/password está errado
						} else {
							outStream.writeObject("Password errada");
							System.exit(0);
						}
					}
					reader.close();
					writer.close();
					
				}else{ //Caso não exista ficheiro users.txt cria-o e adiciona o user atual
					usersFile.createNewFile();
					userRegister(user);
					outStream.writeObject("Registado com sucesso");
				}

				while(!socket.isClosed()){
					String comando = inStream.readObject().toString();
					String[] partsCmd = comando.split(" ");

					switch (partsCmd[0]) {
						case "add":
							Wine wine = new Wine(partsCmd[1], partsCmd[2]);
							wineManager.addWine(wine);
							fileManager.receiveFile(inStream, filesPath + "Wines/" + wine.getName() + "/", "image.jpg");
							break;
						case "sell":
							//TODO Caso o user queira adicionar quantidade ao stock de um vinho que já tem à venda
							//Caso o vinho nao exista e devolvido um erro
							if (!wineManager.checkIfWineExists(partsCmd[1]))
								outStream.writeObject("Não existe vinho com esse nome");
							else
								wineManager.addWineToStock(fileManager, user, partsCmd[1], Integer.parseInt(partsCmd[2]), Integer.parseInt(partsCmd[3]));

							break;
						case "view":
							outStream.writeObject(wineManager.viewWineByName(partsCmd[1]));

							break;
						case "buy":
							//TODO
							wineManager.buyWine(userManager, partsCmd[1], partsCmd[2], Integer.parseInt(partsCmd[3]));
							break;
						case "wallet":
							outStream.writeObject(user.getBalance());

							break;
						case "classify":

							outStream.writeObject(wineManager.classifyWine(fileManager,partsCmd[1], Integer.parseInt(partsCmd[2])));

							break;
						case "talk":
							//TODO
							break;
						case "read":
							//TODO
							break;
						case "exit":
							inStream.close();
							outStream.close();
							socket.close();
							Thread.currentThread().interrupt();
							break;
					}
				}

					outStream.close();
					inStream.close();

					socket.close();

			} catch(IOException | ClassNotFoundException e){
				e.printStackTrace();
			}
		}
	}

	private void userRegister(User user){
		userManager.addUser(user);
		user.setBalance(200);
		fileManager.writeContentToFile(new File(filesPath + usersPath), user.toString(), true);
	}
}