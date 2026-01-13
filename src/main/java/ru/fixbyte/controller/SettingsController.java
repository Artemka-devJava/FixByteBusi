package ru.fixbyte.controller;

import javafx.fxml.FXML;
import javafx.scene. control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.fixbyte.model.CompanySettings;

import java.io.File;

public class SettingsController {
    @FXML private TextField companyNameField;
    @FXML private TextArea addressField;
    @FXML private TextField phoneField;
    @FXML private TextField innField;
    @FXML private CheckBox showInnCheckBox;
    @FXML private CheckBox showBuyerSignatureCheckBox;
    @FXML private TextField logoPathField;
    @FXML private Button browseLogoButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Новые поля для размера окна
    @FXML private TextField windowWidthField;
    @FXML private TextField windowHeightField;
    @FXML private Button resetSizeButton;
    @FXML private Button size800x600Button;
    @FXML private Button size1024x768Button;
    @FXML private Button size1280x720Button;
    @FXML private Button size1366x768Button;

    @FXML
    public void initialize() {
        // Загружаем текущие настройки
        companyNameField.setText(CompanySettings.getCompanyName());
        addressField.setText(CompanySettings.getAddress());
        phoneField.setText(CompanySettings. getPhone());
        innField.setText(CompanySettings.getInn());
        logoPathField.setText(CompanySettings.getLogoPath());
        showInnCheckBox.setSelected(CompanySettings.isShowInn());
        showBuyerSignatureCheckBox.setSelected(CompanySettings.isShowBuyerSignature());

        // Загружаем размеры окна
        windowWidthField.setText(String.valueOf(CompanySettings.getWindowWidth()));
        windowHeightField.setText(String.valueOf(CompanySettings.getWindowHeight()));

        // Связываем чекбокс с полем ИНН
        innField.disableProperty().bind(showInnCheckBox.selectedProperty().not());

        // Обработчики основных кнопок
        browseLogoButton.setOnAction(e -> browseLogo());
        saveButton. setOnAction(e -> saveSettings());
        cancelButton.setOnAction(e -> closeWindow());

        // Обработчики кнопок размеров окна
        resetSizeButton. setOnAction(e -> setWindowSize(800, 600));
        size800x600Button.setOnAction(e -> setWindowSize(800, 600));
        size1024x768Button.setOnAction(e -> setWindowSize(1024, 768));
        size1280x720Button.setOnAction(e -> setWindowSize(1280, 720));
        size1366x768Button.setOnAction(e -> setWindowSize(1366, 768));
    }

    private void setWindowSize(int width, int height) {
        windowWidthField.setText(String. valueOf(width));
        windowHeightField.setText(String.valueOf(height));
    }

    private void browseLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите логотип");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(logoPathField.getScene().getWindow());
        if (file != null) {
            try {
                File resourcesDir = new File("src/main/resources");
                if (! resourcesDir.exists()) {
                    resourcesDir.mkdirs();
                }

                String fileName = file.getName();
                File destFile = new File(resourcesDir, fileName);

                java.nio.file.Files.copy(
                        file.toPath(),
                        destFile.toPath(),
                        java.nio. file.StandardCopyOption. REPLACE_EXISTING
                );

                logoPathField.setText("/" + fileName);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setContentText("Логотип скопирован в resources как: /" + fileName);
                alert. showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                logoPathField.setText(file.getAbsolutePath());
            }
        }
    }

    private void saveSettings() {
        // Валидация основных полей
        if (companyNameField.getText().trim().isEmpty()) {
            showError("Введите название компании");
            return;
        }

        // Валидация размеров окна
        int width, height;
        try {
            width = Integer.parseInt(windowWidthField.getText());
            height = Integer.parseInt(windowHeightField.getText());

            if (width < 600 || width > 1920) {
                showError("Ширина окна должна быть от 600 до 1920 пикселей");
                return;
            }

            if (height < 400 || height > 1080) {
                showError("Высота окна должна быть от 400 до 1080 пикселей");
                return;
            }

        } catch (NumberFormatException e) {
            showError("Введите корректные числовые значения для размеров окна");
            return;
        }

        // Сохраняем все настройки
        CompanySettings.setCompanyName(companyNameField.getText());
        CompanySettings.setAddress(addressField.getText());
        CompanySettings.setPhone(phoneField.getText());
        CompanySettings.setInn(innField.getText());
        CompanySettings.setLogoPath(logoPathField.getText());
        CompanySettings.setShowInn(showInnCheckBox.isSelected());
        CompanySettings.setShowBuyerSignature(showBuyerSignatureCheckBox.isSelected());
        CompanySettings. setWindowSize(width, height);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText("Настройки сохранены!");
        alert.setContentText("Новый размер окна будет применен при следующем запуске приложения.");
        alert.showAndWait();

        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert. AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }
}