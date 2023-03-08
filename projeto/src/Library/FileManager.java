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
    public void sendFile(ObjectOutputStream outStream, String path, String extensao) throws IOException, ClassNotFoundException {
        ////////Tratamento Ficheiro\\\\\\\\

        File file = new File(path);
        FileInputStream fin = new FileInputStream(file);
        InputStream input = new BufferedInputStream(fin);

        ///Tamanho Ficheiro em Bytes\\\
        long fileSize = file.length();
        outStream.writeObject(fileSize);

        ///Envia Extensão\\\
        outStream.writeObject(extensao);

        ///Processo de Envio\\\

        byte[] buffer = new byte[1024];
        int numRead = 0;

        while((numRead = fin.read(buffer)) >= 0){
            outStream.write(buffer,0,numRead);
        }

        outStream.flush();
        outStream.close();
        fin.close();
    }

    /***
     * Recebe um ficheiro do cliente
     * @param inStream - stream de entrada do cliente
     * @param path - caminho onde o ficheiro vai ser guardado
     * @param fileName - nome do ficheiro a ser guardado
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void receiveFile(ObjectInputStream inStream, Path path, String fileName) throws IOException, ClassNotFoundException {
        //Recebe tamanho ficheiro
        long tamanhoFile = (long) inStream.readObject();
        String extensao = (String) inStream.readObject();
        int jaLido = 0;

        //Cria ficheiro para onde vai ser escrito o conteúdo vindo do user
        File f = new File(path + "/" + fileName + extensao);
        //File f = new File(fileName);


        FileOutputStream fout = new FileOutputStream(f);
        OutputStream output = new BufferedOutputStream(fout);
        byte[] buffer = new byte[1024];

        //Recebe conteúdo do User
        while(jaLido < tamanhoFile){
            int lido = inStream.read(buffer);
            output.write(buffer,0,lido);
            jaLido += lido;
        }

        fout.flush();
        fout.close();
        output.flush();
        output.close();
    }
}
