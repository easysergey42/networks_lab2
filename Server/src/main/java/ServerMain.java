public class ServerMain {


    private static final int DEFAULT_SERVER_PORT = 6060;


    public static void main(String[] args) {


        Server server = new Server(DEFAULT_SERVER_PORT);

        server.run();
    }

}