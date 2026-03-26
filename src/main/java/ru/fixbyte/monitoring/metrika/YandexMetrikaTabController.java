package ru.fixbyte.monitoring.metrika;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class YandexMetrikaTabController {
    private final YandexMetrikaService metrikaService;
    private final Label visitsLabel = new Label("Визиты");
    private final Label pageviewsLabel = new Label("Просмотры");
    private final Label usersLabel = new Label("Пользователи");
    private final Label callsLabel = new Label("Звонки");
    private final Label dateLabel = new Label("Дата");
    public final Tab tab;

    public YandexMetrikaTabController(YandexMetrikaService metrikaService) {
        this.metrikaService = metrikaService;

        // Создаём "карточку"
        VBox statsBox = new VBox(9,
                makeStatBox(visitsLabel, "#81ecec"),
                makeStatBox(pageviewsLabel, "#ffeaa7"),
                makeStatBox(usersLabel, "#a29bfe"),
                makeStatBox(callsLabel, "#fab1a0")
        );
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(16));

        Label headerLabel = new Label("Статистика Яндекс.Метрики");
        headerLabel.setFont(Font.font("Arial", 20));
        headerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3436;");

        dateLabel.setFont(Font.font("Arial", 14));
        dateLabel.setStyle("-fx-text-fill: #636e72;");

        Button refreshBtn = new Button("Обновить");
        refreshBtn.setStyle(
                "-fx-background-radius:8; -fx-background-color: #00b894; -fx-font-size: 14; -fx-text-fill:white;"
        );
        refreshBtn.setMaxWidth(Double.MAX_VALUE);

        VBox kanbanCard = new VBox(14, headerLabel, dateLabel, statsBox, refreshBtn);
        kanbanCard.setStyle(
                "-fx-background-radius: 12; " +
                        "-fx-background-color: #f5f6fa; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-color: #dfe6e9; " +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 10, 0, 0, 4);" +
                        "-fx-min-width: 320px; -fx-max-width: 370px;"
        );
        kanbanCard.setPadding(new Insets(24));
        kanbanCard.setAlignment(Pos.TOP_CENTER);

        this.tab = new Tab("Яндекс Метрика", kanbanCard);

        refreshBtn.setOnAction(e -> updateStats());
        updateStats();
    }

    // Обновление статистики с сервера
    private void updateStats() {
        visitsLabel.setText("Визиты: ...");
        pageviewsLabel.setText("Просмотры: ...");
        usersLabel.setText("Пользователи: ...");
        callsLabel.setText("Звонки: ...");
        dateLabel.setText("Дата: ...");
        new Thread(() -> {
            try {
                MetrikaStats stats = metrikaService.getTodayStats();
                Platform.runLater(() -> {
                    visitsLabel.setText("Визиты: " + stats.visits);
                    pageviewsLabel.setText("Просмотры: " + stats.pageviews);
                    usersLabel.setText("Пользователи: " + stats.users);
                    callsLabel.setText("Звонки: " + stats.calls);
                    dateLabel.setText("Дата: " + stats.date);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    visitsLabel.setText("Ошибка");
                    pageviewsLabel.setText("Ошибка");
                    usersLabel.setText("Ошибка");
                    callsLabel.setText("Ошибка");
                    dateLabel.setText("Ошибка: " + ex.getMessage());
                });
            }
        }).start();
    }

    // Оформление маленького блока для каждой метрики
    private HBox makeStatBox(Label label, String color) {
        label.setFont(Font.font("Arial", 17));
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3436;");
        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(10);
        box.setPadding(new Insets(6, 14, 6, 14));
        box.setStyle("-fx-background-radius: 8; -fx-background-color: " + color + ";");
        return box;
    }
}