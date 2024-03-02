package client;

import java.util.ArrayList;

public class Chat {
    static ArrayList<Chat> list = new ArrayList<>();
    int id, messagePointer;
    String username, name;

    Chat (int id, String username, String name, int messagePointer) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.messagePointer = messagePointer;

        list.add(this);
    }
}
