package client;

import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.LinkedList;

import static ui.messengerfx.Messenger.client;
import static ui.messengerfx.Messenger.repository;

public class Chat {
    public static ArrayList<Chat> chatList;
    static volatile boolean searchComplete;
    static Chat searchedChat;
    int chatID;
    public String username, name;
    public LinkedList<Message> messageList;
    public VBox vBox;

    public Chat(int id, String username, String name) {
        this.chatID = id;
        this.username = username;
        this.name = name;
    }

    public boolean sendMessage(String content) {
        int pointer = 1;
        if(!messageList.isEmpty())
            pointer = messageList.getLast().pointer + 1;

        Message message = new Message(Message.Type.SENDER, pointer, content);

        if (repository.insertMessage(message, chatID)) {
            client.write("SEND " + username + " " + message.pointer + "\n" + content);
            messageList.add(message);
            return true;
        } else
            return false;
    }

    public static Chat getChatOf(String username) {
        for (Chat chat : chatList) {
            if (chat.username.equals(username))
                return chat;
        }

        return null;
    }

    Message getMessageOf(int messagePointer) {
        for (Message message : messageList) {
            if (message.pointer == messagePointer) {
                return message;
            }
        }

        return null;
    }

    public static Chat openChat(String username) {
        Chat chat;
        if ((chat = getChatOf(username)) != null)
            return chat;

        searchComplete = false;
        client.write("OPEN " + username);

        //waiting for REPLY
        while (!searchComplete)
            Thread.onSpinWait();

        chatList.add(searchedChat);
        return searchedChat;
    }

    String ackBlock (Message message) {
        return "ACK " + username + " " + message.pointer;
    }
}
