open module ru.fixbyte {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports ru.fixbyte;
    exports ru.fixbyte.controller;
    exports ru.fixbyte.model;

    exports ru.fixbyte.kanban.model;
    exports ru.fixbyte.kanban.view;
    exports ru.fixbyte.kanban.controller;
}