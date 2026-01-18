package ru.fixbyte.kanban.controller;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class KanbanController {
    private final VBox todoColumn;
    private final VBox inProgressColumn;
    private final VBox doneColumn;
    private final Button addTodoCardButton;
    private final StackPane todoWrap;
    private final StackPane inProgressWrap;
    private final StackPane doneWrap;
    private final StackPane archiveArea;

    private static final String CARD_STYLE =
            "-fx-background-color: linear-gradient(to bottom,#ecf0f1 75%,#fbfbfb);" +
                    " -fx-padding: 16 18 16 18; -fx-background-radius: 12;" +
                    " -fx-border-color: #dadee3; -fx-border-radius: 12;" +
                    " -fx-effect: dropshadow(two-pass-box,#b0bec5,2,0,0,2);";
    private static final String CARD_DRAGGED_STYLE =
            "-fx-background-color: #fffde7; -fx-padding: 16 18 16 18; -fx-background-radius: 12; -fx-border-color: #fbc02d; -fx-border-radius: 12;";

    private static final String TODO_DRAG_OVER = "-fx-background-color: #ffe3e3; -fx-background-radius: 12;";
    private static final String INPROG_DRAG_OVER = "-fx-background-color: #e3ffd2; -fx-background-radius: 12;";
    private static final String DONE_DRAG_OVER = "-fx-background-color: #d2e3ff; -fx-background-radius: 12;";
    private static final String ARCHIVE_DRAG_OVER = "-fx-background-color: #ffe598; -fx-background-radius: 27;";

    public KanbanController(
            VBox todoColumn, VBox inProgressColumn, VBox doneColumn,
            Button addTodoCardButton, StackPane todoWrap, StackPane inProgressWrap,
            StackPane doneWrap, StackPane archiveArea) {

        this.todoColumn = todoColumn;
        this.inProgressColumn = inProgressColumn;
        this.doneColumn = doneColumn;
        this.addTodoCardButton = addTodoCardButton;
        this.todoWrap = todoWrap;
        this.inProgressWrap = inProgressWrap;
        this.doneWrap = doneWrap;
        this.archiveArea = archiveArea;

        setupDnDKanban(todoWrap, todoColumn, TODO_DRAG_OVER);
        setupDnDKanban(inProgressWrap, inProgressColumn, INPROG_DRAG_OVER);
        setupDnDKanban(doneWrap, doneColumn, DONE_DRAG_OVER);
        setupArchiveDnD(archiveArea);

        restoreHeaderAndAddButton();

        addTodoCardButton.setOnAction(e ->
                addKanbanCard(todoColumn, "Новое имя", "Контакты", "0.00", "", false));

        loadKanbanFromFile();
    }

    private void restoreHeaderAndAddButton() {
        if (todoColumn.getChildren().isEmpty() ||
                !(todoColumn.getChildren().get(0) instanceof Label label && "Задачи".equals(label.getText()))) {
            Label todoHeader = new Label("Задачи");
            todoHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 0 0 9 0;");
            todoColumn.getChildren().add(0, todoHeader);
        }
        if (!todoColumn.getChildren().contains(addTodoCardButton)) {
            todoColumn.getChildren().add(1, addTodoCardButton);
        }
    }

    public void saveKanbanToFile() {
        File file = new File(ru.fixbyte.model.CompanySettings.getKanbanSavePath());
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();

        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            saveColumnCards(writer, todoColumn, "todo");
            saveColumnCards(writer, inProgressColumn, "inprogress");
            saveColumnCards(writer, doneColumn, "done");
        } catch (Exception ex) {
            showError("Ошибка сохранения Kanban: " + ex.getMessage());
        }
    }

    private void saveColumnCards(PrintWriter writer, VBox column, String col) {
        for (javafx.scene.Node node : column.getChildren()) {
            if (node instanceof HBox card) {
                VBox infoBox = (VBox) card.getChildren().get(1);
                Label nameLbl = (Label) infoBox.getChildren().get(0);
                Label priceLbl = (Label) infoBox.getChildren().get(1);
                Label contactLbl = (Label) infoBox.getChildren().get(2);
                CheckBox paidBox = (CheckBox) infoBox.getChildren().get(3);

                String name = nameLbl.getText().replaceFirst("Имя: ", "");
                String price = priceLbl.getText().replaceFirst("Цена: ", "").replace(" руб.", "");
                String contacts = contactLbl.getText().replaceFirst("Контакты: ", "");
                String paid = paidBox.isSelected() ? "1" : "0";
                String task = card.getAccessibleText() != null ? card.getAccessibleText() : "";

                writer.println(col + "|" +
                        escapeKanban(name) + "|" +
                        escapeKanban(contacts) + "|" +
                        escapeKanban(price) + "|" +
                        escapeKanban(task) + "|" +
                        paid);
            }
        }
    }

    private String escapeKanban(String s) {
        if (s == null) return "";
        return s.replace("|", "%7C").replace("\n", "\\n");
    }

    private String unescapeKanban(String s) {
        return s.replace("%7C", "|").replace("\\n", "\n");
    }

    public void loadKanbanFromFile() {
        clearCardsFromColumn(todoColumn, true);
        clearCardsFromColumn(inProgressColumn, false);
        clearCardsFromColumn(doneColumn, false);

        File file = new File(ru.fixbyte.model.CompanySettings.getKanbanSavePath());
        if (!file.exists()) return;

        try (java.util.Scanner sc = new java.util.Scanner(file, StandardCharsets.UTF_8)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] sp = line.split("\\|", 6);
                if (sp.length < 6) continue;
                String col = sp[0];
                String name = unescapeKanban(sp[1]);
                String contacts = unescapeKanban(sp[2]);
                String price = unescapeKanban(sp[3]);
                String task = unescapeKanban(sp[4]);
                boolean paid = "1".equals(sp[5]);
                if ("todo".equals(col)) {
                    addKanbanCardNoSave(todoColumn, name, contacts, price, task, paid);
                } else if ("inprogress".equals(col)) {
                    addKanbanCardNoSave(inProgressColumn, name, contacts, price, task, paid);
                } else if ("done".equals(col)) {
                    addKanbanCardNoSave(doneColumn, name, contacts, price, task, paid);
                }
            }
        } catch (Exception ex) {
            showError("Ошибка загрузки Kanban: " + ex.getMessage());
        }
        restoreHeaderAndAddButton();
    }

    // Очищаем только карточки (HBox), остальные узлы (Label, Button) не трогаем
    private void clearCardsFromColumn(VBox column, boolean isTodo) {
        List<javafx.scene.Node> toRemove = column.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .collect(Collectors.toList());
        column.getChildren().removeAll(toRemove);

        // Восстанавливаем заголовки если вдруг удалились
        if (column.getChildren().isEmpty()) {
            String title;
            String style;
            if (column == todoColumn) {
                title = "Задачи";
                style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 0 0 9 0;";
            } else if (column == inProgressColumn) {
                title = "В работе";
                style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3498db; -fx-padding: 0 0 9 0;";
            } else {
                title = "Готово";
                style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-padding: 0 0 9 0;";
            }
            Label header = new Label(title);
            header.setStyle(style);
            column.getChildren().add(header);
            if (isTodo && !column.getChildren().contains(addTodoCardButton))
                column.getChildren().add(addTodoCardButton);
        } else if (isTodo) {
            // обязательно кнопка после заголовка
            if (!column.getChildren().contains(addTodoCardButton)) {
                column.getChildren().add(1, addTodoCardButton);
            }
        }
    }

    // --- Главное: карточки добавляются В КОНЕЦ, а кнопка "Добавить карточку" остается вверху ---
    private void addKanbanCardNoSave(VBox column, String name, String contacts, String price, String taskText, boolean paid) {
        HBox card = createKanbanCard(name, contacts, price, taskText, paid);
        column.getChildren().add(card); // всегда в конец
    }

    public void addKanbanCard(VBox column, String name, String contacts, String price, String taskText, boolean paid) {
        HBox card = createKanbanCard(name, contacts, price, taskText, paid);
        column.getChildren().add(card); // всегда в конец
        saveKanbanToFile();
    }

    private HBox createKanbanCard(String name, String contacts, String price, String taskText, boolean paid) {
        HBox card = new HBox(16);
        card.setStyle(CARD_STYLE);
        card.setMinHeight(90);
        card.setMaxHeight(120);

        ImageView iconView;
        try {
            Image userIcon = new Image(getClass().getResourceAsStream("/logomain.png"));
            iconView = new ImageView(userIcon);
        } catch (Exception e) {
            iconView = new ImageView();
        }
        iconView.setFitWidth(36);
        iconView.setFitHeight(36);
        iconView.setPreserveRatio(true);
        iconView.setSmooth(true);

        VBox infoBox = new VBox(6);
        infoBox.setPrefWidth(170);

        Label nameLbl = new Label("Имя: " + name);
        nameLbl.setStyle("-fx-font-family:'Arial'; -fx-font-weight:bold; -fx-font-size: 15; -fx-text-fill: #34495e;");
        Label priceLbl = new Label("Цена: " + price + " руб.");
        priceLbl.setStyle("-fx-font-family:'Arial'; -fx-text-fill: #27ae60; -fx-font-size: 14;");
        Label contactLbl = new Label("Контакты: " + contacts);
        contactLbl.setStyle("-fx-font-family:'Arial'; -fx-text-fill: #888; -fx-font-size: 13;");

        CheckBox paidBox = new CheckBox("Оплачено");
        paidBox.setSelected(paid);
        paidBox.setStyle("-fx-font-size: 13; -fx-font-family:'Arial'");
        paidBox.setAlignment(Pos.CENTER_LEFT);

        paidBox.setOnAction(e -> saveKanbanToFile());

        infoBox.getChildren().addAll(nameLbl, priceLbl, contactLbl, paidBox);

        Button delete = new Button("✖");
        delete.setStyle("-fx-background-color: #e74c3c;-fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 9;");
        delete.setOnAction(e -> {
            Pane parent = (Pane) card.getParent();
            if (parent != null) {
                parent.getChildren().remove(card);
                saveKanbanToFile();
            }
        });
        VBox btnBox = new VBox(delete);
        btnBox.setAlignment(Pos.TOP_RIGHT);

        card.getChildren().addAll(iconView, infoBox, btnBox);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("kanban-card");
            db.setContent(content);
            card.setStyle(CARD_DRAGGED_STYLE);
            event.consume();
        });
        card.setOnDragDone(e -> card.setStyle(CARD_STYLE));

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String currentContacts = contactLbl.getText().replaceFirst("Контакты: ", "");
                String currentName = nameLbl.getText().replaceFirst("Имя: ", "");
                String currentPrice = priceLbl.getText().replaceFirst("Цена: ", "").replace(" руб.", "");
                String currentTask = card.getAccessibleText() == null ? "" : card.getAccessibleText();
                showKanbanCardDialog(card, nameLbl, priceLbl, contactLbl, paidBox, null, currentContacts, currentPrice, currentTask);
            }
        });

        card.setAccessibleText(taskText);

        return card;
    }

    private void setupDnDKanban(StackPane wrapper, VBox column, String dragOverColor) {
        wrapper.setOnDragOver(event -> {
            if (event.getGestureSource() != wrapper && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                wrapper.setStyle(dragOverColor);
            }
            event.consume();
        });
        wrapper.setOnDragExited(e -> wrapper.setStyle(""));
        wrapper.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                HBox card = (HBox) event.getGestureSource();
                Pane parent = (Pane) card.getParent();
                if (parent != null) parent.getChildren().remove(card);
                column.getChildren().add(card); // всегда в конец
                saveKanbanToFile();
                success = true;
            }
            event.setDropCompleted(success);
            wrapper.setStyle("");
            event.consume();
        });
    }

    private void setupArchiveDnD(StackPane archiveArea) {
        archiveArea.setOnDragOver(event -> {
            if (event.getGestureSource() != archiveArea && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                archiveArea.setStyle(ARCHIVE_DRAG_OVER);
            }
            event.consume();
        });
        archiveArea.setOnDragExited(e -> archiveArea.setStyle("-fx-background-color: #3498db; -fx-background-radius: 27;"));
        archiveArea.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                HBox card = (HBox) event.getGestureSource();
                Pane parent = (Pane) card.getParent();
                if (parent != null) {
                    parent.getChildren().remove(card);
                    saveCardToArchiveFile(card);
                    removeCardFromKanbanFile(card);
                }
                success = true;
            }
            event.setDropCompleted(success);
            archiveArea.setStyle("-fx-background-color: #3498db; -fx-background-radius: 27;");
            event.consume();
        });
    }

    // Сохраняем карточку в файл-архив
    private void saveCardToArchiveFile(HBox card) {
        try {
            File file = new File(ru.fixbyte.model.CompanySettings.getKanbanArchivePath());
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();

            VBox infoBox = (VBox) card.getChildren().get(1);
            Label nameLbl = (Label) infoBox.getChildren().get(0);
            Label priceLbl = (Label) infoBox.getChildren().get(1);
            Label contactLbl = (Label) infoBox.getChildren().get(2);
            CheckBox paidBox = (CheckBox) infoBox.getChildren().get(3);

            String name = nameLbl.getText().replaceFirst("Имя: ","");
            String price = priceLbl.getText().replaceFirst("Цена: ","").replace(" руб.","");
            String contacts = contactLbl.getText().replaceFirst("Контакты: ","");
            String paid = paidBox.isSelected() ? "1" : "0";
            String task = card.getAccessibleText() != null ? card.getAccessibleText() : "";

            try (PrintWriter writer = new PrintWriter(new java.io.FileOutputStream(file, true), true, java.nio.charset.StandardCharsets.UTF_8)) {
                writer.println(
                        escapeKanban(name) + "|" +
                                escapeKanban(contacts) + "|" +
                                escapeKanban(price) + "|" +
                                escapeKanban(task) + "|" +
                                paid
                );
            }
        } catch (Exception ex) {
            showError("Ошибка архивации карточки: " + ex.getMessage());
        }
    }

    // Удаляем строку карточки из kanban-файла после архивации
    private void removeCardFromKanbanFile(HBox card) {
        try {
            File file = new File(ru.fixbyte.model.CompanySettings.getKanbanSavePath());
            if (!file.exists()) return;

            VBox infoBox = (VBox) card.getChildren().get(1);
            Label nameLbl = (Label) infoBox.getChildren().get(0);
            Label priceLbl = (Label) infoBox.getChildren().get(1);
            Label contactLbl = (Label) infoBox.getChildren().get(2);
            CheckBox paidBox = (CheckBox) infoBox.getChildren().get(3);

            String name = escapeKanban(nameLbl.getText().replaceFirst("Имя: ",""));
            String contacts = escapeKanban(contactLbl.getText().replaceFirst("Контакты: ",""));
            String price = escapeKanban(priceLbl.getText().replaceFirst("Цена: ","").replace(" руб.",""));
            String task = escapeKanban(card.getAccessibleText() == null ? "" : card.getAccessibleText());
            String paid = paidBox.isSelected() ? "1" : "0";

            java.util.List<String> allLines = java.nio.file.Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            String toRemove = null;
            for (String line : allLines) {
                String[] sp = line.split("\\|");
                if (sp.length >= 6
                        && sp[1].equals(name)
                        && sp[2].equals(contacts)
                        && sp[3].equals(price)
                        && sp[4].equals(task)
                        && sp[5].equals(paid)) {
                    toRemove = line;
                    break;
                }
            }
            if (toRemove != null) {
                allLines.remove(toRemove);
                java.nio.file.Files.write(file.toPath(), allLines, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            showError("Ошибка удаления из Kanban-файла: " + ex.getMessage());
        }
    }

    // ---- Главный фикс: saveKanbanToFile() всегда вызывается после изменения карточки ----
    private void showKanbanCardDialog(
            HBox card,
            Label nameLbl,
            Label priceLbl,
            Label contactLbl,
            CheckBox paidBox,
            Label taskLbl,
            String contacts,
            String price,
            String taskText) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Карточка задачи");
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(420);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #f7fafc;" +
                        "-fx-background-radius: 12;"
        );

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(16);
        grid.setMinWidth(440);
        grid.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-padding: 24;" +
                        "-fx-background-radius: 10;"
        );

        Label nameLab = new Label("Имя:");
        nameLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #222;");
        nameLab.setMinWidth(100);
        Label contactsLab = new Label("Контакты:");
        contactsLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #222;");
        contactsLab.setMinWidth(100);
        Label priceLab = new Label("Цена:");
        priceLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #169c43;");
        priceLab.setMinWidth(100);
        Label taskLabLabel = new Label("Задача:");
        taskLabLabel.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #27659b;");
        taskLabLabel.setMinWidth(100);

        CheckBox dialogPaidBox = new CheckBox("Оплачено");
        dialogPaidBox.setSelected(paidBox != null && paidBox.isSelected());
        dialogPaidBox.setStyle("-fx-font-size: 15px; -fx-font-family:'Arial'");
        dialogPaidBox.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField(nameLbl.getText().replaceFirst("Имя: ",""));
        nameField.setStyle("-fx-font-size: 15px;");
        TextField contactsField = new TextField(contacts);
        contactsField.setStyle("-fx-font-size: 15px;");
        TextField priceField = new TextField(priceLbl.getText().replaceFirst("Цена: ","").replace(" руб.",""));
        priceField.setStyle("-fx-font-size: 15px;");
        TextArea taskArea = new TextArea(card.getAccessibleText() == null ? "" : card.getAccessibleText());
        taskArea.setPrefRowCount(6);
        taskArea.setWrapText(true);
        taskArea.setStyle("-fx-font-size: 15px; -fx-font-family:'Arial';");

        nameField.setPrefWidth(290);
        contactsField.setPrefWidth(290);
        priceField.setPrefWidth(290);
        taskArea.setPrefWidth(290);

        grid.addRow(0, nameLab, nameField);
        grid.addRow(1, contactsLab, contactsField);
        grid.addRow(2, priceLab, priceField);
        grid.addRow(3, taskLabLabel, taskArea);
        grid.addRow(4, new Label(""), dialogPaidBox);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 7;");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #b0bec5; -fx-text-fill: #222; -fx-font-size: 15; -fx-background-radius: 7;");

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                nameLbl.setText("Имя: " + nameField.getText());
                priceLbl.setText("Цена: " + priceField.getText() + " руб.");
                contactLbl.setText("Контакты: " + contactsField.getText());
                if (paidBox != null) paidBox.setSelected(dialogPaidBox.isSelected());
                String newTask = taskArea.getText();
                if (taskLbl != null) taskLbl.setText("Задача: " + newTask);
                card.setAccessibleText(newTask);
                // ---- Сразу после любого изменения - сохранить! ----
                saveKanbanToFile();
            }
        });
    }

    private void showError(String message) {

        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });

    }
}