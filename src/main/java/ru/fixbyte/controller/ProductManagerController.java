package ru.fixbyte.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.fixbyte.model.Product;
import ru.fixbyte.database.DatabaseManager;

import java.sql.SQLException;

public class ProductManagerController {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private Button addProductButton;
    @FXML private Button updateProductButton;
    @FXML private Button cancelButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, String> unitColumn;

    private DatabaseManager dbManager;
    private ObservableList<Product> products;
    private Product selectedProduct;

    @FXML
    public void initialize() {
        dbManager = new DatabaseManager();
        products = FXCollections.observableArrayList();

        unitComboBox.setItems(FXCollections.observableArrayList("шт", "кг", "л", "м", "упак", "г", "мл"));
        unitComboBox.getSelectionModel().selectFirst();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        productsTable.setItems(products);
        loadProducts();

        addProductButton.setOnAction(e -> addProduct());
        updateProductButton.setOnAction(e -> updateProduct());
        cancelButton.setOnAction(e -> clearForm());
        editButton.setOnAction(e -> editProduct());
        deleteButton.setOnAction(e -> deleteProduct());

        productsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) editProduct();
        });
    }

    private void loadProducts() {
        try {
            products.clear();
            products.addAll(dbManager.getAllProducts());
        } catch (SQLException e) {
            showError("Ошибка загрузки товаров: " + e.getMessage());
        }
    }

    private void addProduct() {
        if (!validateInput()) return;
        try {
            dbManager.addProduct(
                    nameField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    unitComboBox.getValue()
            );
            loadProducts();
            clearForm();
            showInfo("Товар успешно добавлен!");
        } catch (SQLException e) {
            showError("Ошибка добавления товара: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Неверный формат цены");
        }
    }

    private void editProduct() {
        selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showError("Выберите товар для редактирования");
            return;
        }
        nameField.setText(selectedProduct.getName());
        priceField.setText(String.valueOf(selectedProduct.getPrice()));
        unitComboBox.setValue(selectedProduct.getUnit());
        addProductButton.setDisable(true);
        updateProductButton.setDisable(false);
    }

    private void updateProduct() {
        if (selectedProduct == null || !validateInput()) return;
        try {
            dbManager.updateProduct(
                    selectedProduct.getId(),
                    nameField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    unitComboBox.getValue()
            );
            loadProducts();
            clearForm();
            showInfo("Товар успешно обновлён!");
        } catch (SQLException e) {
            showError("Ошибка обновления товара: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Неверный формат цены");
        }
    }

    private void deleteProduct() {
        Product product = productsTable.getSelectionModel().getSelectedItem();
        if (product == null) {
            showError("Выберите товар для удаления");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение");
        confirmation.setHeaderText("Удаление товара");
        confirmation.setContentText("Вы уверены, что хотите удалить товар \"" + product.getName() + "\"?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                dbManager.deleteProduct(product.getId());
                loadProducts();
                clearForm();
                showInfo("Товар успешно удалён!");
            } catch (SQLException e) {
                showError("Ошибка удаления товара: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        nameField.clear();
        priceField.clear();
        unitComboBox.getSelectionModel().selectFirst();
        selectedProduct = null;
        addProductButton.setDisable(false);
        updateProductButton.setDisable(true);
        productsTable.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();

        if (name.isEmpty()) {
            showError("Введите наименование товара");
            return false;
        }
        if (priceText.isEmpty()) {
            showError("Введите цену товара");
            return false;
        }
        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showError("Цена должна быть больше нуля");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Неверный формат цены");
            return false;
        }
        if (unitComboBox.getValue() == null || unitComboBox.getValue().isEmpty()) {
            showError("Выберите единицу измерения");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
