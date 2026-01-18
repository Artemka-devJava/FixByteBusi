package ru.fixbyte.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import ru.fixbyte.kanban.controller.KanbanController;
import ru.fixbyte.model.Product;
import ru.fixbyte.model.ReceiptItem;
import ru.fixbyte.model.CompanySettings;
import ru.fixbyte.database.DatabaseManager;
import ru.fixbyte.view.AboutDialog;
import ru.fixbyte.view.ReceiptPrinter;
import ru.fixbyte.view.ReceiptTextBuilder;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {
    // --- Чек/касса ---
    @FXML
    private ComboBox<Product> productComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private Button addButton;
    @FXML
    private TableView<ReceiptItem> receiptTable;
    @FXML
    private TableColumn<ReceiptItem, String> nameColumn;
    @FXML
    private TableColumn<ReceiptItem, Double> priceColumn;
    @FXML
    private TableColumn<ReceiptItem, Double> quantityColumn;
    @FXML
    private TableColumn<ReceiptItem, String> unitColumn;
    @FXML
    private TableColumn<ReceiptItem, Double> totalColumn;
    @FXML
    private Label totalLabel;
    @FXML
    private Label companyNameLabel;
    @FXML
    private Button printButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button manageProductsButton;
    @FXML
    private Button settingsButton;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Button aboutButton;

    // --- Kanban для передачи в KanbanController ---
    @FXML
    private VBox todoColumn;
    @FXML
    private VBox inProgressColumn;
    @FXML
    private VBox doneColumn;
    @FXML
    private Button addTodoCardButton;
    @FXML
    private StackPane todoWrap;
    @FXML
    private StackPane inProgressWrap;
    @FXML
    private StackPane doneWrap;
    @FXML
    private VBox archiveColumn;
    @FXML
    private StackPane archiveWrap;
    @FXML
    private StackPane archiveArea;
    // --- Вспомогательные контроллеры ---
    private KanbanController kanbanController;
    private DatabaseManager dbManager;
    private ObservableList<ReceiptItem> receiptItems;

    @FXML
    public void initialize() {
        // --- Receipt Controller (касса) ---
        CompanySettings.loadSettings();
        dbManager = new DatabaseManager();
        receiptItems = FXCollections.observableArrayList();

        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getName()));
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getProduct().getPrice()).asObject());
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getUnit()));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        receiptTable.setItems(receiptItems);

        loadProducts();
        addButton.setOnAction(e -> addItemToReceipt());
        printButton.setOnAction(e -> printReceipt());
        clearButton.setOnAction(e -> clearReceipt());
        manageProductsButton.setOnAction(e -> openProductManager());
        settingsButton.setOnAction(e -> openSettings());
        loadLogo();
        companyNameLabel.setText(CompanySettings.getCompanyName());

        // --- Kanban Controller интеграция ---
        kanbanController = new KanbanController(
                todoColumn, inProgressColumn, doneColumn,
                addTodoCardButton, todoWrap, inProgressWrap, doneWrap, archiveArea
        );
    }

    // ---- КАССА ----

    private void loadLogo() {
        try {
            String logoPath = CompanySettings.getLogoPath();
            if (logoPath != null && !logoPath.isEmpty()) {
                InputStream logoStream = getClass().getResourceAsStream(logoPath);
                if (logoStream != null) {
                    Image logo = new Image(logoStream);
                    logoImageView.setFitWidth(96);
                    logoImageView.setFitHeight(96);
                    logoImageView.setPreserveRatio(true);
                    logoImageView.setSmooth(true);
                    logoImageView.setImage(logo);
                    logoImageView.setVisible(true);
                    return;
                }
            }
            logoImageView.setVisible(false);
        } catch (Exception e) {
            logoImageView.setVisible(false);
        }
    }

    private void loadProducts() {
        try {
            productComboBox.setItems(FXCollections.observableArrayList(dbManager.getAllProducts()));
        } catch (SQLException e) {
            showError("Ошибка загрузки товаров:  " + e.getMessage());
        }
    }

    private void addItemToReceipt() {
        Product selectedProduct = productComboBox.getValue();
        String quantityText = quantityField.getText();
        if (selectedProduct == null || quantityText.isEmpty()) {
            showError("Выберите товар и введите количество");
            return;
        }
        try {
            double quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) {
                showError("Количество должно быть больше нуля");
                return;
            }
            ReceiptItem item = new ReceiptItem(selectedProduct, quantity);
            receiptItems.add(item);
            updateTotal();
            quantityField.clear();
        } catch (NumberFormatException e) {
            showError("Неверное количество");
        }
    }

    private void updateTotal() {
        double total = receiptItems.stream()
                .mapToDouble(ReceiptItem::getTotal)
                .sum();
        totalLabel.setText(String.format("Итого: %.2f руб.", total));
    }

    private void printReceipt() {
        if (receiptItems.isEmpty()) {
            showError("Чек пуст");
            return;
        }
        String receiptText = ReceiptTextBuilder.buildReceiptText(receiptItems);
        saveReceiptToFile(receiptText);

        ReceiptPrinter printer = new ReceiptPrinter();
        printer.print(receiptItems);

        receiptItems.clear();
        updateTotal();
    }


    private void saveReceiptToFile(String receiptText) {
        if (!CompanySettings.isAutoSaveReceipts()) return;
        String dir = CompanySettings.getReceiptSaveDir();
        if (dir == null || dir.trim().isEmpty()) return;

        File folder = new File(dir);
        if (!folder.exists()) folder.mkdirs();

        String fileName = "Чек_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        File receiptFile = new File(folder, fileName);

        try (PrintWriter writer = new PrintWriter(receiptFile, StandardCharsets.UTF_8)) {
            writer.print(receiptText);
        } catch (Exception e) {
            // silent
        }
    }

    private void clearReceipt() {
        receiptItems.clear();
        updateTotal();
    }

    private void openProductManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/product-manager.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Управление товарами");
            InputStream iconStream = getClass().getResourceAsStream("/logo.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
            stage.setScene(scene);
            stage.showAndWait();
            loadProducts();
        } catch (Exception e) {
            showError("Ошибка открытия окна:  " + e.getMessage());
        }
    }

    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/settings.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Настройки");
            InputStream iconStream = getClass().getResourceAsStream("/logo.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
            stage.setScene(scene);
            stage.showAndWait();
            loadLogo();
            companyNameLabel.setText(CompanySettings.getCompanyName());
        } catch (Exception e) {
            showError("Ошибка открытия окна настроек: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        InputStream iconStream = getClass().getResourceAsStream("/logo.png");
        if (iconStream != null) {
            stage.getIcons().clear();
            stage.getIcons().add(new Image(iconStream));
        }
        alert.showAndWait();
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 1) + "…" : s;
    }

    @FXML
    private void onAbout() {
        AboutDialog.showAbout("/logo.png");
    }
}