package ru.fixbyte.monitoring.metrika;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class YandexMetrikaTabController {
    private final YandexMetrikaService metrikaService;
    private final Label statLabel = new Label("Визиты: ...");
    public final Tab tab;

    public YandexMetrikaTabController(YandexMetrikaService metrikaService) {
        this.metrikaService = metrikaService;
        VBox box = new VBox(10, statLabel, new Button("Обновить"));
        box.setPadding(new Insets(16));
        this.tab = new Tab("Яндекс Метрика", box);

        Button btn = (Button) box.getChildren().get(1);
        btn.setOnAction(e -> updateStats());
        updateStats();
    }

    private void updateStats() {
        statLabel.setText("Обновление...");
        new Thread(() -> {
            try {
                MetrikaStats stats = metrikaService.getTodayStats();
                Platform.runLater(() -> statLabel.setText(
                        "Визиты: " + stats.visits +
                                ", Просмотры: " + stats.pageviews +
                                ", Пользователи: " + stats.users +
                                "\nДата: " + stats.date));
            } catch (Exception ex) {
                Platform.runLater(() -> statLabel.setText("Ошибка: " + ex.getMessage()));
            }
        }).start();
    }
}