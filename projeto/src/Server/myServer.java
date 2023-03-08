package Server;

/***************************************************************************
*   Seguranca e Confiabilidade 2020/21
*
*
***************************************************************************/

import Library.FileManager;
import Library.User;
import Library.UserManager;

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
					} else if (autenticacao.equals("Registado com sucesso")) {
						//Quando se regista um utilizador novo, este é adicionado ao ficheiro users.txt e ao arraylist users
						userRegister(user);
						outStream.writeObject("Registado com sucesso");
					} else if (autenticacao.equals("Autenticado com sucesso")) {
						outStream.writeObject("Autenticado com sucesso");
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

					//I want to make a list of if's like the Client.myClient one but for the server (with partsCmd[0] == "add" for example)
					if(partsCmd[0] == "add"){
						//TODO
					} else if (partsCmd[0] == "sell") {
						//TODO
					} else if (partsCmd[0] == "view") {
						//TODO
					} else if (partsCmd[0] == "buy") {
						//TODO
					} else if (partsCmd[0].equals("wallet")) {
						//TODO
						outStream.writeObject(user.getBalance());

					} else if (partsCmd[0] == "classify") {
						//TODO
					} else if (partsCmd[0] == "talk") {
						//TODO
					} else if (partsCmd[0] == "read") {
						//TODO
					} else if (partsCmd[0] == "exit") {
						inStream.close();
						outStream.close();
						socket.close();
						Thread.currentThread().interrupt();
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