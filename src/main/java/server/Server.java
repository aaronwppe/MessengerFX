package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class Server {
    private ServerSocket serverSocket;

    public Server(int port) {

        try {
            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            System.out.println("ERROR: [SEVER] 'port: " + port + "' not connected!");
            System.exit(3);
        }
    }

    public void run () {
        try{
            while(!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(socket);

                clientHandler.start();
            }
        }
        catch(IOException e) {
            System.out.println("ERROR: Method failure encountered at 'Sever.run()'!");

        } finally {
            close();
        }
    }

    //Closes Server Socket.
    public void close () {
        try{
            if(serverSocket != null)
                serverSocket.close();

            System.out.println(">>> Sever closed.");
        }
        catch(IOException e) {
            System.out.println("ERROR: Method failure encountered at 'Sever.close()'.");
            System.exit(2);
        }
    }

    public static void main(String[] args) throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/messenger";
        String username = "postgres";
        String password = "1234";

        Database database = new Database(url, username, password);

        int port = 1234;

        Server server = new Server(port);
        server.run();
        database.close();
    }
}
