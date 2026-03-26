package ru.fixbyte.view;

import ru.fixbyte.model.CompanySettings;
import ru.fixbyte.model.ReceiptItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptTextBuilder {

    public static String buildReceiptText(List<ReceiptItem> receiptItems) {
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

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 1) + "…" : s;
    }
}