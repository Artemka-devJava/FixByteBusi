open module ru.fixbyte {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    // Основные пакеты приложения
    exports ru.fixbyte;
    exports ru.fixbyte.controller;
    exports ru.fixbyte.model;
    exports ru.fixbyte.database;
    exports ru.fixbyte.view;

    // Kanban-доска
    exports ru.fixbyte.kanban.model;
    exports ru.fixbyte.kanban.view;
    exports ru.fixbyte.kanban.controller;
    exports ru.fixbyte.kanban.service;

    // Яндекс.Метрика
    exports ru.fixbyte.monitoring.metrika;

    // Заметки
    exports ru.fixbyte.notes.model;
    exports ru.fixbyte.notes.service;
    exports ru.fixbyte.notes.controller;
}