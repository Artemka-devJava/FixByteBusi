package ru.fixbyte.kanban.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ru.fixbyte.kanban.model.KanbanCardModel;

public class KanbanCardEditDialog {

    private final Dialog<ButtonType> dialog;
    private final TextField nameField;
    private final TextField contactsField;
    private final TextField priceField;
    private final TextArea taskArea;
    private final CheckBox paidBox;

    public KanbanCardEditDialog(KanbanCardModel initial) {
        dialog = new Dialog<>();
        dialog.setTitle("Карточка задачи");
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(420);

        try {
            ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(
                    new Image(getClass().getResourceAsStream("/logomain.png"))
            );
        } catch (Exception ignored) {}

        dialog.getDialogPane().setStyle(
                "-fx-background-color: linear-gradient(to bottom,#ecf0f1 75%,#fbfbfb);"
                        + "-fx-background-radius: 18;"
                        + "-fx-border-color: #dadee3;"
                        + "-fx-border-radius: 18;"
                        + "-fx-effect: dropshadow(two-pass-box,#b0bec5,2,0,0,2);"
        );

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(16);
        grid.setMinWidth(440);
        grid.setStyle(
                "-fx-background-color: #ffffff;"
                        + "-fx-padding: 24;"
                        + "-fx-background-radius: 13;"
                        + "-fx-effect: dropshadow(two-pass-box,#e1eaf1,1,0,0,1);"
        );

        Label nameLab = new Label("Имя:");
        nameLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #34495e;");
        nameLab.setMinWidth(100);
        Label contactsLab = new Label("Контакты:");
        contactsLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #888;");
        contactsLab.setMinWidth(100);
        Label priceLab = new Label("Цена:");
        priceLab.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #27ae60;");
        priceLab.setMinWidth(100);
        Label taskLabLabel = new Label("Задача:");
        taskLabLabel.setStyle("-fx-font-size: 16px; -fx-font-weight:bold; -fx-text-fill: #27659b;");
        taskLabLabel.setMinWidth(100);

        paidBox = new CheckBox("Оплачено");
        paidBox.setSelected(initial.paid);
        paidBox.setStyle("-fx-font-size: 15px; -fx-font-family:'Arial'; -fx-text-fill: #169c43;");
        paidBox.setAlignment(Pos.CENTER_LEFT);

        nameField = new TextField(initial.name);
        nameField.setStyle("-fx-font-size: 15px; -fx-background-color: #f8f9fa; -fx-border-radius:7; -fx-border-color:#dadee3");
        contactsField = new TextField(initial.contacts);
        contactsField.setStyle("-fx-font-size: 15px; -fx-background-color: #f8f9fa; -fx-border-radius:7; -fx-border-color:#dadee3");
        priceField = new TextField(initial.price);
        priceField.setStyle("-fx-font-size: 15px; -fx-background-color: #f8f9fa; -fx-border-radius:7; -fx-border-color:#dadee3");
        taskArea = new TextArea(initial.task);
        taskArea.setPrefRowCount(6);
        taskArea.setWrapText(true);
        taskArea.setStyle("-fx-font-size: 15px; -fx-background-color: #fbfbfb; -fx-border-radius:7; -fx-border-color:#dadee3");

        nameField.setPrefWidth(290);
        contactsField.setPrefWidth(290);
        priceField.setPrefWidth(290);
        taskArea.setPrefWidth(290);

        grid.addRow(0, nameLab, nameField);
        grid.addRow(1, contactsLab, contactsField);
        grid.addRow(2, priceLab, priceField);
        grid.addRow(3, taskLabLabel, taskArea);
        grid.addRow(4, new Label(""), paidBox);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 9;"
                        + "-fx-effect: dropshadow(two-pass-box,#b0bec5,1,0,0,1);"
        );
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle(
                "-fx-background-color: #b0bec5; -fx-text-fill: #222; -fx-font-size: 15; -fx-background-radius: 9;"
                        + "-fx-effect: dropshadow(two-pass-box,#dadee3,1,0,0,1);"
        );
    }

    public KanbanCardModel showAndGetResult() {
        dialog.showAndWait();
        if (dialog.getResult() == ButtonType.OK) {
            return new KanbanCardModel(
                    nameField.getText(),
                    contactsField.getText(),
                    priceField.getText(),
                    taskArea.getText(),
                    paidBox.isSelected()
            );
        }
        return null;
    }
}