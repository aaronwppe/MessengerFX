package server;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Message {
    Integer pointer;
    String senderUsername, recipientUsername;
    String content;
    String sentTS, deliveredTS;
    Integer senderID, recipientID;
    Scanner scanner;
    ClientHandler senderHandler, recipientHandler;

    Message(Scanner scanner, ClientHandler senderHandler) {
        this.scanner = scanner;
        this.senderHandler = senderHandler;
    }

    Message(Integer senderID, String senderUsername, Integer pointer, String content) {
        this.senderID = senderID;
        this.senderUsername = senderUsername;
        this.pointer = pointer;
        this.content = content;
    }

    //since no scanner checks are made we throw exceptions.
    boolean processSEND() throws NoSuchElementException {
        senderID = senderHandler.user.id;
        senderUsername = senderHandler.user.username;

        recipientUsername = scanner.next();
        pointer = scanner.nextInt();

        //[IMPROVEMENT] define and get content length before read
        content = readContent();
        sentTS = Request.getTimestamp();

        recipientHandler = ClientHandler.getHandlerOf(recipientUsername);
        if (recipientHandler != null) {
            //recipient is online
            recipientID = recipientHandler.user.id;

            //[IMPROVEMENT] can be optimized if message is held in primary memory until DTS arrival
        }
        else if ((recipientID = Database.fetchUserID(recipientUsername)) == null) {
            //recipient does not exist
            senderHandler.write("DENIED " + recipientUsername);
            return false;
        }
        if (!Database.insert(this))
            return false;

        if (recipientHandler != null)
            recipientHandler.write(sentBlock());

        //reply with sent TIMESTAMP
        senderHandler.write(stsBlock());
        return true;
    }

    boolean processACK() throws Exception {
        //here recipient (originally sender of message) is the recipient of DTS
        recipientHandler = senderHandler;

        recipientID = recipientHandler.user.id;

        senderUsername = scanner.next();
        pointer = scanner.nextInt();
        deliveredTS = Request.getTimestamp();

        senderHandler = ClientHandler.getHandlerOf(senderUsername);
        if (senderHandler != null) {
            //sender is online
            senderID = senderHandler.user.id;

        } else if ((senderID = Database.fetchUserID(senderUsername)) == null) {
            //sender does not exist
            recipientHandler.write("DENIED " + senderUsername);
            return false;
        }

        if (!Database.updateDTS(this))
           return false;

        if (senderHandler != null)
            senderHandler.write(dtsBlock(recipientHandler.user.username));

        recipientHandler.write(dtsBlock(senderUsername));
        return true;
    }

    private String readContent() throws NoSuchElementException {
        scanner.nextLine(); //for the '\n' after username and before message

        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNextLine())
            stringBuilder.append(scanner.nextLine()).append("\n");

        //remove trailing '\n'
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }



    void forwardUndeliveredMessages () {

    }

    String sentBlock() {
        return "SENT " + senderUsername + " " + pointer + "\n" + content;
    }

    String stsBlock() {
        return "STS " + recipientUsername + " " + pointer + " " + sentTS;
    }

    String dtsBlock(String username) {
        return "DTS " + username + " " + pointer + " " + deliveredTS;
    }
}
