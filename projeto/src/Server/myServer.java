package Server;

import Library.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.security.cert.X509Certificate;

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
	private EncryptionManager encryptionManager = new EncryptionManager();
	protected SecretKey usersTxtkey;

	protected byte[] paramsPBE;

	public myServer(String[] args) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		main(args);
	}

	public myServer() {}

	public static void main(String[] args) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		System.out.println("servidor: main");
		myServer server = new myServer();
		server.startServer();
	}

	public void startServer () throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		ServerSocket sSoc = null;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Introduza os seguintes parâmetros");
		System.out.println("TintolmarketServer <port> <password-cifra> <keystore> <password-keystore>");
		System.out.println("Caso omita port, será utilizado 12345.");
		//String fromUser = inFromUser.readLine();
		String fromUser = "a cifra keystore.server serverpw";
		String[] fromUserSplitted = fromUser.split(" ");
		int port = 0;
		String passwordCifra = null;
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

		/***** Geracao de chave para encriptar ficheiro users.txt *****/
		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
		PBEKeySpec keySpec = new PBEKeySpec(passwordCifra.toCharArray(), salt, 20);
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		usersTxtkey = kf.generateSecret(keySpec);






		//Arranca socket com a port passada como argumento ou com a port 12345 caso nao seja passada nenhuma
		//sSoc = new ServerSocket(port); //Inicia ss com a port passada como argumento


		System.setProperty("javax.net.ssl.keyStore", keystore);
		System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
		System.setProperty("javax.net.ssl.keyStorePassword", keystorePwd);

		//onLoad();

		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		SSLServerSocket serverSocket;

        try {
			serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
			System.out.println("TintolmarketServer Iniciado");

			// Thread para cada cliente
			while(true) {
				try {
					SSLSocket inSoc = (SSLSocket) serverSocket.accept();
					ServerThread newServerThread = new ServerThread(inSoc);
					newServerThread.start();
				} catch (IOException e) {
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
			User user = null;
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				File files = new File(filesPath);
				if(!files.exists()) files.mkdirs();

				File usersFile = new File(filesPath + usersPath);
				File usersPFolder = new File(usersP);


				/******** Autenticacao 2a fase ********/

				//Recebe o userID do cliente
				String userId = (String) inStream.readObject();
				System.out.println("User ID: " + userId);
				//Servidor envia o nonce ao cliente
				long nonce = new Random().nextLong();
				outStream.writeObject(nonce);
				//Verifica se esta registado
				boolean isRegistered = userManager.checkIfUserExists(userId);
				outStream.writeObject(isRegistered);




                /*Se nao estiver registado, o cliente tem de:
					Devolver o nonce enviado pelo servidor
					Enviar o nonce cifrado com a chave privada do cliente
					Enviar o certificado do cliente com a chave publica
				 */


				if(!isRegistered){
					long nonceReturned = (long) inStream.readObject();
					byte[] nonceSigned = (byte[]) inStream.readObject();
					X509Certificate userCert = (X509Certificate) inStream.readObject();
					PublicKey chavePublica = userCert.getPublicKey();
					String nonceCheck = nonceCheck(userId, nonceReturned, nonceSigned, nonce, chavePublica);

					if(nonceCheck.equals("Verificacao feita com sucesso")){

						//Cria a pasta do user
						File userFolder = new File(filesPath + "/Users/" + userId + "/");
						userFolder.mkdirs();

						fileManager.writeCertToFile(userCert, filesPath + "/Users/" + userId + "/" +  "cert.cer");
						user = new User(userId, filesPath + "/Users/" + userId + "/" +  "cert.cer");


						/****** Caso seja o primeiro user******/
						if (!(new File(filesPath + "users.cif").exists())) {
							if (!usersFile.exists()) {
								usersFile.createNewFile();
								userRegister(user);
								encryptionManager.encryptUsersTxt(usersTxtkey);
							}
						}

						else{
							encryptionManager.decryptUsersTxt(usersTxtkey, paramsPBE);
							userRegister(user);
							encryptionManager.encryptUsersTxt(usersTxtkey);

						}

						outStream.writeObject("Verificacao feita com sucesso");
					} else if (nonceCheck.equals("Erro: nonce assinado diferente")) {
						outStream.writeObject("Erro: nonce assinado diferente");
					}else{
						outStream.writeObject("Erro: primeiro nonce retornado diferente");
					}
				}else{
					FileInputStream fis = new FileInputStream(filesPath + "/Users/" + userId + "/" +  "cert.cer");
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
					PublicKey chavePublica = cert.getPublicKey();
					byte[] nonceSigned = (byte[]) inStream.readObject();

					if (encryptionManager.checkSignedNonce(nonceSigned, chavePublica, nonce)){
						outStream.writeObject("Verificacao feita com sucesso");
					}else{
						outStream.writeObject("Erro: Este userID esta registado mas a assinatura esta errada");
					}
				}


				while(!socket.isClosed()){
					String comando = (String) inStream.readObject();
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
							String receiver = partsCmd[2];
							byte[] mensagem = (byte[]) inStream.readObject();
							outStream.writeUTF(messageManager.talk(fileManager, userManager, user, receiver, mensagem));
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

			} catch(IOException | ClassNotFoundException e ){
				e.printStackTrace();
			} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				throw new RuntimeException(e);
			} catch (InvalidAlgorithmParameterException e) {
				throw new RuntimeException(e);
			}
		}
	}
    /*private void onLoad() throws IOException {
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
	}*/

	private void userRegisterOnLoad(User userTest) throws IOException {
		userManager.addUser(userTest);
		userTest.setBalance(fileManager,200);
	}

	private void userRegister(User user) throws IOException {
		userManager.addUser(user);
		user.setBalance(fileManager,200);
		fileManager.writeContentToFile(new File(filesPath + usersPath), user.toString(), true);
	}

	private String nonceCheck(String userId, long nonceReturned, byte[] nonceSigned, long  nonce, PublicKey chavePublica) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, InvalidKeyException, IOException {
		if (nonceReturned == nonce) {
			boolean nonceAssinadoLong = encryptionManager.checkSignedNonce(nonceSigned, chavePublica, nonce);
			if (nonceAssinadoLong) {
				return "Verificacao feita com sucesso";
			} else {
				return "Erro: nonce assinado diferente";
			}
		} else {
			return "Erro: primeiro nonce retornado diferente";
		}
	}
}