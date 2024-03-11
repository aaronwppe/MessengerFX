package client;

import static client.CLI.client;
import static client.CLI.repository;

import java.util.ArrayList;
import java.util.LinkedList;

public class Chat {
    static ArrayList<Chat> chatList;
    int chatID;
    String username, name;
    LinkedList<Message> messageList;

    Chat (int id, String username, String name) {
        this.chatID = id;
        this.username = username;
        this.name = name;
    }

    boolean sendMessage(String content) {
        Message message = new Message(messageList.size() + 1, content);

        if (repository.insertMessage(message, chatID)) {
            client.write("SEND " + username + " " + message.pointer + "\n" + content);
            messageList.add(message);
            return true;

        } else
            return false;
    }

    static Chat getChatOf(String username) {
        for (Chat chat : chatList) {
            if (chat.username.equals(username))
                return chat;
        }

        return null;
    }

    String ackBlock (Message message) {
        return "ACK " + username + " " + message.pointer;
    }
}
