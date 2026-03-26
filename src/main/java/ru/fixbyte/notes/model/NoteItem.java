package ru.fixbyte.notes.model;

import java.io.File;

/**
 * Модель элемента в дереве заметок: файл (.md) или папка.
 */
public class NoteItem {

    public enum Type { FOLDER, FILE }

    private final String name;
    private final Type type;
    private final File file;

    public NoteItem(String name, Type type, File file) {
        this.name = name;
        this.type = type;
        this.file = file;
    }

    public String getName() { return name; }
    public Type getType()   { return type; }
    public File getFile()   { return file; }
    public boolean isFolder() { return type == Type.FOLDER; }

    @Override
    public String toString() { return name; }
}

