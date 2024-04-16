package client;

import javafx.scene.control.Label;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    int pointer;
    public String content;
    public Type type;
    public String sentTS, deliveredTS;
    public Label timestampLabel = new Label("");

    Message(Type type, int pointer, String content) {
        this.type = type;
        this.pointer = pointer;
        this.content = content;
    }

    Message(Type type, int pointer, String content, String sentTS, String deliveredTS) {
        this.type = type;
        this.pointer = pointer;
        this.content = content;
        this.sentTS = sentTS;
        this.deliveredTS = deliveredTS;
    }

    public String getSTS(){
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");

        try {
            Date time = inputFormat.parse(sentTS);
            return outputFormat.format(time).toUpperCase();
        } catch (ParseException e) {
            return "";
        }
    }

    public String getDTS(){
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");

        try {
            Date time = inputFormat.parse(deliveredTS);
            return outputFormat.format(time).toUpperCase();
        } catch (ParseException e) {
            return "";
        }
    }

    public enum Type {
        SENDER,
        RECIPIENT
    }
}
