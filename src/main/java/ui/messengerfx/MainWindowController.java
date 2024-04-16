package ui.messengerfx;

import client.Chat;
import client.Message;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.collections.FXCollections;

import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {
    @FXML
    public Button sendButton;
    @FXML
    public ScrollPane mainScrollPane;
    @FXML
    public TextField messageTextfield;
    @FXML
    public VBox messagesVBox;
    @FXML
    public ListView<Chat> chatListView;
    @FXML
    public TextField searchTextField;
    @FXML
    public Label chatNameTextField;
    @FXML
    public Button searchButton;
    public ObservableList<Chat> chatList;
    Chat chat;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messagesVBox.heightProperty().addListener((observableValue, oldValue, newValue) -> mainScrollPane.setVvalue((Double) newValue));
        setupChatListView();
        setupSearchButton();
        setupSendButton();
    }

    void setupChatListView() {
        chatList = FXCollections.observableArrayList(Chat.chatList);
        chatListView.setItems(chatList);

        for (Chat chat: chatList) {
            chat.vBox = new VBox(messagesVBox.getSpacing());

            for (Message message: chat.messageList) {
                if(message.type == Message.Type.SENDER)
                    addRightBubble(message, chat.vBox);
                else
                    addLeftBubble(message, chat.vBox);
            }
        }

        chatListView.setCellFactory(chatListView -> new ListCell<>() {
            @Override
            protected void updateItem(Chat chat, boolean empty) {
                super.updateItem(chat, empty);
                if (empty || chat == null) {
                    setText(null);
                } else {
                    setText(chat.name);
                }
            }
        });

        chatListView.getSelectionModel().selectedItemProperty().addListener((observable, currentChat, selectedChat) -> {
            chatNameTextField.setText(selectedChat.name);

            messageTextfield.clear();
            chat = selectedChat;

            if (currentChat != null) {
                //optimization recommended dont create new vbox if not null
                currentChat.vBox = new VBox(messagesVBox.getSpacing());
                currentChat.vBox.getChildren().addAll(messagesVBox.getChildren());
            }

            messagesVBox.getChildren().clear();

            if(selectedChat.vBox != null)
                messagesVBox.getChildren().addAll(selectedChat.vBox.getChildren());
        });

        chatListView.focusedProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> messageTextfield.requestFocus());
        });
    }

     void setTimestamp(Message message, VBox vBox) {
        HBox timestamp = new HBox();

        if(message.type == Message.Type.SENDER) {
            timestamp.setAlignment(Pos.CENTER_RIGHT);
            timestamp.setPadding(new Insets(0, 10, 0, 0));

            if (message.deliveredTS == null && message.sentTS == null)
                message.timestampLabel.setText("Not Sent");
            else if (message.deliveredTS == null)
                message.timestampLabel.setText(message.getSTS());
            else
                message.timestampLabel.setText("Delivered " + message.getDTS());
        }
        else {
            timestamp.setAlignment(Pos.CENTER_LEFT);
            timestamp.setPadding(new Insets(0, 0, 0, 10));

            if(message.deliveredTS != null)
                message.timestampLabel.setText(message.getDTS());
        }

        timestamp.getChildren().add(message.timestampLabel);
        timestamp.setVisible(true);

        vBox.getChildren().add(timestamp);
    }

    void setupSearchButton() {
        searchButton.setOnAction(actionEvent -> {
            String searchString = searchTextField.getText();

            if (searchString.isEmpty())
                return;

            Chat chat = Chat.openChat(searchString);
            if(chat == null)
                return;

            chatList.add(0, chat);
            //chatListView.setItems(chatList);
        });
    }

    void setupSendButton() {
        sendButton.setOnAction(actionEvent -> {
            String messageToSend = messageTextfield.getText();

            if (!messageToSend.isEmpty()) {
                chat.sendMessage(messageToSend);
                messageTextfield.clear();

                addRightBubble(chat.messageList.getLast(), messagesVBox);
            }
        });
    }

    public void addRightBubble(Message message, VBox vBox) {
        Text text = new Text(message.content);
        text.setFill(Color.color(1, 1, 1));
        text.setFont(new Font(15));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(10, 15, 10, 15));
        textFlow.setStyle("-fx-background-color: rgb(15, 125, 242);" +
                "-fx-background-radius: 20px");

        HBox messageBubble = new HBox();
        messageBubble.setAlignment(Pos.CENTER_RIGHT);
        messageBubble.setPadding(new Insets(5, 5, 5, 10));
        messageBubble.getChildren().add(textFlow);

        vBox.getChildren().add(messageBubble);
        setTimestamp(message, vBox);
    }

    public void addLeftBubble (String senderUsername, Message message) {
        Text text = new Text(message.content);
        text.setFill(Color.color(0, 0, 0));
        text.setFont(new Font(15));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(10, 15, 10, 15));
        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" +
                "-fx-background-radius: 20px");

        HBox messageBubble = new HBox();
        messageBubble.setAlignment(Pos.CENTER_LEFT);
        messageBubble.setPadding(new Insets(5, 5, 5, 10));
        messageBubble.getChildren().add(textFlow);

        if(senderUsername.equals(chat.username)) {
            messagesVBox.getChildren().add(messageBubble);
            setTimestamp(message, messagesVBox);
            return;
        }

        for (Chat chat : chatList) {
            if (chat.username.equals(senderUsername)) {
                if (chat.vBox == null)
                    chat.vBox = new VBox(messagesVBox.getSpacing());

                chat.vBox.getChildren().add(messageBubble);
                setTimestamp(message, chat.vBox);
            }
        }


    }

    public void addLeftBubble (Message message, VBox vBox) {
        Text text = new Text(message.content);
        text.setFill(Color.color(0, 0, 0));
        text.setFont(new Font(15));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(10, 15, 10, 15));
        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" +
                "-fx-background-radius: 20px");

        HBox messageBubble = new HBox();
        messageBubble.setAlignment(Pos.CENTER_LEFT);
        messageBubble.setPadding(new Insets(5, 5, 5, 10));
        messageBubble.getChildren().add(textFlow);

        vBox.getChildren().add(messageBubble);

        setTimestamp(message, vBox);
    }
}
