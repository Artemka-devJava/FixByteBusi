package ru.fixbyte. controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.fixbyte.model.Product;
import ru.fixbyte.model. ReceiptItem;
import ru.fixbyte.database.DatabaseManager;
import ru.fixbyte.view. ReceiptPrinter;

import java.sql.SQLException;

public class MainController {
    @FXML private ComboBox<Product> productComboBox;
    @FXML private TextField quantityField;
    @FXML private Button addButton;
    @FXML private TableView<ReceiptItem> receiptTable;
    @FXML private TableColumn<ReceiptItem, String> nameColumn;
    @FXML private TableColumn<ReceiptItem, Double> priceColumn;
    @FXML private TableColumn<ReceiptItem, Double> quantityColumn;
    @FXML private TableColumn<ReceiptItem, String> unitColumn;
    @FXML private TableColumn<ReceiptItem, Double> totalColumn;
    @FXML private Label totalLabel;
    @FXML private Button printButton;
    @FXML private Button clearButton;
    @FXML private Button manageProductsButton;
    @FXML private Button settingsButton;

    private DatabaseManager dbManager;
    private ObservableList<ReceiptItem> receiptItems;

    @FXML
    public void initialize() {
        System.out.println("Инициализация MainController...");

        try {
            dbManager = new DatabaseManager();
            receiptItems = FXCollections.observableArrayList();

            // Проверяем, что все элементы загружены
            if (nameColumn == null) {
                System.err.println("ОШИБКА: nameColumn is null");
                return;
            }

            // Настройка таблицы
            nameColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getName()));
            priceColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getProduct().getPrice()).asObject());
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            unitColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getUnit()));
            totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

            receiptTable.setItems(receiptItems);

            // Загрузка товаров
            loadProducts();

            // Обработчики событий
            addButton.setOnAction(e -> addItemToReceipt());
            printButton.setOnAction(e -> printReceipt());
            clearButton.setOnAction(e -> clearReceipt());
            manageProductsButton.setOnAction(e -> openProductManager());

            System.out.println("MainController инициализирован успешно!");

        } catch (Exception e) {
            System.err.println("ОШИБКА в initialize():");
            e.printStackTrace();
            showError("Ошибка инициализации:  " + e.getMessage());
        }
        settingsButton.setOnAction(e -> openSettings());
    }

    private void loadProducts() {
        try {
            productComboBox.setItems(FXCollections.observableArrayList(dbManager.getAllProducts()));
            System.out.println("Товары загружены");
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки товаров:");
            e.printStackTrace();
            showError("Ошибка загрузки товаров: " + e.getMessage());
        }
    }

    private void addItemToReceipt() {
        Product selectedProduct = productComboBox. getValue();
        String quantityText = quantityField.getText();

        if (selectedProduct == null || quantityText. isEmpty()) {
            showError("Выберите товар и введите количество");
            return;
        }

        try {
            double quantity = Double. parseDouble(quantityText);
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

        ReceiptPrinter printer = new ReceiptPrinter();
        printer.print(receiptItems);
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
            stage. setTitle("Управление товарами");
            stage.setScene(scene);
            stage.showAndWait();
            loadProducts();
        } catch (Exception e) {
            System.err.println("Ошибка открытия окна управления товарами:");
            e.printStackTrace();
            showError("Ошибка открытия окна: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/settings.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Настройки");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("Ошибка открытия настроек:");
            e.printStackTrace();
            showError("Ошибка открытия настроек:  " + e.getMessage());
        }
    }
}