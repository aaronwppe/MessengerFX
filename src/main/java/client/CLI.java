package client;

import java.util.Scanner;

import static java.lang.Thread.sleep;

public class CLI {
    static Client client;
    static ChatRepository repository;

    public static void main (String[] args) throws Exception {
        repository = new ChatRepository("jdbc:sqlite:chatRepository-aaron:db");
        if(!repository.isConnected)
            close(1);

        client = new Client("localhost", 1234);
        if(!client.isConnected)
            close(2);

        if(!client.login("@aaronwppe", "1234"))
            close(3);

        client.startListener();
        Chat c = Chat.openChat("@cooldeep");
        if(c == null)
            System.out.println("nope");
        else
            System.out.println(c.name);
/*
        //------>START

        Chat.chatList = repository.fetchChatList();

        String username = "@ovenTEA";
        Chat chat = repository.openNewChat(username, "aaron");
        if(chat == null)
            chat = Chat.getChatOf(username);

        if(chat == null)
            System.exit(5);

        client.startListener();

        Scanner scanner = new Scanner(System.in);
        String message;
        while (!(message = scanner.nextLine()).equals("CLOSE")){
            chat.sendMessage(message);
        }

        sleep(1000);
        for (Chat c: repository.fetchChatList()) {
            System.out.println(c.username + " " + c.messageList.size());
            for (Message m: c.messageList)
                System.out.println(m.pointer + " " + m.content + " " + m.type + " " + m.sentTS + " " + m.deliveredTS);

        }*/
        close(0);
    }

    static void close(int status) {
        if(repository != null)
            repository.close();

        if(client != null)
            client.close();

        System.exit(status);
    }
}
