package ru.fixbyte.kanban.view;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import ru.fixbyte.kanban.model.KanbanCardModel;

public class KanbanCardView extends HBox {
    private final Label nameLbl;
    private final Label priceLbl;
    private final Label contactLbl;
    private final CheckBox paidBox;
    private String taskText;

    public KanbanCardView(KanbanCardModel model) {
        super(16);
        setStyle("-fx-background-color: linear-gradient(to bottom,#ecf0f1 75%,#fbfbfb);"
                + " -fx-padding: 16 18 16 18; -fx-background-radius: 12;"
                + " -fx-border-color: #dadee3; -fx-border-radius: 12;"
                + " -fx-effect: dropshadow(two-pass-box,#b0bec5,2,0,0,2);");
        setMinHeight(90);
        setMaxHeight(120);

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

        nameLbl = new Label("Имя: " + model.name);
        nameLbl.setStyle("-fx-font-family:'Arial'; -fx-font-weight:bold; -fx-font-size: 15; -fx-text-fill: #34495e;");
        priceLbl = new Label("Цена: " + model.price + " руб.");
        priceLbl.setStyle("-fx-font-family:'Arial'; -fx-text-fill: #27ae60; -fx-font-size: 14;");
        contactLbl = new Label("Контакты: " + model.contacts);
        contactLbl.setStyle("-fx-font-family:'Arial'; -fx-text-fill: #888; -fx-font-size: 13;");

        paidBox = new CheckBox("Оплачено");
        paidBox.setSelected(model.paid);
        paidBox.setStyle("-fx-font-size: 13; -fx-font-family:'Arial'");
        paidBox.setAlignment(Pos.CENTER_LEFT);

        infoBox.getChildren().addAll(nameLbl, priceLbl, contactLbl, paidBox);

        VBox btnBox = new VBox(); // Здесь можно добавить кнопки (удаление, редактировать...)

        btnBox.setAlignment(Pos.TOP_RIGHT);

        getChildren().addAll(iconView, infoBox, btnBox);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        setAccessibleText(model.task);
        this.taskText = model.task;
    }

    // Геттеры и сеттеры для обновления данных
    public String getName() { return nameLbl.getText().replaceFirst("Имя: ", ""); }
    public void setName(String name) { nameLbl.setText("Имя: " + name); }

    public String getContacts() { return contactLbl.getText().replaceFirst("Контакты: ", ""); }
    public void setContacts(String contacts) { contactLbl.setText("Контакты: " + contacts); }

    public String getPrice() { return priceLbl.getText().replaceFirst("Цена: ", "").replace(" руб.", ""); }
    public void setPrice(String price) { priceLbl.setText("Цена: " + price + " руб."); }

    public boolean isPaid() { return paidBox.isSelected(); }
    public void setPaid(boolean paid) { paidBox.setSelected(paid); }

    public String getTaskText() { return taskText; }
    public void setTaskText(String text) {
        this.taskText = text;
        setAccessibleText(text);
    }

    public KanbanCardModel toModel() {
        return new KanbanCardModel(getName(), getContacts(), getPrice(), getTaskText(), isPaid());
    }
}