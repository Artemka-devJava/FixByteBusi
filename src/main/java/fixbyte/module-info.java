module ru.fixbyte {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens controller to javafx.fxml;
    opens model to javafx.base;

    exports controller;
    exports model;
    exports database;
    exports view;
}
