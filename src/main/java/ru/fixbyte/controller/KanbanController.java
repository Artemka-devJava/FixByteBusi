package ru.fixbyte.controller;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
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
    private static final String COLUMN_DRAG_OVER =
            "-fx-background-color: #dbeafe; -fx-background-radius: 12;";

    public KanbanController(VBox todoColumn, VBox inProgressColumn, VBox doneColumn,
                            Button addTodoCardButton, StackPane todoWrap, StackPane inProgressWrap, StackPane doneWrap) {
        this.todoColumn = todoColumn;
        this.inProgressColumn = inProgressColumn;
        this.doneColumn = doneColumn;
        this.addTodoCardButton = addTodoCardButton;
        this.todoWrap = todoWrap;
        this.inProgressWrap = inProgressWrap;
        this.doneWrap = doneWrap;

        setupDnDKanban(todoWrap, todoColumn);
        setupDnDKanban(inProgressWrap, inProgressColumn);
        setupDnDKanban(doneWrap, doneColumn);

        if (addTodoCardButton != null) {
            if (!todoColumn.getChildren().contains(addTodoCardButton)) {
                todoColumn.getChildren().add(addTodoCardButton);
            }
            addTodoCardButton.setOnAction(e -> addKanbanCard(todoColumn, "Новое имя", "Контакты", "0.00", "Текст задачи"));
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
            if (node instanceof HBox card) {
                VBox infoBox = (VBox) card.getChildren().get(1);
                Label nameLbl = (Label) infoBox.getChildren().get(0);
                Label priceLbl = (Label) infoBox.getChildren().get(1);
                Label contactLbl = (Label) infoBox.getChildren().get(2);
                Label taskLbl = (Label) card.getChildren().get(2);

                String name = nameLbl.getText().replaceFirst("Имя: ","");
                String price = priceLbl.getText().replaceFirst("Цена: ","").replace(" руб.","");
                String contacts = contactLbl.getText().replaceFirst("Контакты: ","");
                String task = taskLbl.getText().replaceFirst("Задача: ","");

                writer.println(col + "|" +
                        escapeKanban(name) + "|" +
                        escapeKanban(contacts) + "|" +
                        escapeKanban(price) + "|" +
                        escapeKanban(task));
            }
        }
    }

    private String escapeKanban(String s) {
        if (s == null) return "";
        return s.replace("|", "%7C").replace("\n","\\n");
    }
    private String unescapeKanban(String s) {
        return s.replace("%7C","|").replace("\\n","\n");
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
                String[] sp = line.split("\\|", 5);
                if (sp.length < 5) continue;
                String col = sp[0];
                String name = unescapeKanban(sp[1]);
                String contacts = unescapeKanban(sp[2]);
                String price = unescapeKanban(sp[3]);
                String task = unescapeKanban(sp[4]);
                switch (col) {
                    case "todo" -> addKanbanCardNoSave(todoColumn, name, contacts, price, task);
                    case "inprogress" -> addKanbanCardNoSave(inProgressColumn, name, contacts, price, task);
                    case "done" -> addKanbanCardNoSave(doneColumn, name, contacts, price, task);
                }
            }
        } catch (Exception ex) {
            showError("Ошибка загрузки Kanban: " + ex.getMessage());
        }
        if (addTodoCardButton != null && !todoColumn.getChildren().contains(addTodoCardButton)) {
            todoColumn.getChildren().add(addTodoCardButton);
        }
    }

    private void addKanbanCardNoSave(VBox column, String name, String contacts, String price, String taskText) {
        HBox card = createKanbanCard(column, name, contacts, price, taskText, false);
        column.getChildren().add(card);
    }
    public void addKanbanCard(VBox column, String name, String contacts, String price, String taskText) {
        HBox card = createKanbanCard(column, name, contacts, price, taskText, true);
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

    private HBox createKanbanCard(VBox column, String name, String contacts, String price, String taskText, boolean autosaveOnEdit) {
        HBox card = new HBox(16);
        card.setStyle(CARD_STYLE);
        card.setMinHeight(90);
        card.setMaxHeight(120);

        // Иконка
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
        infoBox.setPrefWidth(148);

        Label nameLbl = new Label("Имя: " + name);
        nameLbl.setStyle("-fx-font-family:'Arial'; -fx-font-weight:bold; -fx-font-size: 15; -fx-text-fill: #34495e;");

        Label priceLbl = new Label("Цена: " + price + " руб.");
        priceLbl.setStyle("-fx-font-family:'Arial'; -fx-text-fill: #27ae60; -fx-font-size: 14;");

        Label contactLbl = new Label("Контакты: " + contacts);
        contactLbl.setStyle("-fx-font-family:'Arial'; -fx-text-fill: #888; -fx-font-size: 13;");

        infoBox.getChildren().addAll(nameLbl, priceLbl, contactLbl);

        Button delete = new Button("✖");
        delete.setStyle("-fx-background-color: #e74c3c;-fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 9;");
        delete.setOnAction(e -> {
            column.getChildren().remove(card);
            saveKanbanToFile();
        });
        VBox btnBox = new VBox(delete);
        btnBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);

        card.getChildren().addAll(iconView, infoBox, btnBox);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Drag&Drop и двойной клик не меняются!
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
                showKanbanCardDialog(card, nameLbl, priceLbl, contactLbl, null, contacts, priceLbl.getText(),
                        card.getUserData() != null ? card.getUserData().toString() : taskText, autosaveOnEdit);
            }
        });

        card.setUserData(contacts + "|" + price + "|" + taskText);
        card.setAccessibleText(taskText);

        return card;
    }

    private void setupDnDKanban(StackPane wrapper, VBox column) {
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
                    if (addTodoCardButton!=null && column == todoColumn && column.getChildren().contains(addTodoCardButton)) {
                        int btnIdx = column.getChildren().indexOf(addTodoCardButton);
                        column.getChildren().add(btnIdx, card);
                    } else {
                        column.getChildren().add(card);
                    }
                } else if (fromColumn == column) {
                    fromColumn.getChildren().remove(card);
                    if (addTodoCardButton!=null && column == todoColumn && column.getChildren().contains(addTodoCardButton)) {
                        int btnIdx = column.getChildren().indexOf(addTodoCardButton);
                        column.getChildren().add(btnIdx, card);
                    } else {
                        column.getChildren().add(card);
                    }
                }
                success = true;
                saveKanbanToFile();
            }
            event.setDropCompleted(success);
            wrapper.setStyle("");
            event.consume();
        });
    }

    private void showKanbanCardDialog(
            HBox card,
            Label nameLbl,
            Label priceLbl,
            Label contactLbl,
            Label taskLbl,
            String contacts,
            String price,
            String taskText,
            boolean autosaveOnEdit) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Карточка задачи");

        // Светлый фон, без обводки
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(390);
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

        // Четкие, крупные и тёмные метки
        Label nameLab = new Label("Имя:");
        nameLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #222;");
        nameLab.setMinWidth(100);
        Label contactsLab = new Label("Контакты:");
        contactsLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #222;");
        contactsLab.setMinWidth(100);
        Label priceLab = new Label("Цена:");
        priceLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #169c43;");
        priceLab.setMinWidth(100);
        Label taskLab = new Label("Задача:");
        taskLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #27659b;");
        taskLab.setMinWidth(100);

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

        // Явно ограничиваем ширину полей, чтобы лейблы не сжимались
        nameField.setPrefWidth(290);
        contactsField.setPrefWidth(290);
        priceField.setPrefWidth(290);
        taskArea.setPrefWidth(290);

        grid.addRow(0, nameLab, nameField);
        grid.addRow(1, contactsLab, contactsField);
        grid.addRow(2, priceLab, priceField);
        grid.addRow(3, taskLab, taskArea);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Стиль кнопок: зелёная и серая, без бордеров
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 7;");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #b0bec5; -fx-text-fill: #222; -fx-font-size: 15; -fx-background-radius: 7;");

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                nameLbl.setText("Имя: " + nameField.getText());
                priceLbl.setText("Цена: " + priceField.getText() + " руб.");
                contactLbl.setText("Контакты: " + contactsField.getText());
                String newTask = taskArea.getText();
                taskLbl.setText("Задача: " + newTask);
                card.setAccessibleText(newTask);
                card.setUserData(contactsField.getText() + "|" + priceField.getText() + "|" + newTask);
                if (autosaveOnEdit) saveKanbanToFile();
            }
        });
    }

    private void showError(String msg) {
        System.err.println(msg);
        // Можно сделать алерт через JavaFX, если необходимо
    }
}