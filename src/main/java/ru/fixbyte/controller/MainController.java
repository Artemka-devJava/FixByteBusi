package ru.fixbyte.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.fixbyte.model.Product;
import ru.fixbyte.model.ReceiptItem;
import ru.fixbyte.model.CompanySettings;
import ru.fixbyte.database.DatabaseManager;
import ru.fixbyte.view.ReceiptPrinter;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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
    @FXML private TextArea addressField;
    private DatabaseManager dbManager;
    private ObservableList<ReceiptItem> receiptItems;
    @FXML private Button aboutButton;
    @FXML
    public void initialize() {
        System.out.println("Инициализация MainController...");

        try {
            dbManager = new DatabaseManager();
            receiptItems = FXCollections.observableArrayList();

            // Загружаем логотип и название компании
            loadLogo();
            companyNameLabel.setText(CompanySettings.getCompanyName());

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
            settingsButton.setOnAction(e -> openSettings());

            System.out.println("MainController инициализирован успешно!");

        } catch (Exception e) {
            System.err.println("ОШИБКА в initialize():");
            e.printStackTrace();
            showError("Ошибка инициализации:  " + e.getMessage());
        }
    }

    private void loadLogo() {
        try {
            String logoPath = CompanySettings.getLogoPath();
            if (logoPath != null && !logoPath.isEmpty()) {
                InputStream logoStream = getClass().getResourceAsStream(logoPath);
                if (logoStream != null) {
                    Image logo = new Image(logoStream);
                    // Например, ограничим размер до 96x96 (или любые ваши значения!)
                    logoImageView.setFitWidth(96);
                    logoImageView.setFitHeight(96);
                    logoImageView.setPreserveRatio(true);      // сохранять пропорции!
                    logoImageView.setSmooth(true);              // сглаживание
                    logoImageView.setImage(logo);
                    logoImageView.setVisible(true);
                    System.out.println("Логотип загружен");
                    return;
                }
            }
            System.out.println("Логотип не найден — скрыто изображение");
            logoImageView.setVisible(false);
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

        // Построение текста чека для печати и для файла
        String receiptText = buildReceiptText();

        // Сохраняем чек в файл, если это включено в настройках
        saveReceiptToFile(receiptText);

        // Реальная печать — вызываем ReceiptPrinter
        ReceiptPrinter printer = new ReceiptPrinter();
        printer.print(receiptItems);

        // Очистим чек после печати (если нужно)
        receiptItems.clear();
        updateTotal();
    }

    /**
     * Строит строку (текст чека)
     */
    private String buildReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           ТОВАРНЫЙ ЧЕК\n");
        sb.append("========================================\n\n");

        sb.append(CompanySettings.getCompanyName()).append("\n");
        sb.append("ИНН: ").append(CompanySettings.getInn()).append("\n");
        sb.append(CompanySettings.getAddress()).append("\n");
        sb.append("Дата: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("\n\n");

        sb.append("----------------------------------------\n");
        sb.append("Наименование         Кол-во   Цена  Сумма\n");
        sb.append("----------------------------------------\n");

        for (ReceiptItem item : receiptItems) {
            sb.append(String.format(
                    "%-20.17s %7.2f %7.2f %7.2f\n",
                    truncate(item.getProduct().getName(), 17),
                    item.getQuantity(),
                    item.getProduct().getPrice(),
                    item.getTotal()
            ));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("Итого: %33.2f руб.\n", receiptItems.stream().mapToDouble(ReceiptItem::getTotal).sum()));
        sb.append("========================================\n");
        sb.append("Спасибо за покупку!\n");
        sb.append("========================================\n");
        return sb.toString();
    }

    /**
     * Сохраняет чек в файл (если настройка включена)
     */
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
            System.out.println("Чек сохранён в файл: " + receiptFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Ошибка сохранения чека: " + e);
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

            // --- ДОБАВЛЕНИЕ ИКОНКИ ---
            java.io.InputStream iconStream = getClass().getResourceAsStream("/logo.png");
            if (iconStream != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconStream));
            } else {
                System.err.println("icon.png не найден для окна управления товарами!");
            }
            // --- КОНЕЦ ДОБАВЛЕНИЯ ИКОНКИ ---

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

            // ------ ДОБАВЛЯЕМ ИКОНКУ ------
            java.io.InputStream iconStream = getClass().getResourceAsStream("/logo.png");
            if (iconStream != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconStream));
            } else {
                System.err.println("logo.png не найден для окна настроек!");
            }
            // ------ КОНЕЦ ДОБАВЛЕНИЯ ИКОНКИ ------

            stage.setScene(scene);
            stage.showAndWait();

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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // --- Изменяем иконку окна ---
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        java.io.InputStream iconStream = getClass().getResourceAsStream("/logo.png");
        if (iconStream != null) {
            stage.getIcons().clear();
            stage.getIcons().add(new javafx.scene.image.Image(iconStream));
        } else {
            System.err.println("icon.png не найден для окна ошибки!");
        }

        alert.showAndWait();
    }

    /**
     * Обрезает строку до maxLen символов (с троеточием)
     */
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 1) + "…" : s;
    }
    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("FixByteBusi");
        alert.setContentText(
                """
                Программа автоматизации торговли FixByteBusi
                Версия: 0.2
    
                Разработчик: Артём Т. (Artemka-devJava)
                Email: artem@tarabakin.ru
    
                © 2024 Artemka-devJava
                Все права защищены.
                """
        );
        // --------- смена иконки ---------
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();
        stage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));
        // --------- конец смены иконки ----
        alert.showAndWait();
    }
}