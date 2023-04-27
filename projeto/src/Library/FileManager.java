package Library;

import java.io.*;
import java.nio.file.Files;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

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
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void sendFile(ObjectOutputStream outStream, String path, String fileName) throws IOException, ClassNotFoundException {
        // Create a file object for the file to be sent
        File file = new File(path + fileName);
        // Create a byte array to hold the file data
        byte[] fileBytes = new byte[4096];
        
        // Wrap the output stream in a buffered output stream
        BufferedOutputStream bos = new BufferedOutputStream(outStream);
        // Send the file name and length
        outStream.writeObject(fileName);
        outStream.writeLong(fileBytes.length);
        // Send the file data
        bos.write(fileBytes, 0, fileBytes.length);
        bos.flush();
    }

    /***
     * Recebe um ficheiro do cliente
     * @param inStream - stream de entrada do cliente
     * @param path - caminho onde o ficheiro vai ser guardado
     * @param fileName - nome do ficheiro a ser guardado
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void receiveFile(ObjectInputStream inStream, String path/* , String fileName*/) throws IOException, ClassNotFoundException {
        String fileName = (String) inStream.readObject();
        long fileLength = inStream.readLong();
        
        // Create a file output stream for the destination file
        FileOutputStream fos = new FileOutputStream(new File(path, fileName));
        // Wrap the input stream in a buffered input stream
        BufferedInputStream bis = new BufferedInputStream(inStream);
        
        // Create a byte array to hold the file data
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesRead = 0;
        
        // Reads the file data from the input stream and writes it to the output stream 
        // in chunks of 4096 bytes until the entire file has been received
        while ((bytesRead = bis.read(buffer, 0, Math.min(buffer.length, (int) (fileLength - totalBytesRead)))) != -1) {
            fos.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
            if (totalBytesRead == fileLength) {
                break;
            }
        }
        fos.flush();
        fos.close();
    }

    public void writeCertToFile(X509Certificate cert, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(cert.getEncoded());
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
    }
}
