public class MainInitServer {
    private static final String HOST = "localhost";
    private static final int PORT = 3457;
    public static void main(String[] args){

        ExclusaServer server = new ExclusaServer(HOST, PORT);
        server.iniciarServidor();
    }
}