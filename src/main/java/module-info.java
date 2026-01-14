open module ru.fixbyte {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    // Разрешаем нативный доступ
    requires static org.slf4j;

}