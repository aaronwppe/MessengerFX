package client;

import static ui.messengerfx.Messenger.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Reply {
    Scanner scanner;

    Reply (String block) {
        scanner = new Scanner(block);
        System.out.println(block);
    }

    boolean process () {
        boolean status = true;

        if (!scanner.hasNext()) {
            scanner.close();
            return false;
        }

        try {
            switch (scanner.next()) {
                case "SENT":
                    getMessage();
                    break;

                case "CLOSE":
                    client.close();
                    break;

                default:
                    status = false;
            }
        } catch (Exception e) {
            status = false;
        }

        if(scanner != null)
            scanner.close();
        return status;
    }

    boolean getMessage () throws Exception {
        Message message = new Message();

        message.senderUsername = scanner.next();
        message.pointer = scanner.nextInt();
        message.content = readContent();

        client.write(message.ackBlock());

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

    static String getTimestamp() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
