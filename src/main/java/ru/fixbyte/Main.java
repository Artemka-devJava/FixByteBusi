package ru.fixbyte;

import javafx.application.Application;
import javafx. application.Platform;
import javafx. fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene. control.Alert;
import javafx.scene.image.Image;
import javafx. stage.Stage;
import ru. fixbyte.model.CompanySettings;

import java.io. InputStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Запуск приложения...");

            // Загружаем настройки при старте
            CompanySettings.loadSettings();

            Platform.setImplicitExit(true);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/main.fxml")
            );

            System.out. println("FXML файл найден:  " + (loader.getLocation() != null));

            // Применяем сохраненные размеры окна
            int windowWidth = CompanySettings. getWindowWidth();
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
                    System.out.println("Файл иконки не найден:   /icon.png");
                }
            } catch (Exception e) {
                System.err.println("Ошибка загрузки иконки:  " + e.getMessage());
                e.printStackTrace();
            }
            // ========================================

            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Закрытие приложения...");
                Platform.exit();
                System.exit(0);
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
            alert. setTitle("Ошибка");
            alert.setHeaderText("Не удалось запустить приложение");
            alert.setContentText(e.getMessage());
            alert.showAndWait();

            Platform. exit();
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