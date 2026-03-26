package ru.fixbyte;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import ru.fixbyte.model.CompanySettings;

import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.InputStream;

public class Main extends Application {

    private TrayIcon trayIcon;

    @Override
    public void start(Stage primaryStage) {
        try {
            CompanySettings.loadSettings();

            if (!authenticateOnStart()) {
                Platform.exit();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            int windowWidth = CompanySettings.getWindowWidth();
            int windowHeight = CompanySettings.getWindowHeight();

            Scene scene = new Scene(loader.load(), windowWidth, windowHeight);
            primaryStage.setTitle("Товарные чеки");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);

            setJavaFxIcon(primaryStage);
            setupSystemTray(primaryStage);

            primaryStage.setOnCloseRequest(event -> {
                if (trayIcon != null) {
                    event.consume();
                    primaryStage.hide();
                    trayIcon.displayMessage("FixByteBusi", "Приложение свернуто в трей", TrayIcon.MessageType.INFO);
                } else {
                    Platform.exit();
                    System.exit(0);
                }
            });

            primaryStage.show();
            primaryStage.toFront();
            primaryStage.requestFocus();

        } catch (Exception e) {
            System.err.println("Ошибка загрузки приложения: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось запустить приложение");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
        }
    }

    private boolean authenticateOnStart() {
        if (!CompanySettings.isAuthEnabled()) {
            return true;
        }

        while (true) {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Вход в систему");
            dialog.setHeaderText("Введите логин и пароль");

            ButtonType loginButtonType = new ButtonType("Войти", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 20));

            TextField loginField = new TextField();
            loginField.setPromptText("Логин");
            loginField.setText(CompanySettings.getAuthLogin());

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Пароль");

            grid.add(new Label("Логин:"), 0, 0);
            grid.add(loginField, 1, 0);
            grid.add(new Label("Пароль:"), 0, 1);
            grid.add(passwordField, 1, 1);

            dialog.getDialogPane().setContent(grid);
            Platform.runLater(passwordField::requestFocus);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    return new Pair<>(loginField.getText(), passwordField.getText());
                }
                return null;
            });

            var result = dialog.showAndWait();
            if (result.isEmpty()) {
                return false;
            }

            String login = result.get().getKey();
            String password = result.get().getValue();
            if (CompanySettings.verifyCredentials(login, password)) {
                return true;
            }

            Alert error = new Alert(Alert.AlertType.ERROR, "Неверный логин или пароль", ButtonType.OK);
            error.setHeaderText(null);
            error.setTitle("Ошибка авторизации");
            error.showAndWait();
        }
    }

    private void setJavaFxIcon(Stage primaryStage) {
        try {
            InputStream iconStream = getClass().getResourceAsStream("/logo.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки иконки: " + e.getMessage());
        }
    }

    private void setupSystemTray(Stage primaryStage) {
        if (!SystemTray.isSupported()) {
            Platform.setImplicitExit(true);
            return;
        }

        try {
            Platform.setImplicitExit(false);

            PopupMenu popup = new PopupMenu();
            java.awt.MenuItem openItem = new java.awt.MenuItem("Открыть");
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Выход");

            openItem.addActionListener(e -> Platform.runLater(() -> {
                primaryStage.show();
                primaryStage.toFront();
            }));

            exitItem.addActionListener(e -> {
                SystemTray.getSystemTray().remove(trayIcon);
                Platform.exit();
                System.exit(0);
            });

            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);

            java.awt.Image awtImage = null;
            try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
                if (is != null) {
                    awtImage = Toolkit.getDefaultToolkit().createImage(is.readAllBytes());
                }
            }

            if (awtImage == null) {
                Platform.setImplicitExit(true);
                return;
            }

            trayIcon = new TrayIcon(awtImage, "FixByteBusi", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(() -> {
                primaryStage.show();
                primaryStage.toFront();
            }));

            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception e) {
            System.err.println("Не удалось инициализировать системный трей: " + e.getMessage());
            trayIcon = null;
            Platform.setImplicitExit(true);
        }
    }

    @Override
    public void stop() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        System.out.println("Приложение завершено.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}