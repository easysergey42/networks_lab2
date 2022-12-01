import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ClientHandler implements Runnable{


    private static final String SUBDIRECTORY = "./Server/uploads/";
    private final static Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final long SPEED_TEST_INTERVAL = 3000;
    private final Socket socket;

    private FileInfo fileInfo;


    public ClientHandler(Socket socket) {
        this.socket = socket;
    }


    private FileInfo getFileContextFromClient(InputStream inputStream) throws IOException, ClassNotFoundException {

        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        FileInfo fileInfo = (FileInfo) objectInputStream.readObject();

        System.out.println("fileName = " + fileInfo.getFileName());
        System.out.println("fileSize = " + fileInfo.getFileSizeInBytes() + " bytes");

        return fileInfo;
    }




    private void receiveFile(String fileName, Long fileSizeInBytes, InputStream inputStream) throws IOException {

        Path filePath = Paths.get(fileName);
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        DataOutputStream writerToClient = new DataOutputStream(socket.getOutputStream());


        File file = filePath.toFile();
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        int bytes;
        long size = fileSizeInBytes;
        byte[] buffer = new byte[4*1024];
        long allReadBytes = 0;
        long prevAllReadBytes = 0;
        boolean clientActiveLessSpeedTestInterval = true;
        long initTime = System.currentTimeMillis();
        long lastTime = initTime;

        while (allReadBytes < size && (bytes = dataInputStream.read(buffer, 0,
                (int)Math.min(buffer.length, size))) != -1) {

            fileOutputStream.write(buffer,0,bytes);
            allReadBytes += bytes;

            long currentTime = System.currentTimeMillis();

            if (currentTime - lastTime > SPEED_TEST_INTERVAL){
                long currentSpeed = (allReadBytes - prevAllReadBytes) / (currentTime - lastTime);
                long avgSpeed = allReadBytes  / (currentTime - initTime) ;
                logger.info("File - {" + filePath.getFileName().toString() +"} has current speed = " + currentSpeed + "  Kb per second" + ", avg speed = " + avgSpeed +
                        ". PORT: " + socket.getPort());
                lastTime = currentTime;
                clientActiveLessSpeedTestInterval = false;
                prevAllReadBytes = allReadBytes;
            }

        }

        if (clientActiveLessSpeedTestInterval){
            lastTime = System.currentTimeMillis();
            if (lastTime - initTime == 0) {
                System.out.println("operating system cannot measure time");
            }
            else {
                long speed = allReadBytes  / (lastTime - initTime) ;
                logger.info("File - {" + filePath.getFileName().toString() +"} has speed = "+ speed + " Kb per second. " +
                        "PORT: " + socket.getPort());
            }
        }


        fileOutputStream.close();
    }

    @Override
    public void run() {


        try {
            InputStream inputStream = socket.getInputStream();
            this.fileInfo = getFileContextFromClient(inputStream);


            String fileNameWithPath = SUBDIRECTORY + fileInfo.getFileName();
            receiveFile(fileNameWithPath, fileInfo.getFileSizeInBytes(), inputStream);


            inputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }



    }
}