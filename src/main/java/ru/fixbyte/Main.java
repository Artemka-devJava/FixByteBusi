package ru.fixbyte;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.fixbyte.auth.LoginController;
import ru.fixbyte.model.CompanySettings;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

public class Main extends Application {

    private TrayIcon trayIcon;

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Запуск приложения...");

            // Загружаем настройки при старте
            CompanySettings.loadSettings();

            // ============ ПРОВЕРКА АВТОРИЗАЦИИ ============
            if (CompanySettings.isAuthEnabled()) {
                boolean authenticated = showLoginDialog();
                if (!authenticated) {
                    Platform.exit();
                    System.exit(0);
                    return;
                }
            }
            // =============================================

            Platform.setImplicitExit(false);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/main.fxml")
            );

            System.out.println("FXML файл найден: " + (loader.getLocation() != null));

            // Применяем сохраненные размеры окна
            int windowWidth = CompanySettings.getWindowWidth();
            int windowHeight = CompanySettings.getWindowHeight();

            Scene scene = new Scene(loader.load(), windowWidth, windowHeight);

            primaryStage.setTitle("Товарные чеки");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);

            // Устанавливаем минимальные размеры
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);

            // ============ УСТАНОВКА ИКОНКИ ============
            try {
                InputStream iconStream = getClass().getResourceAsStream("/logo.png");
                if (iconStream != null) {
                    Image icon = new Image(iconStream);
                    primaryStage.getIcons().add(icon);
                    System.out.println("Иконка приложения загружена успешно");
                } else {
                    System.out.println("Файл иконки не найден: /logo.png");
                }
            } catch (Exception e) {
                System.err.println("Ошибка загрузки иконки: " + e.getMessage());
            }
            // ========================================

            // ============ СИСТЕМНЫЙ ТРЕЙ ============
            initSystemTray(primaryStage);
            // ========================================

            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                primaryStage.hide();
                if (trayIcon != null) {
                    trayIcon.displayMessage("FixByteBusi",
                            "Приложение свёрнуто в трей", TrayIcon.MessageType.INFO);
                }
            });

            primaryStage.show();

            System.out.println("Окно открыто!");
            System.out.println("Размеры окна: " + windowWidth + "x" + windowHeight);

            primaryStage.toFront();
            primaryStage.requestFocus();

        } catch (Exception e) {
            System.err.println("ОШИБКА ЗАГРУЗКИ ПРИЛОЖЕНИЯ:");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось запустить приложение");
            alert.setContentText(e.getMessage());
            alert.showAndWait();

            Platform.exit();
        }
    }

    private boolean showLoginDialog() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Scene loginScene = new Scene(loader.load());
        LoginController controller = loader.getController();

        Stage loginStage = new Stage();
        loginStage.setTitle("Вход");
        loginStage.setScene(loginScene);
        loginStage.setResizable(false);
        loginStage.initModality(Modality.APPLICATION_MODAL);

        try {
            InputStream iconStream = getClass().getResourceAsStream("/logo.png");
            if (iconStream != null) {
                loginStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        loginStage.showAndWait();
        return controller.isLoginSuccess();
    }

    private void initSystemTray(Stage primaryStage) {
        if (!SystemTray.isSupported()) {
            System.out.println("Системный трей не поддерживается");
            return;
        }

        try {
            BufferedImage awtImage = null;
            URL iconUrl = getClass().getResource("/logo.png");
            if (iconUrl != null) {
                try (InputStream is = iconUrl.openStream()) {
                    awtImage = ImageIO.read(is);
                } catch (Exception ignored) {}
            }
            if (awtImage == null) {
                awtImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            }

            PopupMenu popup = new PopupMenu();

            MenuItem openItem = new MenuItem("Открыть");
            openItem.addActionListener(e -> Platform.runLater(() -> {
                primaryStage.show();
                primaryStage.toFront();
                primaryStage.requestFocus();
            }));

            MenuItem aboutItem = new MenuItem("О программе");
            aboutItem.addActionListener(e -> Platform.runLater(() -> {
                primaryStage.show();
                primaryStage.toFront();
                Alert about = new Alert(Alert.AlertType.INFORMATION);
                about.setTitle("О программе");
                about.setHeaderText("FixByteBusi");
                about.setContentText("Система управления бизнесом FixByteBusi");
                about.showAndWait();
            }));

            MenuItem exitItem = new MenuItem("Выход");
            exitItem.addActionListener(e -> {
                SystemTray.getSystemTray().remove(trayIcon);
                Platform.runLater(() -> {
                    Platform.exit();
                    System.exit(0);
                });
            });

            popup.add(openItem);
            popup.add(aboutItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon = new TrayIcon(awtImage, "FixByteBusi", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(() -> {
                primaryStage.show();
                primaryStage.toFront();
                primaryStage.requestFocus();
            }));

            SystemTray.getSystemTray().add(trayIcon);
            System.out.println("Иконка в трее добавлена");
        } catch (AWTException e) {
            System.err.println("Ошибка добавления иконки в трей: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        System.out.println("Приложение останавливается...");
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    public static void main(String[] args) {
        System.out.println("Старт main()...");
        launch(args);
        System.out.println("Приложение завершено.");
    }
}