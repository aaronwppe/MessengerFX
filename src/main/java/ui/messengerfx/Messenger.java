package ui.messengerfx;

import client.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Messenger extends Application {
    public static Client client = new Client("localhost", 1234);

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setOnCloseRequest(event -> Platform.exit());
        openLoginWindow(stage);
    }

    void openLoginWindow(Stage stage) throws IOException {
        if (!client.isConnected)
            return; //'unable to connect to server' fxml in a while loop

        FXMLLoader fxmlLoader = new FXMLLoader(Messenger.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 340, 340);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle("Messenger");
        stage.setScene(scene);
        stage.show();
    }
}