package ui.messengerfx;

import client.Chat;
import client.ChatRepository;
import client.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Messenger extends Application {
    static String user = "kuldeep";
    public static Client client = new Client("localhost", 1234);
    public static ChatRepository repository = new ChatRepository("jdbc:sqlite:chatRepository-" + user + ":db");
    public static MainWindowController mainWindowController;


    @Override
    public void start(Stage stage) throws IOException {
        Chat.chatList = repository.fetchChatList();

        stage.setOnCloseRequest(event -> {
            client.close();
            Platform.exit();
        });

        LoginController loginController = loginWindow(stage);

        loginController.setLoginSuccessCallback(() -> {
            try {
                mainWindowController = mainWindow(stage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // mainWindowController = mainWindow(stage);
    }

    MainWindowController mainWindow(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Messenger.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle(user);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        double width = bounds.getWidth() * 0.55;
        double height = bounds.getHeight() * 0.80;

        stage.setWidth(width);
        stage.setHeight(height);
        stage.show();

        return fxmlLoader.getController();
    }

    LoginController loginWindow(Stage stage) throws IOException {
        //  if (!Messenger.client.isConnected)
        // return; //'unable to connect to server' fxml in a while loop

        FXMLLoader fxmlLoader = new FXMLLoader(Messenger.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 340, 340);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle(user);
        stage.setScene(scene);
        stage.show();

        return fxmlLoader.getController();

    }
    public static void main(String[] args) {
        launch();
    }
}