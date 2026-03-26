package ru.fixbyte.kanban.controller;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ru.fixbyte.kanban.view.KanbanCardView;

import java.util.List;
import java.util.stream.Collectors;

public record KanbanColumnView(VBox columnBox, Label headerLabel, Button addButton) {

    public void addCard(KanbanCardView card) {
        columnBox.getChildren().add(card); // всегда в конец
    }

    public void removeCard(KanbanCardView card) {
        columnBox.getChildren().remove(card);
    }

    public List<KanbanCardView> getCards() {
        return columnBox.getChildren().stream().filter(n -> n instanceof KanbanCardView)
                .map(n -> (KanbanCardView) n).collect(Collectors.toList());
    }
}