package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    public static ArrayList<ClientHandler> activeHandlers = new ArrayList<>();
    ArrayList<Message> dtsWaitList = new ArrayList<>();
    final private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    protected User user = null;
    private Request request = new Request(this);

    public ClientHandler (Socket socket) {
        this.socket = socket;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (IOException e) {
            System.out.println("ERROR: Failed to establish connection with client.");
        }
    }

    public synchronized boolean write (String message) {
        try {
            bufferedWriter.write(message + "\nEND");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    String read () {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();

            while (line == null)
                line = bufferedReader.readLine();

            while (!line.equals("END")) {
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }

            //remove trailing '\n'
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

            return stringBuilder.toString();

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void run () {
        String loginRequest = read();
        if(!request.authenticateClient(loginRequest)){
            close();
            return;
        }


        for (Message message: Database.fetchUndeliveredMessages(user.id)) {
            write(message.sentBlock());
        }

        while (!socket.isClosed()) {
            String requestString = read();

            if(requestString == null) {
                close();
                break;
            }

            System.out.println(requestString);
            if(request.service(requestString))
                System.out.println("REQUEST serviced!");
            else
                System.out.println("failed servicing");
        }
    }

    public void close () {
        write("CLOSE");
        activeHandlers.remove(this);

        try {
            if(bufferedReader != null)
                bufferedReader.close();

            if(bufferedWriter != null)
                bufferedWriter.close();

            if(socket != null)
                socket.close();

            if(user != null)
                System.out.println(">>> Client Handler " + user.username + " closed.");
            else
                System.out.println(">>> Client Handler [ANONYMOUS] closed.");

        } catch (IOException e) {
            System.out.println("ERROR: Unable to close ClientHandler.");
        }
    }

    static ClientHandler getHandlerOf(String username) {
        for (ClientHandler handler : activeHandlers) {
            if (handler.user.username.equals(username))
                return handler;
        }

        return null;
    }

     Message getMessageOf (String senderUsername) {
        //search is linear for the sake of message queues from same user
        for (Message message : dtsWaitList) {
            if (message.senderUsername.equals(senderUsername))
                return message;
        }

        return null;
    }
}