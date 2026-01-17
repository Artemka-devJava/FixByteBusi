package ru.fixbyte.view;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class AboutDialog {

    public static void showAbout(String logoPath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("FixByteBusi");
        alert.setContentText(
                """
                Программа услуг FixByteBusi
                Версия: 0.3

                Разработчик: Артём Т. (Artemka-devJava)
                Email: artem@tarabakin.ru

                © 2024 Artemka-devJava
                Все права защищены.
                """
        );
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        InputStream iconStream = AboutDialog.class.getResourceAsStream(logoPath);
        if (iconStream != null) {
            stage.getIcons().clear();
            stage.getIcons().add(new Image(iconStream));
        }
        alert.showAndWait();
    }
}