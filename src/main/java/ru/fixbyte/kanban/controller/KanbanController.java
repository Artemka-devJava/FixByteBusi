package ru.fixbyte.kanban.controller;

import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import ru.fixbyte.kanban.model.KanbanCardModel;
import ru.fixbyte.kanban.service.KanbanFileService;
import ru.fixbyte.kanban.view.KanbanCardEditDialog;
import ru.fixbyte.kanban.view.KanbanCardView;

import java.util.List;
import java.util.ArrayList;

public class KanbanController {
    private final VBox todoColumn;
    private final VBox inProgressColumn;
    private final VBox doneColumn;
    private final Button addTodoCardButton;
    private final StackPane todoWrap, inProgressWrap, doneWrap, archiveArea;
    private final KanbanFileService fileService = new KanbanFileService();
    private final String kanbanPath = ru.fixbyte.model.CompanySettings.getKanbanSavePath();
    private final String archivePath = ru.fixbyte.model.CompanySettings.getKanbanArchivePath();

    // Стили и drag over.
    private static final String TODO_BASE_STYLE = "-fx-background-color: #f8f9fa; -fx-background-radius: 13;";
    private static final String INPROG_BASE_STYLE = "-fx-background-color: #e3f2fd; -fx-background-radius: 13;";
    private static final String DONE_BASE_STYLE = "-fx-background-color: #e8f5e9; -fx-background-radius: 13;";
    private static final String ARCHIVE_BASE_STYLE = "-fx-background-color: #3498db; -fx-background-radius: 27;";
    private static final String TODO_DRAG_OVER = "-fx-background-color: #ffe3e3; -fx-background-radius: 13;";
    private static final String INPROG_DRAG_OVER = "-fx-background-color: #e3ffd2; -fx-background-radius: 13;";
    private static final String DONE_DRAG_OVER = "-fx-background-color: #d2e3ff; -fx-background-radius: 13;";
    private static final String ARCHIVE_DRAG_OVER = "-fx-background-color: #ffe598; -fx-background-radius: 27;";

    public KanbanController(
            VBox todoColumn, VBox inProgressColumn, VBox doneColumn,
            Button addTodoCardButton, StackPane todoWrap, StackPane inProgressWrap,
            StackPane doneWrap, StackPane archiveArea
    ) {
        this.todoColumn = todoColumn;
        this.inProgressColumn = inProgressColumn;
        this.doneColumn = doneColumn;
        this.addTodoCardButton = addTodoCardButton;
        this.todoWrap = todoWrap;
        this.inProgressWrap = inProgressWrap;
        this.doneWrap = doneWrap;
        this.archiveArea = archiveArea;

        todoWrap.setStyle(TODO_BASE_STYLE);
        inProgressWrap.setStyle(INPROG_BASE_STYLE);
        doneWrap.setStyle(DONE_BASE_STYLE);
        archiveArea.setStyle(ARCHIVE_BASE_STYLE);

        setupDnDKanban(todoWrap, todoColumn, TODO_DRAG_OVER, TODO_BASE_STYLE);
        setupDnDKanban(inProgressWrap, inProgressColumn, INPROG_DRAG_OVER, INPROG_BASE_STYLE);
        setupDnDKanban(doneWrap, doneColumn, DONE_DRAG_OVER, DONE_BASE_STYLE);
        setupArchiveDnD(archiveArea, ARCHIVE_DRAG_OVER, ARCHIVE_BASE_STYLE);

        restoreHeaderAndAddButton();

        addTodoCardButton.setOnAction(e ->
                addCardToList(todoColumn, new KanbanCardModel("todo", "Новое имя", "Контакты", "0.00", false)));

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

    private void addCardToList(VBox column, KanbanCardModel model) {
        String columnType = getColumnType(column);
        final KanbanCardView[] cardRef = new KanbanCardView[1];
        KanbanCardView card = new KanbanCardView(
                model,
                () -> {
                    Pane parent = (Pane) cardRef[0].getParent();
                    if (parent != null) {
                        parent.getChildren().remove(cardRef[0]);
                        saveKanbanToFile();
                    }
                },
                () -> editCard(cardRef[0]),
                this::saveKanbanToFile
        );
        cardRef[0] = card;
        column.getChildren().add(card);
        cardRef[0].toModelWithColumn(columnType); // можно не присваивать, тк на сохранении будет корректно
        saveKanbanToFile();
    }

    private String getColumnType(VBox column) {
        if (column == todoColumn) return "todo";
        if (column == inProgressColumn) return "inprogress";
        if (column == doneColumn) return "done";
        return "todo";
    }

    public void loadKanbanFromFile() {
        todoColumn.getChildren().removeIf(n -> n instanceof KanbanCardView);
        inProgressColumn.getChildren().removeIf(n -> n instanceof KanbanCardView);
        doneColumn.getChildren().removeIf(n -> n instanceof KanbanCardView);
        for (KanbanCardModel c : fileService.loadCardsFromFile(kanbanPath)) {
            switch (c.column) {
                case "todo" -> addCardToList(todoColumn, c);
                case "inprogress" -> addCardToList(inProgressColumn, c);
                case "done" -> addCardToList(doneColumn, c);
                default -> addCardToList(todoColumn, c);
            }
        }
        restoreHeaderAndAddButton();
    }

    public void saveKanbanToFile() {
        List<KanbanCardModel> all = new ArrayList<>();
        for (var n : todoColumn.getChildren())
            if (n instanceof KanbanCardView)
                all.add(((KanbanCardView) n).toModelWithColumn("todo"));
        for (var n : inProgressColumn.getChildren())
            if (n instanceof KanbanCardView)
                all.add(((KanbanCardView) n).toModelWithColumn("inprogress"));
        for (var n : doneColumn.getChildren())
            if (n instanceof KanbanCardView)
                all.add(((KanbanCardView) n).toModelWithColumn("done"));
        fileService.saveCardsToFile(all, kanbanPath);
    }

    private void editCard(KanbanCardView cardView) {
        KanbanCardModel before = cardView.toModelWithColumn(""); // column неважен при редактировании
        KanbanCardEditDialog editDialog = new KanbanCardEditDialog(before);
        KanbanCardModel after = editDialog.showAndGetResult();
        if (after != null) {
            cardView.updateFromModel(after);
            saveKanbanToFile();
        }
    }

    private void setupDnDKanban(StackPane wrapper, VBox column, String dragOverColor, String baseStyle) {
        wrapper.setOnDragOver(event -> {
            if (event.getGestureSource() != wrapper && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                wrapper.setStyle(dragOverColor);
            }
            event.consume();
        });
        wrapper.setOnDragExited(e -> wrapper.setStyle(baseStyle));
        wrapper.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                KanbanCardView card = (KanbanCardView) event.getGestureSource();
                Pane parent = (Pane) card.getParent();
                if (parent != null) parent.getChildren().remove(card);
                column.getChildren().add(card);
                saveKanbanToFile();
                success = true;
            }
            event.setDropCompleted(success);
            wrapper.setStyle(baseStyle);
            event.consume();
        });
    }

    private void setupArchiveDnD(StackPane archiveArea, String dragOverColor, String baseStyle) {
        archiveArea.setOnDragOver(event -> {
            if (event.getGestureSource() != archiveArea && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                archiveArea.setStyle(dragOverColor);
            }
            event.consume();
        });
        archiveArea.setOnDragExited(e -> archiveArea.setStyle(baseStyle));
        archiveArea.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                KanbanCardView card = (KanbanCardView) event.getGestureSource();
                Pane parent = (Pane) card.getParent();
                if (parent != null) {
                    parent.getChildren().remove(card);
                    fileService.archiveCard(card.toModelWithColumn(""), archivePath);
                    removeCardFromKanbanFile(card);
                }
                success = true;
            }
            event.setDropCompleted(success);
            archiveArea.setStyle(baseStyle);
            event.consume();
        });
    }

    private static boolean safeEquals(String s1, String s2) {
        return (s1 == null && s2 == null) || (s1 != null && s1.equals(s2));
    }

    private void removeCardFromKanbanFile(KanbanCardView card) {
        List<KanbanCardModel> all = fileService.loadCardsFromFile(kanbanPath);
        KanbanCardModel toRemove = card.toModelWithColumn(""); // column неважно для поиска
        all.removeIf(m -> safeEquals(m.name, toRemove.name)
                && safeEquals(m.contacts, toRemove.contacts)
                && safeEquals(m.price, toRemove.price)
                && safeEquals(m.task, toRemove.task)
                && m.paid == toRemove.paid);
        fileService.saveCardsToFile(all, kanbanPath);
    }
}