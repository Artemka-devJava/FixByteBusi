package ru.fixbyte.auth;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.fixbyte.model.CompanySettings;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private int attempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    private boolean loginSuccess = false;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void onLogin() {
        String enteredLogin = loginField.getText();
        String enteredPassword = passwordField.getText();

        String storedLogin = CompanySettings.getLogin();
        String storedHash = CompanySettings.getPasswordHash();

        boolean loginMatch = storedLogin.equals(enteredLogin);
        boolean passwordMatch;

        if (storedHash == null || storedHash.isEmpty()) {
            // Пароль не установлен — пускаем без проверки пароля
            passwordMatch = true;
        } else {
            passwordMatch = storedHash.equals(CompanySettings.hashPassword(enteredPassword));
        }

        if (loginMatch && passwordMatch) {
            loginSuccess = true;
            closeStage();
        } else {
            attempts++;
            int remaining = MAX_ATTEMPTS - attempts;
            if (remaining <= 0) {
                errorLabel.setText("Превышено количество попыток. Выход.");
                errorLabel.setVisible(true);
                PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                pause.setOnFinished(e -> {
                    Platform.exit();
                    System.exit(0);
                });
                pause.play();
            } else {
                errorLabel.setText("Неверный логин или пароль. Осталось попыток: " + remaining);
                errorLabel.setVisible(true);
            }
            passwordField.clear();
        }
    }

    @FXML
    private void onCancel() {
        Platform.exit();
        System.exit(0);
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    private void closeStage() {
        Stage stage = (Stage) loginField.getScene().getWindow();
        stage.close();
    }
}
