package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Scanner;

class Request {
    ClientHandler senderHandler;

    Request (ClientHandler handler) {
        this.senderHandler = handler;
    }
    boolean service (String request) {
        String reply;
        Message message;
        try (Scanner scanner = new Scanner(request)) {
            switch (scanner.next()) {
                case "LOGIN":
                    return true;

                case "SEND":
                    //SEND @username message_pointer \n content
                    message = new Message(scanner, senderHandler);
                    return message.processSEND();

                case "ACK":
                    //ACK @username message_pointer
                    message = new Message(scanner, senderHandler);
                    return message.processACK();

                case "PULL":
                    return false;

                case "CLOSE":
                    senderHandler.close();
                    return true;

                default:
                    return false;
            }

        } catch (Exception e) {
            //since no scanner checks have been made we use exceptions.
            return false;
        }
    }

    boolean authenticateClient (String loginRequest) {
        try (Scanner scanner = new Scanner(loginRequest)) {
            if (!scanner.next().equals("LOGIN")) {
                senderHandler.write("DENIED");
                return false;
            }
            String username = scanner.next();
            String password = scanner.next();

            //[IMPROVEMENT] multi login by same user is unsafe
            if (ClientHandler.getHandlerOf(username) != null) {
                senderHandler.write("ACTIVE");
                System.out.println("Active");
                return true;
            }

            User user = Database.fetchUser(username, password);
            if (user == null) {
                System.out.println("failed");
                senderHandler.write("FAILED");
                return false;
            }

            senderHandler.user = user;
            ClientHandler.activeHandlers.add(senderHandler);

            senderHandler.write("AUTHENTICATED");
            System.out.println("auth");
            return true;


        } catch (NoSuchElementException e) {

            return false;
        }

    }
    static String getTimestamp() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}

