package ru.fixbyte.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.fixbyte.model.CompanySettings;

import java.io.File;

public class SettingsController {
    @FXML private TextField companyNameField;
    @FXML private TextArea addressField;
    @FXML private TextField phoneField;
    @FXML private TextField innField;
    @FXML private TextField logoPathField;
    @FXML private Button browseLogoButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML
    public void initialize() {
        // Загружаем текущие настройки
        companyNameField.setText(CompanySettings.getCompanyName());
        addressField.setText(CompanySettings.getAddress());
        phoneField.setText(CompanySettings. getPhone());
        innField.setText(CompanySettings.getInn());
        logoPathField. setText(CompanySettings.getLogoPath());

        // Обработчики
        browseLogoButton.setOnAction(e -> browseLogo());
        saveButton. setOnAction(e -> saveSettings());
        cancelButton.setOnAction(e -> closeWindow());
    }

    private void browseLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите логотип");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(logoPathField.getScene().getWindow());
        if (file != null) {
            logoPathField.setText(file.getAbsolutePath());
        }
    }

    private void saveSettings() {
        CompanySettings.setCompanyName(companyNameField.getText());
        CompanySettings.setAddress(addressField.getText());
        CompanySettings.setPhone(phoneField. getText());
        CompanySettings. setInn(innField.getText());
        CompanySettings.setLogoPath(logoPathField. getText());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setContentText("Настройки сохранены!");
        alert.showAndWait();

        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}