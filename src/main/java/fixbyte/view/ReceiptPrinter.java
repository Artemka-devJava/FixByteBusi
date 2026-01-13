package view;

import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx. scene.text.Font;
import javafx.scene. text.FontWeight;
import model.ReceiptItem;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.time.LocalDateTime;
import java. time.format.DateTimeFormatter;

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
        VBox receipt = new VBox(5);
        receipt.setPadding(new Insets(20));
        receipt.setAlignment(Pos.TOP_CENTER);
        receipt.setStyle("-fx-background-color: white;");

        // Заголовок
        Text header = new Text("ТОВАРНЫЙ ЧЕК");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        receipt.getChildren().add(header);

        // Дата и время
        Text dateTime = new Text(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd. MM.yyyy HH:mm:ss")));
        dateTime.setFont(Font.font("Arial", 12));
        receipt.getChildren().add(dateTime);

        // Линия
        Text line1 = new Text("═". repeat(50));
        receipt.getChildren().add(line1);

        // Заголовки столбцов
        Text columnHeaders = new Text(String.format("%-20s %8s %6s %10s",
                "Наименование", "Цена", "Кол-во", "Сумма"));
        columnHeaders.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
        receipt.getChildren().add(columnHeaders);

        Text line2 = new Text("─".repeat(50));
        receipt.getChildren().add(line2);

        // Товары
        double total = 0;
        for (ReceiptItem item : items) {
            String itemText = String.format("%-20s %8.2f %6.2f %10.2f",
                    truncate(item.getProduct().getName(), 20),
                    item.getProduct().getPrice(),
                    item.getQuantity(),
                    item.getTotal());

            Text itemNode = new Text(itemText);
            itemNode.setFont(Font.font("Courier New", 11));
            receipt.getChildren().add(itemNode);

            total += item.getTotal();
        }

        // Линия
        Text line3 = new Text("═".repeat(50));
        receipt.getChildren().add(line3);

        // Итого
        Text totalText = new Text(String.format("ИТОГО: %.2f руб.", total));
        totalText. setFont(Font.font("Arial", FontWeight.BOLD, 14));
        receipt.getChildren().add(totalText);

        // Футер
        Text footer = new Text("\nСпасибо за покупку!");
        footer.setFont(Font.font("Arial", 12));
        receipt.getChildren().add(footer);

        return receipt;
    }

    private String truncate(String str, int length) {
        return str.length() > length ? str.substring(0, length) : str;
    }
}
