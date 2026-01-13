package ru. fixbyte;

import javafx.application.Application;
import javafx.application.Platform;
import javafx. fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage. Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Запуск приложения...");

            // Предотвращаем неявное завершение приложения
            Platform.setImplicitExit(true);

            // Загружаем FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/main.fxml")
            );

            System.out.println("FXML файл найден: " + (loader.getLocation() != null));

            Scene scene = new Scene(loader.load(), 800, 600);

            primaryStage.setTitle("Товарные чеки");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);

            // Обработчик закрытия окна
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Закрытие приложения...");
                Platform.exit();
                System.exit(0);
            });

            // Показываем окно
            primaryStage.show();

            // Выводим информацию об окне
            System.out.println("Окно открыто!");
            System.out.println("Размеры окна: " + primaryStage. getWidth() + "x" + primaryStage.getHeight());
            System.out.println("Окно видимо: " + primaryStage.isShowing());

            // Переводим окно на передний план
            primaryStage.toFront();
            primaryStage.requestFocus();

        } catch (Exception e) {
            System.err.println("ОШИБКА ЗАГРУЗКИ ПРИЛОЖЕНИЯ:");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert. setTitle("Ошибка");
            alert.setHeaderText("Не удалось запустить приложение");
            alert. setContentText(e.getMessage());
            alert. showAndWait();

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