package ru.fixbyte.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.fixbyte.model.CompanySettings;

import java.io.File;

public class SettingsController {

    @FXML private TextField companyNameField;
    @FXML private TextArea addressField;
    @FXML private TextField phoneField;
    @FXML private TextField innField;
    @FXML private TextField logoPathField;

    @FXML private TextField windowWidthField;
    @FXML private TextField windowHeightField;

    @FXML private CheckBox showInnBox;
    @FXML private CheckBox showBuyerSignatureBox;

    // Автосохранение чеков
    @FXML private TextField receiptDirField;
    @FXML private Button chooseReceiptDirButton;
    @FXML private CheckBox autoSaveReceiptsBox;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML
    public void initialize() {
        companyNameField.setText(CompanySettings.getCompanyName());
        addressField.setText(CompanySettings.getAddress());
        phoneField.setText(CompanySettings.getPhone());
        innField.setText(CompanySettings.getInn());
        logoPathField.setText(CompanySettings.getLogoPath());

        windowWidthField.setText(String.valueOf(CompanySettings.getWindowWidth()));
        windowHeightField.setText(String.valueOf(CompanySettings.getWindowHeight()));

        showInnBox.setSelected(CompanySettings.isShowInn());
        showBuyerSignatureBox.setSelected(CompanySettings.isShowBuyerSignature());

        receiptDirField.setText(CompanySettings.getReceiptSaveDir());
        autoSaveReceiptsBox.setSelected(CompanySettings.isAutoSaveReceipts());
    }

    @FXML
    private void onChooseReceiptDir() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Выберите папку для чеков");
        String start = receiptDirField.getText();
        if (start != null && !start.isEmpty()) {
            File startDir = new File(start);
            if (startDir.exists()) chooser.setInitialDirectory(startDir);
        }
        File selected = chooser.showDialog(receiptDirField.getScene().getWindow());
        if (selected != null) receiptDirField.setText(selected.getAbsolutePath());
    }

    @FXML
    private void onSaveSettings() {
        try {
            CompanySettings.setCompanyName(companyNameField.getText());
            CompanySettings.setAddress(addressField.getText());
            CompanySettings.setPhone(phoneField.getText());
            CompanySettings.setInn(innField.getText());
            CompanySettings.setLogoPath(logoPathField.getText());

            int width = 1400, height = 900;
            try { width = Integer.parseInt(windowWidthField.getText()); } catch (NumberFormatException ignored) {}
            try { height = Integer.parseInt(windowHeightField.getText()); } catch (NumberFormatException ignored) {}
            CompanySettings.setWindowSize(width, height);

            CompanySettings.setShowInn(showInnBox.isSelected());
            CompanySettings.setShowBuyerSignature(showBuyerSignatureBox.isSelected());

            CompanySettings.setReceiptSaveDir(receiptDirField.getText());
            CompanySettings.setAutoSaveReceipts(autoSaveReceiptsBox.isSelected());

            CompanySettings.saveSettings();

            closeStage();
        } catch (Exception e) {
            showError("Ошибка сохранения настроек: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeStage();
    }

    @FXML
    private void onResetSize() {
        windowWidthField.setText("800");
        windowHeightField.setText("600");
    }

    @FXML
    private void onSet800x600() {
        windowWidthField.setText("800");
        windowHeightField.setText("600");
    }
    @FXML
    private void onSet1024x768() {
        windowWidthField.setText("1024");
        windowHeightField.setText("768");
    }
    @FXML
    private void onSet1280x720() {
        windowWidthField.setText("1280");
        windowHeightField.setText("720");
    }
    @FXML
    private void onSet1366x768() {
        windowWidthField.setText("1366");
        windowHeightField.setText("768");
    }

    private void closeStage() {
        Stage stage = (Stage) companyNameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR, text, ButtonType.OK);
        alert.showAndWait();
    }
}