package ru.fixbyte;

import javafx.application.Application;
import javafx.application.Platform;
import javafx. fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx. scene.image.Image;
import javafx. stage.Stage;
import ru.fixbyte.model.CompanySettings;

import java.io. InputStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Запуск приложения...");

            // Загружаем настройки при старте
            CompanySettings.loadSettings();

            // ============ УСТАНАВЛИВАЕМ ИКОНКУ ПРИЛОЖЕНИЯ ============
            try {
                InputStream iconStream = getClass().getResourceAsStream("/logo.png");
                if (iconStream != null) {
                    Image icon = new Image(iconStream);
                    primaryStage.getIcons().add(icon);
                    System.out.println("Иконка приложения загружена");
                } else {
                    System.out.println("Иконка не найдена:  /icon.png");
                }
            } catch (Exception e) {
                System.err.println("Ошибка загрузки иконки: " + e.getMessage());
            }
            // ========================================================

            Platform.setImplicitExit(true);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/main.fxml")
            );

            System.out.println("FXML файл найден:  " + (loader.getLocation() != null));

            Scene scene = new Scene(loader.load(), 800, 600);

            primaryStage.setTitle("Товарные чеки");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);

            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Закрытие приложения...");
                Platform.exit();
                System.exit(0);
            });

            primaryStage.show();

            System.out.println("Окно открыто!");
            primaryStage.toFront();
            primaryStage.requestFocus();

        } catch (Exception e) {
            System.err.println("ОШИБКА ЗАГРУЗКИ ПРИЛОЖЕНИЯ:");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert. setTitle("Ошибка");
            alert.setHeaderText("Не удалось запустить приложение");
            alert.setContentText(e. getMessage());
            alert.showAndWait();

            Platform.exit();
        }
    }

    @Override
    public void stop() {
        System.out.println("Приложение останавливается...");
    }

    public static void main(String[] args) {
        System.out.println("Старт main()...");
        launch(args);
        System.out.println("Приложение завершено.");
    }
}