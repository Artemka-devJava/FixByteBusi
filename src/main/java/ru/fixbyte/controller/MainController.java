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
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import ru.fixbyte.kanban.controller.KanbanController;
import ru.fixbyte.model.Product;
import ru.fixbyte.model.ReceiptItem;
import ru.fixbyte.model.CompanySettings;
import ru.fixbyte.database.DatabaseManager;
import ru.fixbyte.monitoring.metrika.YandexMetrikaService;
import ru.fixbyte.monitoring.metrika.YandexMetrikaTabController;
import ru.fixbyte.notes.controller.NotesController;
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
import java.util.List;

public class MainController {

    // --- Чеки ---
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
    @FXML private Button aboutButton;

    // --- Kanban ---
    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;
    @FXML private Button addTodoCardButton;
    @FXML private StackPane todoWrap;
    @FXML private StackPane inProgressWrap;
    @FXML private StackPane doneWrap;
    @FXML private StackPane archiveArea;

    // --- Вкладки ---
    @FXML private TabPane tabPane;
    @FXML private Tab receiptsTab;
    @FXML private Tab kanbanTab;

    // --- Сервисы ---
    private DatabaseManager dbManager;
    private ObservableList<ReceiptItem> receiptItems;
    private Tab metrikaTab;
    private Tab notesTab;

    @FXML
    public void initialize() {
        CompanySettings.loadSettings();
        dbManager = new DatabaseManager();
        receiptItems = FXCollections.observableArrayList();

        // Настройка таблицы чеков
        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getName()));
        priceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getProduct().getPrice()).asObject());
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getUnit()));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        receiptTable.setItems(receiptItems);

        loadProducts();

        // Обработчики кнопок кассы
        addButton.setOnAction(e -> addItemToReceipt());
        printButton.setOnAction(e -> printReceipt());
        clearButton.setOnAction(e -> clearReceipt());
        manageProductsButton.setOnAction(e -> openProductManager());
        settingsButton.setOnAction(e -> openSettings());

        loadLogo();
        companyNameLabel.setText(CompanySettings.getCompanyName());

        // Инициализация Kanban
        new KanbanController(
                todoColumn, inProgressColumn, doneColumn,
                addTodoCardButton, todoWrap, inProgressWrap, doneWrap, archiveArea
        );

        // Инициализация Яндекс.Метрики
        YandexMetrikaService metrikaService = new YandexMetrikaService(
                "y0__xCN_6a9BRijgT0g3PPMjharbw5qpsLnjebIcuqW5xfpily7Vg",
                "106195489"
        );
        metrikaTab = new YandexMetrikaTabController(metrikaService).tab;

        // Инициализация Заметок
        notesTab = new NotesController().tab;

        applyTabVisibility();
    }

    // --- Управление видимостью вкладок ---

    private void applyTabVisibility() {
        setTabVisible(receiptsTab, CompanySettings.isShowReceiptsTab());
        setTabVisible(kanbanTab, CompanySettings.isShowKanbanTab());
        setTabVisible(metrikaTab, CompanySettings.isShowMetrikaTab());
        setTabVisible(notesTab, CompanySettings.isShowNotesTab());
        tabPane.getTabs().setAll(getOrderedVisibleTabs());

        if (!tabPane.getTabs().isEmpty()
                && !tabPane.getTabs().contains(tabPane.getSelectionModel().getSelectedItem())) {
            tabPane.getSelectionModel().selectFirst();
        }
    }

    private void setTabVisible(Tab tab, boolean visible) {
        if (tab == null) return;
        if (visible) {
            if (!tabPane.getTabs().contains(tab)) tabPane.getTabs().add(tab);
        } else {
            tabPane.getTabs().remove(tab);
        }
    }

    private List<Tab> getOrderedVisibleTabs() {
        return java.util.stream.Stream.of(receiptsTab, kanbanTab, metrikaTab, notesTab)
                .filter(tab -> tab != null && tabPane.getTabs().contains(tab))
                .toList();
    }

    // --- Касса ---

    private void loadLogo() {
        try {
            String logoPath = CompanySettings.getLogoPath();
            if (logoPath != null && !logoPath.isEmpty()) {
                InputStream logoStream = getClass().getResourceAsStream(logoPath);
                if (logoStream != null) {
                    logoImageView.setFitWidth(96);
                    logoImageView.setFitHeight(96);
                    logoImageView.setPreserveRatio(true);
                    logoImageView.setSmooth(true);
                    logoImageView.setImage(new Image(logoStream));
                    logoImageView.setVisible(true);
                    return;
                }
            }
        } catch (Exception ignored) {}
        logoImageView.setVisible(false);
    }

    private void loadProducts() {
        try {
            productComboBox.setItems(FXCollections.observableArrayList(dbManager.getAllProducts()));
        } catch (SQLException e) {
            showError("Ошибка загрузки товаров: " + e.getMessage());
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
            receiptItems.add(new ReceiptItem(selectedProduct, quantity));
            updateTotal();
            quantityField.clear();
        } catch (NumberFormatException e) {
            showError("Неверное количество");
        }
    }

    private void updateTotal() {
        double total = receiptItems.stream().mapToDouble(ReceiptItem::getTotal).sum();
        totalLabel.setText(String.format("Итого: %.2f руб.", total));
    }

    private void printReceipt() {
        if (receiptItems.isEmpty()) {
            showError("Чек пуст");
            return;
        }
        saveReceiptToFile(ReceiptTextBuilder.buildReceiptText(receiptItems));
        new ReceiptPrinter().print(receiptItems);
        receiptItems.clear();
        updateTotal();
    }

    private void saveReceiptToFile(String receiptText) {
        if (!CompanySettings.isAutoSaveReceipts()) return;
        String dir = CompanySettings.getReceiptSaveDir();
        if (dir == null || dir.trim().isEmpty()) return;

        File folder = new File(dir);
        if (!folder.exists() && !folder.mkdirs()) return;

        String fileName = "Чек_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        try (PrintWriter writer = new PrintWriter(new File(folder, fileName), StandardCharsets.UTF_8)) {
            writer.print(receiptText);
        } catch (Exception ignored) {}
    }

    private void clearReceipt() {
        receiptItems.clear();
        updateTotal();
    }

    // --- Окна ---

    private void openProductManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/product-manager.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Управление товарами");
            stage.setScene(new Scene(loader.load()));
            setStageIcon(stage);
            stage.showAndWait();
            loadProducts();
        } catch (Exception e) {
            showError("Ошибка открытия окна: " + e.getMessage());
        }
    }

    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/settings.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Настройки");
            stage.setScene(new Scene(loader.load()));
            setStageIcon(stage);
            stage.showAndWait();
            loadLogo();
            companyNameLabel.setText(CompanySettings.getCompanyName());
            applyTabVisibility();
        } catch (Exception e) {
            showError("Ошибка открытия окна настроек: " + e.getMessage());
        }
    }

    private void setStageIcon(Stage stage) {
        InputStream iconStream = getClass().getResourceAsStream("/logo.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
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
            stage.getIcons().add(new Image(iconStream));
        }
        alert.showAndWait();
    }


    @FXML
    private void onAbout() {
        AboutDialog.showAbout("/logo.png");
    }
}