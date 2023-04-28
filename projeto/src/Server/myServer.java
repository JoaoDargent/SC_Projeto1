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
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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

	public myServer(String[] args) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ClassNotFoundException {
		main(args);
	}

	public myServer() {}

	public static void main(String[] args) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ClassNotFoundException {
		System.out.println("servidor: main");
		myServer server = new myServer();
		server.startServer();
	}

	public void startServer () throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ClassNotFoundException {
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
		File usersTxtkeyFile = new File(filesPath + "usersTxtkey.ser");
		if(usersTxtkeyFile.exists()){

			FileInputStream fileIn = new FileInputStream(filesPath + "usersTxtkey.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			byte[] keyBytes  = (byte[]) in.readObject();

			// Reconstruct the SecretKey object
			usersTxtkey = new SecretKeySpec(keyBytes, "PBEWithHmacSHA256AndAES_128");

			in.close();
			fileIn.close();
		}else{
			File files = new File(filesPath);
			if(!files.exists()) files.mkdirs();

			usersTxtkey = kf.generateSecret(keySpec);
			FileOutputStream fileOut = new FileOutputStream(filesPath + "usersTxtkey.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(usersTxtkey.getEncoded());
			out.flush();
			out.close();
			fileOut.close();
		}

		System.setProperty("javax.net.ssl.keyStore", keystore);
		System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
		System.setProperty("javax.net.ssl.keyStorePassword", keystorePwd);

		onLoad();

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
						//cria a pasta dos clientFiles
						if(!usersPFolder.exists()) usersPFolder.mkdir();
						File userFdr = new File(usersPFolder + "/" + userId + "/");
						userFdr.mkdir();

						//Cria a pasta do user no serverFiles
						File userFolder = new File(filesPath + "/Users/" + userId + "/");
						userFolder.mkdirs();

						fileManager.writeCertToFile(userCert, filesPath + "/Users/" + userId + "/" +  "cert.cer");
						user = new User(userId, filesPath + "/Users/" + userId + "/" +  "cert.cer");

						/****** Caso seja o primeiro user******/
						if (!(new File(filesPath + "users.cif").exists())) {
							if (!usersFile.exists()) {
								usersFile.createNewFile();
								userRegister(user);
								paramsPBE = encryptionManager.encryptUsersTxt(usersTxtkey);

								FileOutputStream fileOut = new FileOutputStream(filesPath + "usersPBE.ser");
								ObjectOutputStream out = new ObjectOutputStream(fileOut);

								out.writeObject(paramsPBE);
								out.flush();
								out.close();
								fileOut.close();
							}
						} else {
							encryptionManager.decryptUsersTxt(usersTxtkey, paramsPBE);
							userRegister(user);
							paramsPBE = encryptionManager.encryptUsersTxt(usersTxtkey);

							FileOutputStream fileOut = new FileOutputStream(filesPath + "usersPBE.ser");
							ObjectOutputStream out = new ObjectOutputStream(fileOut);

							out.writeObject(paramsPBE);
							out.flush();
							out.close();
							fileOut.close();
						}

						outStream.writeObject("Verificacao feita com sucesso");
					} else if (nonceCheck.equals("Erro: nonce assinado diferente")) {
						outStream.writeObject("Erro: nonce assinado diferente");
					} else {
						outStream.writeObject("Erro: primeiro nonce retornado diferente");
					}
				} else {
					FileInputStream fis = new FileInputStream(filesPath + "/Users/" + userId + "/" +  "cert.cer");
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
					PublicKey chavePublica = cert.getPublicKey();
					byte[] nonceSigned = (byte[]) inStream.readObject();


					if (encryptionManager.checkSignedNonce(nonceSigned, chavePublica, nonce)){
						outStream.writeObject("Verificacao feita com sucesso");
						user = userManager.getUserById(userId);
					} else {
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
							outStream.writeObject(wineManager.viewWineByName(partsCmd[1]));
							if(wineManager.checkIfWineExists(partsCmd[1])){
								outStream.writeObject(true);
								fileManager.sendFile(outStream,filesPath + "Wines/" + partsCmd[1] + "/", partsCmd[1] + ".jpg");
							} else {
								outStream.writeObject(false);
							}
							break;
						case "buy":
							outStream.writeObject(wineManager.buyWine(userManager, partsCmd[1], partsCmd[2], user.getId(), Integer.parseInt(partsCmd[3])));
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
							outStream.writeObject(messageManager.talk(fileManager, userManager, user, receiver, mensagem));
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
						default:
							outStream.writeObject("Comando nao reconhecido");
							break;
					}
				}
				outStream.close();
				inStream.close();
				socket.close();

			} catch(IOException | ClassNotFoundException e ){
				e.printStackTrace();
			} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException e) {
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

    private void onLoad() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ClassNotFoundException {

		//Check if usersPBE.txt exists
		File usersPBE = new File( filesPath + "usersPBE.ser");
		if (usersPBE.exists()) {

			FileInputStream fileIn = new FileInputStream(filesPath + "usersPBE.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			paramsPBE = (byte[]) in.readObject();
			in.close();
			fileIn.close();

			File usersFile = new File(filesPath + "users.cif");
			if (usersFile.exists()) {
				encryptionManager.decryptUsersTxt(usersTxtkey, paramsPBE);
				File usersTxt = new File(filesPath + "users.txt");
				String usersContent = fileManager.readContentFromFile(usersTxt);
				String[] users = usersContent.split("\n");
				for (String user : users) {
					if (!userManager.checkIfUserExists(user)) {
						String[] userSplitted = user.split(":");
						User userTest = new User(userSplitted[0], userSplitted[1]);
						userRegisterOnLoad(userTest);
						userTest.loadBalance(userTest.getId(), fileManager);
					}
				}
				paramsPBE = encryptionManager.encryptUsersTxt(usersTxtkey);

				FileOutputStream fileOut = new FileOutputStream(filesPath + "usersPBE.ser");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(paramsPBE);
				out.flush();
				out.close();
				fileOut.close();


			}

			File winesFolders = new File(filesPath + "/Wines/");
			//How to get a list of all folders in a directory
			File[] wines = winesFolders.listFiles();
			if (wines != null) {
				for (File wine : wines) {
					if (!wineManager.checkIfWineExists(wine.getName())) {
						File wineFolder = new File(filesPath + "/Wines/" + wine.getName() + "/");
						Wine wineTest = new Wine(wine.getName(), wine.getName() + ".jpg");
						wineManager.addWine(wineTest);

						File stockTXT = new File(wineFolder + "/stock.txt");
						if (stockTXT.length()!=0){
							String[] stockLine = fileManager.readContentFromFile(stockTXT).split("\n");

							for (String strings: stockLine) {
								String[] stock = strings.split(":");
								wineManager.addWineToStockOnLoad(stock[0], wineTest, Integer.parseInt(stock[1]), Integer.parseInt(stock[2]));
							}
						}



						File classifyTXT = new File(wineFolder + "/classify.txt");
						if (classifyTXT.length()!=0){
							String[] classify = fileManager.readContentFromFile(classifyTXT).split("\n");
							if (classify.length > 0) {
								for (String s : classify) {
									wineTest.setStars(Integer.parseInt(s));
								}
							}
						}

					}
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

	private static byte[] parseByteArray(String s) {
		String[] byteStrings = s.substring(1, s.length() - 1).split(", ");
		byte[] byteArray = new byte[byteStrings.length];
		for (int i = 0; i < byteStrings.length; i++) {
			byteArray[i] = Byte.parseByte(byteStrings[i]);
		}
		return byteArray;
	}
}