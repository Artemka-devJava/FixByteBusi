open module ru.fixbyte {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Добавляем SQLite JDBC как автоматический модуль
    requires org.xerial.sqlitejdbc;
}