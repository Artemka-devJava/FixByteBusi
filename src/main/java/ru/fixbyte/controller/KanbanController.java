package ru.fixbyte.controller;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class KanbanController {
    private final VBox todoColumn;
    private final VBox inProgressColumn;
    private final VBox doneColumn;
    private final Button addTodoCardButton;
    private final StackPane todoWrap;
    private final StackPane inProgressWrap;
    private final StackPane doneWrap;

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

    public KanbanController(
            VBox todoColumn, VBox inProgressColumn, VBox doneColumn,
            Button addTodoCardButton, StackPane todoWrap, StackPane inProgressWrap, StackPane doneWrap) {

        this.todoColumn = todoColumn;
        this.inProgressColumn = inProgressColumn;
        this.doneColumn = doneColumn;
        this.addTodoCardButton = addTodoCardButton;
        this.todoWrap = todoWrap;
        this.inProgressWrap = inProgressWrap;
        this.doneWrap = doneWrap;

        setupDnDKanban(todoWrap, todoColumn, TODO_DRAG_OVER);
        setupDnDKanban(inProgressWrap, inProgressColumn, INPROG_DRAG_OVER);
        setupDnDKanban(doneWrap, doneColumn, DONE_DRAG_OVER);

        if (addTodoCardButton != null) {
            if (!todoColumn.getChildren().contains(addTodoCardButton)) {
                todoColumn.getChildren().add(addTodoCardButton);
            }
            addTodoCardButton.setOnAction(e -> addKanbanCard(todoColumn, "Новое имя", "Контакты", "0.00", "", false));
        }

        loadKanbanFromFile();
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
            // Пропустить кнопку "Добавить"
            if (node == addTodoCardButton) continue;
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
        File file = new File(ru.fixbyte.model.CompanySettings.getKanbanSavePath());
        if (!file.exists()) return;

        todoColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        doneColumn.getChildren().clear();

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
                switch (col) {
                    case "todo" -> addKanbanCardNoSave(todoColumn, name, contacts, price, task, paid);
                    case "inprogress" -> addKanbanCardNoSave(inProgressColumn, name, contacts, price, task, paid);
                    case "done" -> addKanbanCardNoSave(doneColumn, name, contacts, price, task, paid);
                }
            }
        } catch (Exception ex) {
            showError("Ошибка загрузки Kanban: " + ex.getMessage());
        }
        if (addTodoCardButton != null && !todoColumn.getChildren().contains(addTodoCardButton)) {
            todoColumn.getChildren().add(addTodoCardButton);
        }
    }

    private void addKanbanCardNoSave(VBox column, String name, String contacts, String price, String taskText, boolean paid) {
        HBox card = createKanbanCard(column, name, contacts, price, taskText, paid, false);
        column.getChildren().add(card);
    }

    public void addKanbanCard(VBox column, String name, String contacts, String price, String taskText, boolean paid) {
        HBox card = createKanbanCard(column, name, contacts, price, taskText, paid, true);
        if (addTodoCardButton != null && column == todoColumn) {
            int btnIdx = column.getChildren().indexOf(addTodoCardButton);
            if (btnIdx >= 0) {
                column.getChildren().add(btnIdx, card);
            } else {
                column.getChildren().add(card);
            }
        } else {
            column.getChildren().add(card);
        }
        saveKanbanToFile();
    }

    // Карточка: имя, цена, контакты, чекбокс "Оплачено"
    private HBox createKanbanCard(VBox column, String name, String contacts, String price, String taskText, boolean paid, boolean autosaveOnEdit) {
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

        // Если нужно автосохранять состояние оплаты:
        paidBox.setOnAction(e -> saveKanbanToFile());

        infoBox.getChildren().addAll(nameLbl, priceLbl, contactLbl, paidBox);

        Button delete = new Button("✖");
        delete.setStyle("-fx-background-color: #e74c3c;-fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 9;");
        delete.setOnAction(e -> {
            column.getChildren().remove(card);
            saveKanbanToFile();
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
            card.setUserData(column);
            event.consume();
        });
        card.setOnDragDone(e -> card.setStyle(CARD_STYLE));

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                showKanbanCardDialog(card, nameLbl, priceLbl, contactLbl, paidBox, null, contacts, priceLbl.getText(),
                        card.getUserData() != null ? card.getUserData().toString() : taskText, autosaveOnEdit);
            }
        });

        card.setUserData(contacts + "|" + price + "|" + taskText + "|" + (paid ? "1" : "0"));
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
                VBox fromColumn = (VBox) card.getUserData();
                if (fromColumn != null) {
                    fromColumn.getChildren().remove(card);
                }
                if (addTodoCardButton != null && column == todoColumn && column.getChildren().contains(addTodoCardButton)) {
                    int btnIdx = column.getChildren().indexOf(addTodoCardButton);
                    column.getChildren().add(btnIdx, card);
                } else {
                    column.getChildren().add(card);
                }
                success = true;
                saveKanbanToFile();
            }
            event.setDropCompleted(success);
            wrapper.setStyle("");
            event.consume();
        });
    }

    // По двойному клику: полное редактирование карточки
    private void showKanbanCardDialog(
            HBox card,
            Label nameLbl,
            Label priceLbl,
            Label contactLbl,
            CheckBox paidBox,
            Label taskLbl,
            String contacts,
            String price,
            String taskText,
            boolean autosaveOnEdit) {

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
                card.setUserData(contactsField.getText() + "|" + priceField.getText() + "|" + newTask + "|" + (dialogPaidBox.isSelected() ? "1" : "0"));
                if (autosaveOnEdit) saveKanbanToFile();
            }
        });
    }

    private void showError(String msg) {
        System.err.println(msg);
        // Можно сделать алерт через JavaFX, если необходимо
    }
}