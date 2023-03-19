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


public class myServer{
	public static final String usersPath = "files/serverFiles/users.txt";

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


		File usersTxt = new File(usersPath);

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

				File usersFile = new File(usersPath);
				boolean fileExists = usersFile.exists();
				if (fileExists) { //Caso exista users.txt
					String autenticacao = userManager.loginManager(user);
					outStream.writeObject(autenticacao);

					if (autenticacao.equals("Password errada")) {
						inStream.close();
						outStream.close();
						Thread.currentThread().interrupt();
					}

					if (autenticacao.equals("Registado com sucesso")) {
						//Quando se regista um utilizador novo, este é adicionado ao ficheiro users.txt e ao arraylist users
						userRegister(user);
					}
				}else{ //Caso não exista ficheiro users.txt cria-o e adiciona o user atual
					Path path = Paths.get("files/serverFiles/");
					File checkIfExists = new File(path.toString());
					if (!checkIfExists.exists()) Files.createDirectories(path);

					usersFile.createNewFile();
					outStream.writeObject("Registado com sucesso");
					userRegister(user);
				}

				while(!socket.isClosed()){
					String comando = inStream.readObject().toString();
					String[] partsCmd = comando.split(" ");

					switch (partsCmd[0]) {
						case "add":
							Wine wine = new Wine(partsCmd[1], partsCmd[2]);
							wineManager.addWine(wine);
							fileManager.receiveFile(inStream, Path.of("files/serverFiles/Wines/" + wine.getName() + "/"), "image.jpeg");

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
		fileManager.writeContentToFile(new File(usersPath), user.toString(), true);
	}
}