package ru.fixbyte.kanban.model;

public class KanbanCardModel {
    public String column;
    public String name;
    public String contacts;
    public String price;
    public String task;
    public boolean paid;

    /**
     * Основной конструктор для работы Kanban-доски (используйте этот!)
     */
    public KanbanCardModel(String column, String name, String contacts, String price, String task, boolean paid) {
        this.column = column == null ? "" : column;
        this.name = name == null ? "" : name;
        this.contacts = contacts == null ? "" : contacts;
        this.price = price == null ? "" : price;
        this.task = task == null ? "" : task;
        this.paid = paid;
    }

    /**
     * Для совместимости со старыми вызовами (по умолчанию column="todo")
     */
    public KanbanCardModel(String name, String contacts, String price, String task, boolean paid) {
        this("todo", name, contacts, price, task, paid);
    }
}