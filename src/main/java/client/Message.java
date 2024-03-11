package client;

public class Message {
    int pointer;
    String content;
    String sentTS, deliveredTS;

    Message(int pointer, String content) {
        this.pointer = pointer;
        this.content = content;
    }

    Message(int pointer, String content, String sentTS, String deliveredTS) {
        this.pointer = pointer;
        this.content = content;
        this.sentTS = sentTS;
        this.deliveredTS = deliveredTS;
    }

}
