package ru. fixbyte.view;

import javafx.print.*;
import javafx.scene.Node;
import javafx.scene. layout. VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx. scene.text.Font;
import javafx.scene. text.FontWeight;
import javafx.scene.image.Image;
import javafx. scene.image.ImageView;
import javafx.scene.shape.Line;
import ru.fixbyte.model. ReceiptItem;
import ru.fixbyte.model.CompanySettings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.time.LocalDateTime;
import java. time.format.DateTimeFormatter;
import java.io. InputStream;

public class ReceiptPrinter {

    public void print(ObservableList<ReceiptItem> items) {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null && job.showPrintDialog(null)) {
            Node receipt = createReceiptNode(items);

            boolean success = job.printPage(receipt);
            if (success) {
                job. endJob();
            }
        }
    }

    private Node createReceiptNode(ObservableList<ReceiptItem> items) {
        VBox receipt = new VBox(8);
        receipt.setPadding(new Insets(30));
        receipt.setAlignment(Pos.TOP_CENTER);
        receipt.setStyle("-fx-background-color: white;");
        receipt.setPrefWidth(600);

        // ============ ЛОГОТИП И НАЗВАНИЕ КОМПАНИИ ============
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);

        // Попытка загрузить логотип
        try {
            InputStream logoStream = getClass().getResourceAsStream(CompanySettings.getLogoPath());
            if (logoStream != null) {
                Image logo = new Image(logoStream);
                ImageView logoView = new ImageView(logo);
                logoView.setFitWidth(120);
                logoView.setFitHeight(120);
                logoView.setPreserveRatio(true);
                headerBox.getChildren().add(logoView);
            } else {
                // Если логотипа нет - показываем рамку
                Text logoPlaceholder = new Text("[МЕСТО ДЛЯ ЛОГОТИПА]");
                logoPlaceholder.setFont(Font.font("Arial", FontWeight. BOLD, 12));
                logoPlaceholder. setStyle("-fx-fill: #888888;");
                headerBox.getChildren().add(logoPlaceholder);
            }
        } catch (Exception e) {
            // Если ошибка загрузки - показываем заглушку
            Text logoPlaceholder = new Text("[МЕСТО ДЛЯ ЛОГОТИПА]");
            logoPlaceholder.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            logoPlaceholder.setStyle("-fx-fill: #888888;");
            headerBox.getChildren().add(logoPlaceholder);
        }

        // Название компании
        Text companyName = new Text(CompanySettings. getCompanyName());
        companyName.setFont(Font. font("Arial", FontWeight. BOLD, 20));
        headerBox.getChildren().add(companyName);

        receipt.getChildren().add(headerBox);

        // ============ АДРЕС И КОНТАКТЫ ============
        VBox addressBox = new VBox(5);
        addressBox.setAlignment(Pos.CENTER);

        Text address = new Text(CompanySettings. getAddress());
        address.setFont(Font.font("Arial", 11));

        Text phone = new Text("Тел:  " + CompanySettings.getPhone());
        phone.setFont(Font.font("Arial", 11));

        Text inn = new Text("ИНН:  " + CompanySettings.getInn());
        inn.setFont(Font.font("Arial", 11));

        addressBox.getChildren().addAll(address, phone, inn);
        receipt.getChildren().add(addressBox);

        // Линия
        receipt.getChildren().add(createLine(550));

        // ============ ЗАГОЛОВОК ЧЕКА ============
        Text header = new Text("ТОВАРНЫЙ ЧЕК");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        receipt.getChildren().add(header);

        // Дата и время
        Text dateTime = new Text("Дата: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd. MM.yyyy HH:mm:ss")));
        dateTime.setFont(Font.font("Arial", 12));
        receipt.getChildren().add(dateTime);

        receipt.getChildren().add(createLine(550));

        // ============ ЗАГОЛОВКИ СТОЛБЦОВ ============
        HBox columnHeaders = new HBox();
        columnHeaders.setSpacing(10);
        columnHeaders. setPadding(new Insets(5, 0, 5, 0));

        Text colName = new Text("Наименование");
        colName.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        colName.setWrappingWidth(250);

        Text colPrice = new Text("Цена");
        colPrice.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        colPrice.setWrappingWidth(80);

        Text colQty = new Text("Кол-во");
        colQty.setFont(Font.font("Courier New", FontWeight. BOLD, 12));
        colQty.setWrappingWidth(70);

        Text colTotal = new Text("Сумма");
        colTotal.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        colTotal.setWrappingWidth(100);

        columnHeaders. getChildren().addAll(colName, colPrice, colQty, colTotal);
        receipt.getChildren().add(columnHeaders);

        receipt.getChildren().add(createLine(550));

        // ============ ТОВАРЫ ============
        double total = 0;
        for (ReceiptItem item : items) {
            HBox itemRow = new HBox();
            itemRow.setSpacing(10);
            itemRow.setPadding(new Insets(3, 0, 3, 0));

            Text itemName = new Text(item.getProduct().getName());
            itemName.setFont(Font.font("Courier New", 11));
            itemName.setWrappingWidth(250);

            Text itemPrice = new Text(String.format("%.2f", item.getProduct().getPrice()));
            itemPrice.setFont(Font.font("Courier New", 11));
            itemPrice.setWrappingWidth(80);

            Text itemQty = new Text(String.format("%.2f %s",
                    item.getQuantity(), item.getProduct().getUnit()));
            itemQty.setFont(Font.font("Courier New", 11));
            itemQty.setWrappingWidth(70);

            Text itemTotal = new Text(String.format("%.2f", item.getTotal()));
            itemTotal.setFont(Font.font("Courier New", 11));
            itemTotal.setWrappingWidth(100);

            itemRow. getChildren().addAll(itemName, itemPrice, itemQty, itemTotal);
            receipt.getChildren().add(itemRow);

            total += item.getTotal();
        }

        // Линия перед итогом
        receipt.getChildren().add(createLine(550));

        // ============ ИТОГО ============
        HBox totalBox = new HBox();
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(10, 20, 10, 0));

        Text totalLabel = new Text("ИТОГО:  ");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Text totalValue = new Text(String.format("%.2f руб.", total));
        totalValue.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        totalBox. getChildren().addAll(totalLabel, totalValue);
        receipt.getChildren().add(totalBox);

        receipt.getChildren().add(createLine(550));

        // ============ МЕСТО ДЛЯ ПОДПИСИ ============
        VBox signatureBox = new VBox(15);
        signatureBox.setPadding(new Insets(30, 0, 10, 0));

        // Подпись продавца
        HBox sellerSignature = new HBox();
        sellerSignature.setSpacing(10);
        sellerSignature.setAlignment(Pos.CENTER_LEFT);

        Text sellerLabel = new Text("Продавец:");
        sellerLabel.setFont(Font.font("Arial", 12));

        Line sellerLine = new Line(0, 0, 200, 0);
        sellerLine.setStroke(javafx.scene.paint.Color.BLACK);

        Text sellerHint = new Text("(подпись)");
        sellerHint.setFont(Font. font("Arial", 10));
        sellerHint.setStyle("-fx-fill: #666666;");

        HBox sellerLineBox = new HBox(10, sellerLine, sellerHint);
        sellerLineBox.setAlignment(Pos.CENTER_LEFT);

        sellerSignature.getChildren().addAll(sellerLabel, sellerLineBox);

        // Подпись покупателя
        HBox buyerSignature = new HBox();
        buyerSignature.setSpacing(10);
        buyerSignature.setAlignment(Pos.CENTER_LEFT);

        Text buyerLabel = new Text("Покупатель:");
        buyerLabel.setFont(Font. font("Arial", 12));

        Line buyerLine = new Line(0, 0, 200, 0);
        buyerLine.setStroke(javafx.scene.paint.Color.BLACK);

        Text buyerHint = new Text("(подпись)");
        buyerHint.setFont(Font.font("Arial", 10));
        buyerHint. setStyle("-fx-fill: #666666;");

        HBox buyerLineBox = new HBox(10, buyerLine, buyerHint);
        buyerLineBox.setAlignment(Pos.CENTER_LEFT);

        buyerSignature. getChildren().addAll(buyerLabel, buyerLineBox);

        signatureBox.getChildren().addAll(sellerSignature, buyerSignature);
        receipt.getChildren().add(signatureBox);

        // ============ ФУТЕР ============
        VBox footerBox = new VBox(5);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(20, 0, 0, 0));

        receipt.getChildren().add(createLine(550));

        Text thanks = new Text("Спасибо за покупку!");
        thanks.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Text visitAgain = new Text("Будем рады видеть Вас снова!");
        visitAgain.setFont(Font. font("Arial", 11));

        footerBox.getChildren().addAll(thanks, visitAgain);
        receipt.getChildren().add(footerBox);

        return receipt;
    }

    // Вспомогательный метод для создания линий
    private Line createLine(double width) {
        Line line = new Line(0, 0, width, 0);
        line.setStroke(javafx.scene.paint.Color.BLACK);
        line.setStrokeWidth(1);
        return line;
    }
}