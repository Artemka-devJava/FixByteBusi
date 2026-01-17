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
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.input.*;

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
    @FXML private Button aboutButton;

    // --- Канбан доска ---
    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;
    @FXML private Button addTodoCardButton;
    @FXML private StackPane todoWrap;
    @FXML private StackPane inProgressWrap;
    @FXML private StackPane doneWrap;

    // --- Стили для Kanban и диалогов ---
    private static final String CARD_STYLE =
            "-fx-background-color: linear-gradient(to bottom,#ecf0f1 75%,#fbfbfb);" +
                    " -fx-padding: 7 11 7 11; -fx-background-radius: 7;" +
                    " -fx-border-color: #dadee3; -fx-border-radius: 7;" +
                    " -fx-effect: dropshadow(two-pass-box,#b0bec5,2,0,0,1);";
    private static final String CARD_DRAGGED_STYLE =
            "-fx-background-color: #fffde7; -fx-padding: 7 11 7 11; -fx-background-radius: 7; -fx-border-color: #fbc02d; -fx-border-radius: 7;";
    private static final String COLUMN_DRAG_OVER =
            "-fx-background-color: #dbeafe; -fx-background-radius: 12;";

    private DatabaseManager dbManager;
    private ObservableList<ReceiptItem> receiptItems;

    @FXML
    public void initialize() {
        System.out.println("Инициализация MainController...");

        try {
            dbManager = new DatabaseManager();
            receiptItems = FXCollections.observableArrayList();

            // ---- КАНБАН ----
            setupDnDKanban(todoWrap, todoColumn);
            setupDnDKanban(inProgressWrap, inProgressColumn);
            setupDnDKanban(doneWrap, doneColumn);
            if (addTodoCardButton != null)
                addTodoCardButton.setOnAction(e -> addKanbanCard(
                        todoColumn,
                        "Новое имя", "Контакты", "0.00", "Текст задачи"
                ));

            // ---- КАССА ----
            loadLogo();
            companyNameLabel.setText(CompanySettings.getCompanyName());

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

    // ------ КАНБАН ------
    private void addKanbanCard(VBox column, String name, String contacts, String price, String taskText) {
        HBox card = new HBox(10);
        card.setStyle(CARD_STYLE);

        VBox infoBox = new VBox(2);
        infoBox.setPrefWidth(130);

        Label nameLbl = new Label("Имя: " + name);
        nameLbl.setStyle("-fx-font-family:'Arial'; -fx-font-weight:bold; -fx-font-size: 12; -fx-text-fill: #34495e;");
        Label priceLbl = new Label("Цена: " + price + " руб.");
        priceLbl.setStyle("-fx-font-family:'Arial'; -fx-text-fill: #27ae60; -fx-font-size: 12;");
        infoBox.getChildren().addAll(nameLbl, priceLbl);

        String shortTask = (taskText.length() > 30) ? taskText.substring(0,28) + "…" : taskText;
        Label taskLbl = new Label("Задача: " + shortTask);
        taskLbl.setStyle("-fx-font-family:'Arial'; -fx-text-fill: #3498db; -fx-font-size: 11;");

        Button delete = new Button("✖");
        delete.setStyle("-fx-background-color: #e74c3c;-fx-text-fill: white; -fx-font-size: 10; -fx-background-radius: 5;");
        delete.setOnAction(e -> column.getChildren().remove(card));
        VBox btnBox = new VBox(delete);
        btnBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);

        card.getChildren().addAll(infoBox, taskLbl, btnBox);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        card.setMinHeight(38);
        card.setMaxHeight(45);

        // Drag & Drop
        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("kanban-card");
            db.setContent(content);
            card.setStyle(CARD_DRAGGED_STYLE);
            card.setUserData(column);
            event.consume();
        });
        card.setOnDragDone(e -> card.setStyle(CARD_STYLE));

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                showKanbanCardDialog(card, nameLbl, priceLbl, taskLbl, contacts, priceLbl.getText(), card.getUserData() != null ? card.getUserData().toString() : taskText);
            }
        });

        card.setUserData(contacts + "|" + price + "|" + taskText);
        card.setAccessibleText(taskText);
        column.getChildren().add(card);
    }

    private void setupDnDKanban(StackPane wrapper, VBox column) {
        // wrapper — StackPane, column — VBox внутри него
        wrapper.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                wrapper.setStyle(COLUMN_DRAG_OVER);
            }
            event.consume();
        });
        wrapper.setOnDragExited(e -> wrapper.setStyle(""));
        wrapper.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                HBox card = (HBox) event.getGestureSource();
                VBox fromColumn = (VBox) card.getUserData();
                if (fromColumn != null && fromColumn != column) {
                    fromColumn.getChildren().remove(card);
                    column.getChildren().add(card);
                } else if (fromColumn == column) {
                    fromColumn.getChildren().remove(card);
                    column.getChildren().add(card);
                }
                success = true;
            }
            event.setDropCompleted(success);
            wrapper.setStyle("");
            event.consume();
        });
    }

    // --- увеличенный подробный диалог карточки ---
    private void showKanbanCardDialog(HBox card, Label nameLbl, Label priceLbl, Label taskLbl, String contacts, String price, String taskText) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Карточка задачи");
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(18);
        grid.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 18;");

        Label nameLab = new Label("Имя:");
        nameLab.setStyle("-fx-text-fill:#34495e;-fx-font-weight:bold; -fx-font-size:16;");
        Label contactsLab = new Label("Контакты:");
        contactsLab.setStyle("-fx-text-fill:#888;-fx-font-size:14;");
        Label priceLab = new Label("Цена:");
        priceLab.setStyle("-fx-text-fill:#27ae60;-fx-font-size:14;");
        Label taskLab = new Label("Задача:");
        taskLab.setStyle("-fx-text-fill:#3498db;-fx-font-size:15;");

        TextField nameField = new TextField(nameLbl.getText().replaceFirst("Имя: ",""));
        TextField contactsField = new TextField(contacts);
        TextField priceField = new TextField(priceLbl.getText().replaceFirst("Цена: ","").replace(" руб.",""));
        TextArea taskArea = new TextArea(card.getAccessibleText() == null ? "" : card.getAccessibleText());
        taskArea.setPrefRowCount(7);
        taskArea.setWrapText(true);
        taskArea.setStyle(
                "-fx-border-color: #3498db;-fx-focus-color: #3498db;-fx-background-radius:4;" +
                        "-fx-font-size:14; -fx-text-fill:#1a2539;"
        );

        grid.addRow(0, nameLab, nameField);
        grid.addRow(1, contactsLab, contactsField);
        grid.addRow(2, priceLab, priceField);
        grid.addRow(3, taskLab, taskArea);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                nameLbl.setText("Имя: " + nameField.getText());
                priceLbl.setText("Цена: " + priceField.getText() + " руб.");
                String newTask = taskArea.getText();
                String shortTask2 = (newTask.length() > 30) ? newTask.substring(0,28) + "…" : newTask;
                taskLbl.setText("Задача: " + shortTask2);
                card.setAccessibleText(newTask); // сохраняем длинную задачу
                card.setUserData(contactsField.getText() + "|" + priceField.getText() + "|" + newTask);
            }
        });
    }

    // ------ /КАНБАН ------

    // КАССА
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
        String receiptText = buildReceiptText();
        saveReceiptToFile(receiptText);

        ReceiptPrinter printer = new ReceiptPrinter();
        printer.print(receiptItems);

        receiptItems.clear();
        updateTotal();
    }

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
            InputStream iconStream = getClass().getResourceAsStream("/logo.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            } else {
                System.err.println("logo.png не найден для окна управления товарами!");
            }
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
            InputStream iconStream = getClass().getResourceAsStream("/logo.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            } else {
                System.err.println("logo.png не найден для окна настроек!");
            }
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
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        InputStream iconStream = getClass().getResourceAsStream("/logo.png");
        if (iconStream != null) {
            stage.getIcons().clear();
            stage.getIcons().add(new Image(iconStream));
        }
        alert.showAndWait();
    }
}