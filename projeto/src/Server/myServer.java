package Server;

import Library.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * Seguranca e Confiabilidade 2022/23
 * Filipa Monteiro: 51015
 * João Aguiar: 47120
 * João Figueiredo: 53524
 */
public class myServer{
	public static final String filesPath = "../files/serverFiles/";
	public static final String usersP = "../files/clientFiles/";
	public static final String usersPath = "users.txt";

	private UserManager userManager = new UserManager();
	private FileManager fileManager = new FileManager();
	private WineManager wineManager = new WineManager();
	private MessageManager messageManager = new MessageManager();

	public static void main(String[] args) throws IOException {
		System.out.println("servidor: main");
		myServer server = new myServer();
		server.startServer();
	}

	public void startServer () throws IOException {
		ServerSocket sSoc = null;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Introduza os seguintes parâmetros");
		System.out.println("TintolmarketServer <port> <password-cifra> <keystore> <password-keystore>");
		System.out.println("Caso omita port, será utilizado 12345.");
		String fromUser = inFromUser.readLine();
		String[] fromUserSplitted = fromUser.split(" ");
		int port = 0;
		String passwordCifra;
		String keystore = null;
		String keystorePwd = null;

		if(fromUserSplitted.length == 4){
			port = 12345;
			passwordCifra = fromUserSplitted[1];
			keystore = fromUserSplitted[2];
			keystorePwd = fromUserSplitted[3];
		} else if (fromUserSplitted.length == 5){
			port = Integer.parseInt(fromUserSplitted[1]);
			passwordCifra = fromUserSplitted[2];
			keystore = fromUserSplitted[3];
			keystorePwd = fromUserSplitted[4];
		} else if (fromUserSplitted.length < 4){
			System.out.println("Parâmetros insuficientes");
		}

		//Arranca socket com a port passada como argumento ou com a port 12345 caso nao seja passada nenhuma
		//sSoc = new ServerSocket(port); //Inicia ss com a port passada como argumento
		System.out.println("TintolmarketServer Iniciado");

		System.setProperty("javax.net.ssl.keyStore", keystore);
		System.setProperty("javax.net.ssl.keyStoreType", "JCEKS");
		System.setProperty("javax.net.ssl.keyStorePassword", keystorePwd);
		//System.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, RC4");

		onLoad();

		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		SSLServerSocket serverSocket;

        try {
         	serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
			
			// Thread para cada cliente
			while(true) {
				try {
					SSLSocket inSoc = (SSLSocket) serverSocket.accept();
					ServerThread newServerThread = new ServerThread(inSoc);
					newServerThread.start();
				}
				catch (IOException e) {
					System.err.println("Erro ao aceitar conexao de um cliente:");
					e.printStackTrace();
				}
			}
        } catch (IOException a) {
            System.err.println( "Erro ao criar server socket: " + a.getMessage() );
            System.exit( -1 );
        }
	}

	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {
		private Socket socket;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}
 
		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String userName = (String) inStream.readObject();
				System.out.println("Credenciais: " + userName);

				//Servidor envia nonce ao cliente
				long nonce = (new Random()).nextLong();
        		outStream.writeObject(nonce);
				byte[] nonceAssinado = (byte[]) inStream.readObject();
				User user = new User(userName);
				//String login = user.getId() + ":" + user.getPassword();

				File files = new File(filesPath);
				if(!files.exists()) files.mkdirs();

				File usersFile = new File(filesPath + usersPath);
				File usersPFolder = new File(usersP);
				if(!usersFile.exists()) usersPFolder.mkdirs();

				Scanner reader;
				BufferedWriter writer;
			
				if (usersFile.exists()) { //Caso exista users.txt
					reader = new Scanner(usersFile);
					writer = new BufferedWriter(new FileWriter(usersFile, true));
					boolean contains = false;
					boolean wrongLogin = false;

					while(reader.hasNextLine()){
						String account = reader.nextLine();
						/*//utilizador existe
						if(account.contains(login)){
							contains = true;
						//utilizador enviou uma das credenciais errada
						} else if((account.contains(user.getId()) && !account.contains(user.getPassword()))) {
							wrongLogin = true;
						}*/
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
						
							if(!wineManager.addWine(wine)) {
								outStream.writeObject(false);
							} else {
								outStream.writeObject(true);
								fileManager.receiveFile(inStream, filesPath + "Wines/" + wine.getName() + "/");
							}					
							break;
						case "sell":
							//Caso o vinho nao exista e devolvido um erro
							if (!wineManager.checkIfWineExists(partsCmd[1])) outStream.writeObject("O vinho nao existe");
							String resposta = wineManager.addWineToStock(fileManager, user, partsCmd[1], Integer.parseInt(partsCmd[2]), Integer.parseInt(partsCmd[3]));
							outStream.writeObject(resposta);
							break;
						case "view":
							outStream.writeObject(wineManager.viewWineByName(partsCmd[1], user.getId()));
							if(wineManager.checkIfWineExists(partsCmd[1])){
								outStream.writeObject(true);
								fileManager.sendFile(outStream,filesPath + "Wines/" + partsCmd[1] + "/", partsCmd[1] + ".jpg");
							} else {
								outStream.writeObject(false);
							}
							break;
						case "buy":
							outStream.writeObject(wineManager.buyWine(userManager, partsCmd[1], partsCmd[2],user.getId(), Integer.parseInt(partsCmd[3])));
							break;
						case "wallet":
							outStream.writeObject(user.getBalance());
							break;
						case "classify":
							outStream.writeObject(wineManager.classifyWine(fileManager,partsCmd[1], Integer.parseInt(partsCmd[2])));
							break;
						case "talk":
							StringBuilder mensagem = new StringBuilder();
							for(int i = 2; i < partsCmd.length; i++){
								mensagem.append(partsCmd[i] + " ");
							}
							outStream.writeObject(messageManager.talk(fileManager, userManager, user, partsCmd[1], mensagem.toString()));
							break;
						case "read":
								outStream.writeObject(messageManager.read(fileManager, user));
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
	private void onLoad() throws IOException {
		File usersTXT = new File(filesPath+usersPath);
		if(usersTXT.exists()){
		String usersContent = fileManager.readContentFromFile(usersTXT);
		String[] users = usersContent.split("\n");
			for (String user : users){
				if (!userManager.checkIfUserExists(user)){
					String[] userSplitted = user.split(":");
					User userTest = new User(userSplitted[0]);
					userRegisterOnLoad(userTest);
					userTest.loadBalance(fileManager);
				}
			}
		}

		File winesFolders = new File(filesPath+"/Wines/");
		//How to get a list of all folders in a directory
		File[] wines = winesFolders.listFiles();
		if (wines != null) {
			for (File wine : wines) {
				if(!wineManager.checkIfWineExists(wine.getName())){
					File wineFolder = new File(filesPath + "/Wines/" + wine.getName());
					Wine wineTest = new Wine(wine.getName(), wine.getName() + ".jpg");
					wineManager.addWine(wineTest);

					File stockTXT = new File(wineFolder + "/stock.txt");
					fileManager.readContentFromFile(stockTXT);
					String[] stock = fileManager.readContentFromFile(stockTXT).split("\n");
					ArrayList<String> stockLoad = new ArrayList<>();
					wineTest.setStock(stockLoad);

					File classifyTXT = new File(wineFolder + "/classify.txt");
					String[] classify = fileManager.readContentFromFile(classifyTXT).split("\n");
					ArrayList<String> classifyLoad = new ArrayList<>();
					wineTest.setStarsLoad(fileManager, classifyLoad);
				}
			}
		}
	}

	private void userRegisterOnLoad(User userTest) throws IOException {
		userManager.addUser(userTest);
		userTest.setBalance(fileManager,200);
	}

	private void userRegister(User user) throws IOException {
		userManager.addUser(user);
		user.setBalance(fileManager,200);
		fileManager.writeContentToFile(new File(filesPath + usersPath), user.toString(), true);

		File userFolder = new File(usersP + user.getId() + "/");
		userFolder.mkdir();
	}
}