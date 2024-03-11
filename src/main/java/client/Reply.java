package client;

import static client.CLI.client;
import static client.CLI.repository;

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

    boolean getSTS () throws Exception {
        String recipientUsername = scanner.next();
        int pointer = scanner.nextInt();
        String sentTS = scanner.next() + " " + scanner.next();

        Chat chat = Chat.getChatOf(recipientUsername);
        if (chat == null)
            return false;

        Message message = chat.messageList.get(pointer - 1);
        message.sentTS = sentTS;

        return repository.updateSTS(message, chat.chatID);
    }

    boolean getMessage () throws Exception {
        String senderUsername = scanner.next();

        Chat chat = Chat.getChatOf(senderUsername);
        if(chat == null) {
            client.write("DENIED " + senderUsername);
            return false;
        }

        int pointer = scanner.nextInt();
        if(chat.messageList.size() >= pointer) {
            client.write("PTR " + senderUsername + " " + chat.messageList.size());
            return false;
        }

        String content = readContent();

        Message message = new Message(pointer, content);
        chat.messageList.add(message);
        client.write(chat.ackBlock(message));

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
