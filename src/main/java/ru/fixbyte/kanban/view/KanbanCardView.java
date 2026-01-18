package ru.fixbyte.kanban.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import ru.fixbyte.kanban.model.KanbanCardModel;

public class KanbanCardView extends HBox {
    private final Label nameLbl;
    private final Label priceLbl;
    private final Label contactLbl;
    private final CheckBox paidBox;
    private final VBox infoBox;
    private String taskText;

    private Runnable onDelete, onEdit, onChanged;

    public KanbanCardView(KanbanCardModel model, Runnable onDelete, Runnable onEdit, Runnable onChanged) {
        super(16);
        this.onDelete = onDelete;
        this.onEdit = onEdit;
        this.onChanged = onChanged;
        setStyle(CARD_STYLE);

        ImageView iconView = new ImageView();
        try {
            Image icon = new Image(getClass().getResourceAsStream("/logomain.png"));
            iconView.setImage(icon);
        } catch (Exception ignore) {}
        iconView.setFitWidth(36);
        iconView.setFitHeight(36);

        infoBox = new VBox(6);
        infoBox.setPrefWidth(170);

        nameLbl = new Label("Имя: " + model.name);
        nameLbl.setStyle("-fx-font-family:'Arial';-fx-font-weight:bold;-fx-font-size: 15; -fx-text-fill: #34495e;");
        priceLbl = new Label("Цена: " + model.price + " руб.");
        priceLbl.setStyle("-fx-font-family:'Arial';-fx-text-fill: #27ae60;-fx-font-size: 14;");
        contactLbl = new Label("Контакты: " + model.contacts);
        contactLbl.setStyle("-fx-font-family:'Arial';-fx-text-fill: #888;-fx-font-size: 13;");
        paidBox = new CheckBox("Оплачено");
        paidBox.setSelected(model.paid);
        paidBox.setStyle("-fx-font-size: 13; -fx-font-family:'Arial'");

        paidBox.setOnAction(e -> { if (onChanged != null) onChanged.run(); });

        infoBox.getChildren().addAll(nameLbl, priceLbl, contactLbl, paidBox);

        Button delete = new Button("✖");
        delete.setStyle("-fx-background-color: #e74c3c;-fx-text-fill: white; -fx-font-size: 15; -fx-background-radius: 9;");
        delete.setOnAction(e -> { if (onDelete != null) onDelete.run(); });
        VBox btnBox = new VBox(delete);
        btnBox.setAlignment(Pos.TOP_RIGHT);

        getChildren().addAll(iconView, infoBox, btnBox);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        setAccessibleText(model.task);
        this.taskText = model.task;

        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && onEdit != null) onEdit.run();
        });
        setOnDragDetected(e -> {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString("kanban-card");
            db.setContent(cc);
            setStyle(CARD_DRAGGED_STYLE);
            e.consume();
        });
        setOnDragDone(e -> setStyle(CARD_STYLE));
    }

    public void updateFromModel(KanbanCardModel model) {
        nameLbl.setText("Имя: " + model.name);
        priceLbl.setText("Цена: " + model.price + " руб.");
        contactLbl.setText("Контакты: " + model.contacts);
        paidBox.setSelected(model.paid);
        setAccessibleText(model.task);
        this.taskText = model.task;
    }

    // Всегда требует column как параметр!
    public KanbanCardModel toModelWithColumn(String column) {
        return new KanbanCardModel(
                column,
                nameLbl.getText().replaceFirst("Имя: ", ""),
                contactLbl.getText().replaceFirst("Контакты: ", ""),
                priceLbl.getText().replaceFirst("Цена: ", "").replace(" руб.", ""),
                paidBox.isSelected()
        );
    }

    // Геттеры для диалога редактирования
    public String getNameValue() { return nameLbl.getText().replaceFirst("Имя: ", ""); }
    public String getContactsValue() { return contactLbl.getText().replaceFirst("Контакты: ", ""); }
    public String getPriceValue() { return priceLbl.getText().replaceFirst("Цена: ", "").replace(" руб.", ""); }
    public boolean isPaidValue() { return paidBox.isSelected(); }
    public String getTaskValue() { return taskText == null ? "" : taskText; }
    public void setTaskValue(String t) {
        this.taskText = t;
        setAccessibleText(t);
    }

    // Стили
    private static final String CARD_STYLE =
            "-fx-background-color: linear-gradient(to bottom,#ecf0f1 75%,#fbfbfb);"
                    + " -fx-padding: 16 18 16 18; -fx-background-radius: 12;"
                    + " -fx-border-color: #dadee3; -fx-border-radius: 12;"
                    + " -fx-effect: dropshadow(two-pass-box,#b0bec5,2,0,0,2);";
    private static final String CARD_DRAGGED_STYLE =
            "-fx-background-color: #fffde7; -fx-padding: 16 18 16 18;"
                    + " -fx-background-radius: 12; -fx-border-color: #fbc02d; -fx-border-radius: 12;";
}