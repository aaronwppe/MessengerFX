package client;

import static java.lang.Thread.sleep;

public class CLI {
    static Client client;
    static ChatRepository repository;

    public static void main (String[] args) throws Exception {
        repository = new ChatRepository("jdbc:sqlite:chatRepository:db");
        if(!repository.isConnected)
            close(1);

        client = new Client("localhost", 1234);
        if(!client.isConnected)
            close(2);

        if(!client.login("@cooldeep", "1234"))
            close(3);

        //------>START

        Chat.chatList = repository.fetchChatList();

        Chat chat = repository.openNewChat("@aaronwppe", "Aaron Mathew");
        if(chat == null)
            close(4);
        Chat.chatList.add(chat);


        chat.sendMessage("hello");
        client.startListener();

        sleep(1000);
        for (Chat c: repository.fetchChatList()) {
            System.out.print(c.username + " " + c.messageList.size() + " ");
            for (Message m: c.messageList)
                System.out.print(" " + m.pointer + " " + m.content + " " + m.sentTS);

            System.out.println();
        }
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
