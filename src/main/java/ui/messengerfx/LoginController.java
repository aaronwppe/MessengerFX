package ui.messengerfx;

import static ui.messengerfx.Messenger.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Label loginSuccessLabel;
    @FXML
    private Label loginFailedLabel;

    @FXML
    protected void onLoginButtonClick () {
        boolean isAuthenticated = client.login(usernameTextField.getText(), passwordTextField.getText());

        if (isAuthenticated)
            loginSuccessLabel.setVisible(true);
        else
            loginFailedLabel.setVisible(true);
    }
}
