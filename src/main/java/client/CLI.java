package client;

public class CLI {
    static Client client;

    public static void main (String[] args) {
        String url = "jdbc:sqlite:chatRepository:db";
        ChatRepository repository = new ChatRepository(url);

        Chat chat = repository.getNewChat("@cooldeep", "Kuldeep Jangid", 0);

        Chat.list = repository.fetchChatList();
        for (Chat c: Chat.list) {
            System.out.println(c.id + " " + c.username);
        }

       // client = new Client("localhost", 1234);
/*
        if(!client.isConnected) {
            System.out.println("ERROR: Failed to establish connection with server!");
            System.exit(1);
        }

        if (!client.login("@cooldeep")) {
            System.out.println("ERROR: Login failed!");
            System.exit(1);
        }

        client.startListener();
        //client.write("SEND @aaronwppe 2\ntesting again beech!!");*/


    }
}
