package ru.fixbyte.kanban.model;

public class KanbanCardModel {
    public String name;
    public String contacts;
    public String price;
    public String task;
    public boolean paid;

    public KanbanCardModel(String name, String contacts, String price, String task, boolean paid) {
        this.name = name;
        this.contacts = contacts;
        this.price = price;
        this.task = task;
        this.paid = paid;
    }
}