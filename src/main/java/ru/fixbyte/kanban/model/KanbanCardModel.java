package ru.fixbyte.kanban.model;

public class KanbanCardModel {
    public String column; // "todo", "inprogress", "done"
    public String name;
    public String contacts;
    public String price;
    public String task;
    public boolean paid;

    public KanbanCardModel(String column, String name, String contacts, String price, boolean paid) {
        this.column = column;
        this.name = name;
        this.contacts = contacts;
        this.price = price;
        this.task = task;
        this.paid = paid;
    }
}