package Library;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class FileManager {

    /***
     * Escreve conteudo no ficheiro. Caso o ficheiro nao exista, cria um novo.
     * @param file - nome do ficheiro em que se pretende escrever
     * @param message - conteudo a escrever no ficheiro
     * @param append - se true, adiciona conteudo ao ficheiro. Se false, escreve sobre o conteudo existente
     */
    public void writeContentToFile(File file, String message, boolean append) {
        try {
            FileWriter fileToWrite = new FileWriter(file.getPath(),append);
            if(!append){
                fileToWrite.write(message);
            }else{
                fileToWrite.write(message + "\n");
            }

            fileToWrite.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * Le o conteudo de um ficheiro
     * @param file - nome do ficheiro a ler
     * @return conteudo do ficheiro em String
     *
     */
    public String readContentFromFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileReader fr = new FileReader(file);
            long length = file.length();

            long i = 0;
            while ((i = fr.read()) != -1) {
                stringBuilder.append((char) i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();

    }

    /***
     * Envia um ficheiro para o cliente
     * @param outStream - stream de saida para o cliente
     * @param path - caminho do ficheiro a enviar
     * @param extensao - extensao do ficheiro a enviar
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void sendFile(ObjectOutputStream outStream, String path, String fileName) throws IOException, ClassNotFoundException {
        // Create a file object for the file to be sent
        File file = new File(path + fileName);
        FileInputStream fileInputStream = new FileInputStream(file);
        
        // Create a byte array to hold the file data
        byte[] fileBytes = new byte[(int) file.length()];
        // Read the file data into the byte array
        fileInputStream.read(fileBytes);
        fileInputStream.close();
        
        // Send the file data to the server
        outStream.write(fileBytes);
        outStream.flush();
    }

    /***
     * Recebe um ficheiro do cliente
     * @param inStream - stream de entrada do cliente
     * @param path - caminho onde o ficheiro vai ser guardado
     * @param fileName - nome do ficheiro a ser guardado
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void receiveFile(ObjectInputStream inStream, String path, String fileName) throws IOException, ClassNotFoundException {
        File file = new File(path);
        // make sure the directory exists
        if(!file.exists()) file.mkdirs();
        // Create a file output stream for the destination file
        FileOutputStream fos = new FileOutputStream(new File(file, fileName));
        
        // Create a byte array to hold the file data
        byte[] buffer = new byte[1024];
        int bytesRead;
        // Read the file data from the input stream and write it to the output stream
        while ((bytesRead = inStream.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }

        fos.flush();
        fos.close();
    }
}
