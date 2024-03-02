package client;

public class Message {
    int pointer;
    String senderUsername;
    String content;
    String deliveredTS;

    Message() {}

    String ackBlock () {
        return "ACK " + senderUsername + " " + pointer;
    }


}
