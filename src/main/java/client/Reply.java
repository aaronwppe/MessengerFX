package client;

import javafx.application.Platform;
import ui.messengerfx.MainWindowController;

import static client.Chat.chatList;
import static ui.messengerfx.Messenger.client;
import static ui.messengerfx.Messenger.repository;
import static ui.messengerfx.Messenger.mainWindowController;

import java.util.Scanner;

public class Reply {
    Scanner scanner;
    Reply () {}

    boolean process (String block) {
        try(Scanner scanner = new Scanner(block)) {
            this.scanner = scanner;

            switch (scanner.next()) {
                case "SENT" -> {
                    return getMessage();
                }

                case "STS" -> {
                    return getSTS();
                }

                case "DTS" -> {
                    return getDTS();
                }

                case "USER" -> {
                    return getUSER();
                }

                case "CLOSE" -> {
                    return client.close();
                }
                default -> {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

    }

    boolean getUSER() throws Exception {
        String username = scanner.next();
        if (username.equals("DENIED")) {
            Chat.searchedChat = null;
            Chat.searchComplete = true;
            return false;
        }

        String name = scanner.nextLine();
        int pointer = scanner.nextInt();

        Chat.searchedChat = repository.openNewChat(username, name);
        Chat.searchComplete = true;
        return true;
    }

    boolean getSTS () throws Exception {
        String recipientUsername = scanner.next();
        int pointer = scanner.nextInt();
        String sentTS = scanner.next() + " " + scanner.next();

        Chat chat = Chat.getChatOf(recipientUsername);
        if (chat == null)
            return false;

        Message message = chat.messageList.get(pointer - 1);
        message.sentTS = sentTS;
        Platform.runLater(() ->message.timestampLabel.setText(message.getSTS()));

        repository.insertSentTS(message, chat.chatID);

        return true;
    }

    boolean getDTS () throws Exception {
        String username = scanner.next();
        int pointer = scanner.nextInt();
        String deliveredTS = scanner.next() + " " + scanner.next();

        Chat chat = Chat.getChatOf(username);
        if(chat == null) {
            client.write("DENIED " + username);
            return false;
        }

        Message message = chat.getMessageOf(pointer);
        message.deliveredTS = deliveredTS;
        repository.insertDeliveredTS(message, chat.chatID);

        String timestamp;
        if(message.type == Message.Type.SENDER)
            timestamp = "Delivered " + message.getDTS();
        else
            timestamp = message.getDTS();

        System.out.println("a dts ts: "+timestamp);
        Platform.runLater(() -> message.timestampLabel.setText(timestamp));
        return true;
    }

    boolean getMessage () throws Exception{
        String senderUsername = scanner.next();
        int pointer = scanner.nextInt();
        String content = readContent();

        Chat chat = Chat.getChatOf(senderUsername);
        if(chat == null) {
            return false;
            //if((chat = Chat.openChat(senderUsername)) == null)

        }
        //mainWindowController.chatList.add(0, chat);
        /*
        if(chat.messageList.size() >= pointer) {
            client.write("PTR " + senderUsername + " " + chat.messageList.size());
            return false;
        }*/

        Message message = new Message(Message.Type.RECIPIENT, pointer, content);
        chat.messageList.add(message);
        client.write(chat.ackBlock(message));

        repository.insertMessage(message, chat.chatID);

        Platform.runLater(() ->mainWindowController.addLeftBubble(senderUsername, message));

        return true;
    }

    private String readContent() throws Exception {
        scanner.nextLine(); //for the '\n' after username and before message

        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNextLine())
            stringBuilder.append(scanner.nextLine()).append("\n");

        //remove trailing '\n'
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }
}
