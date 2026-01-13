package ru.fixbyte. controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx. scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene. image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage. Stage;
import ru.fixbyte.model.Product;
import ru.fixbyte.model.ReceiptItem;
import ru.fixbyte. model.CompanySettings;
import ru.fixbyte.database.DatabaseManager;
import ru.fixbyte.view.ReceiptPrinter;

import java.io. InputStream;
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
    @FXML private Label companyNameLabel;
    @FXML private Button printButton;
    @FXML private Button clearButton;
    @FXML private Button manageProductsButton;
    @FXML private Button settingsButton;
    @FXML private ImageView logoImageView;

    private DatabaseManager dbManager;
    private ObservableList<ReceiptItem> receiptItems;

    @FXML
    public void initialize() {
        System.out.println("Инициализация MainController...");

        try {
            dbManager = new DatabaseManager();
            receiptItems = FXCollections. observableArrayList();

            // Загружаем логотип
            loadLogo();

            // Загружаем название компании
            companyNameLabel.setText(CompanySettings.getCompanyName());

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
            quantityColumn. setCellValueFactory(new PropertyValueFactory<>("quantity"));
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
            settingsButton.setOnAction(e -> openSettings());

            System.out.println("MainController инициализирован успешно!");

        } catch (Exception e) {
            System.err. println("ОШИБКА в initialize():");
            e.printStackTrace();
            showError("Ошибка инициализации:  " + e.getMessage());
        }
    }

    private void loadLogo() {
        try {
            InputStream logoStream = getClass().getResourceAsStream(CompanySettings.getLogoPath());
            if (logoStream != null) {
                Image logo = new Image(logoStream);
                logoImageView.setImage(logo);
                System.out.println("Логотип загружен");
            } else {
                System.out.println("Логотип не найден по пути: " + CompanySettings.getLogoPath());
                // Можно установить логотип по умолчанию
                logoImageView. setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки логотипа:  " + e.getMessage());
            logoImageView.setVisible(false);
        }
    }

    private void loadProducts() {
        try {
            productComboBox.setItems(FXCollections.observableArrayList(dbManager.getAllProducts()));
            System.out.println("Товары загружены");
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки товаров:");
            e.printStackTrace();
            showError("Ошибка загрузки товаров:  " + e.getMessage());
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
            loadProducts();
        } catch (Exception e) {
            System.err.println("Ошибка открытия окна управления товарами:");
            e.printStackTrace();
            showError("Ошибка открытия окна:  " + e.getMessage());
        }
    }

    private void openSettings() {
        try {
            System.out.println("Открытие настроек...");

            // Проверяем наличие файла
            java.net.URL fxmlUrl = getClass().getResource("/settings.fxml");
            System.out.println("URL settings.fxml: " + fxmlUrl);

            if (fxmlUrl == null) {
                showError("Файл settings.fxml не найден!\nУбедитесь, что файл находится в src/main/resources/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Настройки");
            stage.setScene(scene);
            stage.showAndWait();

            // Обновляем интерфейс после закрытия настроек
            loadLogo();
            companyNameLabel.setText(CompanySettings.getCompanyName());

            System.out.println("Настройки закрыты");

        } catch (Exception e) {
            System.err.println("Ошибка открытия настроек:");
            e.printStackTrace();
            showError("Ошибка открытия настроек: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType. ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }
}