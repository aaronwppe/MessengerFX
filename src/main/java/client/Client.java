package client;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public boolean isConnected;

    public Client (String host, int port) {
        try {
            socket = new Socket(host, port);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            isConnected = true;

        } catch (IOException e) {
            isConnected = false;
        }
    }

    public boolean login (String username, String password) {
        String request = "LOGIN " + username +  " " + password;
        write(request);

        String status = read();

        if (!status.equals("AUTHENTICATED")) {
            return false;
        }

        startListener();
        return true;
    }
    synchronized void write (String message) {
        try {
            bufferedWriter.write(message + "\nEND");
            bufferedWriter.newLine();
            bufferedWriter.flush();

        } catch (Exception e) {
            System.out.println("write not possible");
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

    //only this listener will close
    public void startListener () {
        new Thread(() -> {
            Reply reply = new Reply();
            while(!socket.isClosed()) {
                String block = read();
                if(reply.process(block))
                    System.out.println("reply processed successfully");
                else
                    System.out.println("reply processing failed");
            }
        }).start();
    }

    boolean close () {
        isConnected = false;
        try {
            if(bufferedReader != null)
                bufferedReader.close();

            if(bufferedWriter != null)
                bufferedWriter.close();

            if(socket != null)
                socket.close();

            System.out.println(">>> Client closed.");
            return true;

        } catch (IOException e) {
            System.out.println("ERROR: Unable to close Client.");
            return false;
        }
    }
}
