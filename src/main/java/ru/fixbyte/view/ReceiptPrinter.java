package ru.fixbyte.view;

import javafx. print.*;
import javafx.scene.Node;
import javafx. scene. layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene. text.Text;
import javafx.scene.text.Font;
import javafx.scene.text. FontWeight;
import javafx. scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import ru.fixbyte.model. ReceiptItem;
import ru. fixbyte.model.CompanySettings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java. time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io. InputStream;

public class ReceiptPrinter {

    private static final double RECEIPT_WIDTH = 520;
    private static final int LOGO_SIZE = 80;

    public void print(ObservableList<ReceiptItem> items) {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null && job.showPrintDialog(null)) {
            Node receipt = createReceiptNode(items);

            boolean success = job.printPage(receipt);
            if (success) {
                job.endJob();
                System.out.println("Чек успешно отправлен на печать");
            } else {
                System.err.println("Ошибка печати чека");
            }
        }
    }

    private Node createReceiptNode(ObservableList<ReceiptItem> items) {
        VBox receipt = new VBox(5);
        receipt.setPadding(new Insets(15));
        receipt.setAlignment(Pos.TOP_CENTER);
        receipt.setStyle("-fx-background-color: white;");
        receipt.setPrefWidth(550);

        // Шапка:  логотип и название
        receipt.getChildren().add(createHeader());

        // Контактная информация
        receipt.getChildren().add(createContactInfo());

        // Разделитель
        receipt.getChildren().add(createLine(RECEIPT_WIDTH));

        // Заголовок чека
        receipt.getChildren().add(createReceiptTitle());

        // Дата и время
        receipt.getChildren().add(createDateTime());

        // Разделитель
        receipt.getChildren().add(createLine(RECEIPT_WIDTH));

        // Заголовки таблицы
        receipt.getChildren().add(createTableHeaders());

        // Разделитель
        receipt.getChildren().add(createLine(RECEIPT_WIDTH));

        // Товары и подсчет итога
        double total = addItemsToReceipt(receipt, items);

        // Разделитель перед итогом
        receipt. getChildren().add(createLine(RECEIPT_WIDTH));

        // Итоговая сумма
        receipt.getChildren().add(createTotalSection(total));

        // Разделитель
        receipt.getChildren().add(createLine(RECEIPT_WIDTH));

        // Подписи
        receipt.getChildren().add(createSignatureSection());

        // Разделитель
        receipt. getChildren().add(createLine(RECEIPT_WIDTH));

        // Футер
        receipt.getChildren().add(createFooter());

        return receipt;
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);

        // Логотип
        try {
            InputStream logoStream = getClass().getResourceAsStream(CompanySettings.getLogoPath());
            if (logoStream != null) {
                Image logo = new Image(logoStream);
                ImageView logoView = new ImageView(logo);
                logoView.setFitWidth(LOGO_SIZE);
                logoView. setFitHeight(LOGO_SIZE);
                logoView.setPreserveRatio(true);
                headerBox.getChildren().add(logoView);
            } else {
                headerBox.getChildren().add(createLogoPlaceholder());
            }
        } catch (Exception e) {
            headerBox.getChildren().add(createLogoPlaceholder());
            System.err.println("Ошибка загрузки логотипа:  " + e.getMessage());
        }

        // Название компании
        Text companyName = new Text(CompanySettings. getCompanyName());
        companyName.setFont(Font. font("Arial", FontWeight. BOLD, 14));
        headerBox.getChildren().add(companyName);

        return headerBox;
    }

    private Text createLogoPlaceholder() {
        Text placeholder = new Text("[ЛОГОТИП]");
        placeholder. setFont(Font.font("Arial", FontWeight.BOLD, 10));
        placeholder.setFill(Color.web("#888888"));
        return placeholder;
    }

    private VBox createContactInfo() {
        VBox contactBox = new VBox(2);
        contactBox.setAlignment(Pos.CENTER);

        // Адрес
        Text address = new Text(CompanySettings.getAddress());
        address.setFont(Font.font("Arial", 9));
        contactBox.getChildren().add(address);

        // Телефон
        Text phone = new Text("Тел:  " + CompanySettings.getPhone());
        phone.setFont(Font.font("Arial", 9));
        contactBox.getChildren().add(phone);

        // ИНН (условно)
        if (CompanySettings.isShowInn()) {
            Text inn = new Text("ИНН: " + CompanySettings.getInn());
            inn.setFont(Font.font("Arial", 9));
            contactBox.getChildren().add(inn);
        }

        return contactBox;
    }

    private VBox createReceiptTitle() {
        VBox titleBox = new VBox();
        titleBox.setAlignment(Pos.CENTER);

        Text title = new Text("ТОВАРНЫЙ ЧЕК");
        title.setFont(Font.font("Arial", FontWeight. BOLD, 16));
        titleBox.getChildren().add(title);

        return titleBox;
    }

    private VBox createDateTime() {
        VBox dateBox = new VBox();
        dateBox.setAlignment(Pos.CENTER);

        String formattedDate = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd. MM.yyyy HH:mm:ss")
        );

        Text dateTime = new Text("Дата: " + formattedDate);
        dateTime.setFont(Font.font("Arial", 10));
        dateBox.getChildren().add(dateTime);

        return dateBox;
    }

    private HBox createTableHeaders() {
        HBox headers = new HBox();
        headers.setSpacing(5);
        headers.setPadding(new Insets(3, 0, 3, 0));

        Text colName = createTableHeaderText("Наименование", 240);
        Text colPrice = createTableHeaderText("Цена", 70);
        Text colQty = createTableHeaderText("Кол-во", 70);
        Text colTotal = createTableHeaderText("Сумма", 90);

        headers.getChildren().addAll(colName, colPrice, colQty, colTotal);

        return headers;
    }

    private Text createTableHeaderText(String text, double width) {
        Text headerText = new Text(text);
        headerText.setFont(Font.font("Courier New", FontWeight.BOLD, 10));
        headerText.setWrappingWidth(width);
        return headerText;
    }

    private double addItemsToReceipt(VBox receipt, ObservableList<ReceiptItem> items) {
        double total = 0;

        for (ReceiptItem item : items) {
            HBox itemRow = new HBox();
            itemRow.setSpacing(5);
            itemRow. setPadding(new Insets(2, 0, 2, 0));

            Text itemName = createItemText(item.getProduct().getName(), 240);
            Text itemPrice = createItemText(String.format("%.0f", item.getProduct().getPrice()), 70);
            Text itemQty = createItemText(
                    String.format("%.2f %s", item.getQuantity(), item.getProduct().getUnit()),
                    70
            );
            Text itemTotal = createItemText(String.format("%.0f", item.getTotal()), 90);

            itemRow.getChildren().addAll(itemName, itemPrice, itemQty, itemTotal);
            receipt.getChildren().add(itemRow);

            total += item.getTotal();
        }

        return total;
    }

    private Text createItemText(String text, double width) {
        Text itemText = new Text(text);
        itemText.setFont(Font.font("Courier New", 9));
        itemText.setWrappingWidth(width);
        return itemText;
    }

    private VBox createTotalSection(double total) {
        VBox totalBox = new VBox();
        totalBox.setAlignment(Pos.CENTER);
        totalBox.setPadding(new Insets(5, 0, 5, 0));

        Text totalText = new Text(String.format("ИТОГО: %.0f руб.", total));
        totalText. setFont(Font.font("Arial", FontWeight.BOLD, 13));
        totalBox.getChildren().add(totalText);

        return totalBox;
    }

    private VBox createSignatureSection() {
        VBox signatureBox = new VBox(10);
        signatureBox.setPadding(new Insets(15, 0, 5, 0));

        // Подпись продавца (всегда)
        signatureBox.getChildren().add(createSignatureLine("Продавец: "));

        // Подпись покупателя (условно)
        if (CompanySettings.isShowBuyerSignature()) {
            signatureBox.getChildren().add(createSignatureLine("Покупатель:"));
        }

        return signatureBox;
    }

    private HBox createSignatureLine(String label) {
        HBox signatureRow = new HBox();
        signatureRow.setSpacing(10);
        signatureRow.setAlignment(Pos.CENTER_LEFT);

        Text labelText = new Text(label);
        labelText.setFont(Font. font("Arial", 10));

        Line line = new Line(0, 0, 180, 0);
        line.setStroke(Color.BLACK);

        Text hint = new Text("(подпись)");
        hint.setFont(Font.font("Arial", 8));
        hint.setFill(Color.web("#666666"));

        HBox lineBox = new HBox(5, line, hint);
        lineBox.setAlignment(Pos.CENTER_LEFT);

        signatureRow.getChildren().addAll(labelText, lineBox);

        return signatureRow;
    }

    private VBox createFooter() {
        VBox footerBox = new VBox(3);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(10, 0, 0, 0));

        Text thanks = new Text("Спасибо за покупку!");
        thanks.setFont(Font. font("Arial", FontWeight. BOLD, 11));

        Text visitAgain = new Text("Будем рады видеть Вас снова!");
        visitAgain.setFont(Font.font("Arial", 9));

        footerBox.getChildren().addAll(thanks, visitAgain);

        return footerBox;
    }

    private Line createLine(double width) {
        Line line = new Line(0, 0, width, 0);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(0.5);
        return line;
    }
}