package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout. VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import model.Product;
import model.ReceiptItem;
import database.DatabaseManager;
import view.ReceiptPrinter;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    private DatabaseManager dbManager;
    private ObservableList<ReceiptItem> receiptItems;

    @FXML
    public void initialize() {
        dbManager = new DatabaseManager();
        receiptItems = FXCollections. observableArrayList();

        // Настройка таблицы
        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getName()));
        priceColumn.setCellValueFactory(cellData ->
                new javafx.beans. property.SimpleDoubleProperty(cellData.getValue().getProduct().getPrice()).asObject());
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
    }

    private void loadProducts() {
        try {
            productComboBox.setItems(FXCollections.observableArrayList(dbManager.getAllProducts()));
        } catch (SQLException e) {
            showError("Ошибка загрузки товаров:  " + e.getMessage());
        }
    }

    private void addItemToReceipt() {
        Product selectedProduct = productComboBox. getValue();
        String quantityText = quantityField. getText();

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
        totalLabel.setText(String.format("Итого:  %.2f руб.", total));
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
            stage.setTitle("Управление товарами");
            stage.setScene(scene);
            stage.showAndWait();
            loadProducts(); // Обновить список после закрытия
        } catch (Exception e) {
            showError("Ошибка открытия окна:  " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }
}