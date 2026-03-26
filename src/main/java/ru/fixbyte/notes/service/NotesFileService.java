package ru.fixbyte.notes.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Сервис для работы с файлами заметок в папке data/notes.
 */
public class NotesFileService {

    private static final String NOTES_DIR = "data/notes";

    /** Возвращает корневую папку заметок, создаёт её при необходимости. */
    public File getNotesRoot() {
        File dir = new File(NOTES_DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** Читает содержимое файла в UTF-8. */
    public String readFile(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /** Сохраняет текст в файл в UTF-8. */
    public void saveFile(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }

    /**
     * Создаёт новый .md файл в указанной папке.
     * Автоматически добавляет расширение .md если его нет.
     */
    public File createFile(File parent, String name) throws IOException {
        String fileName = name.endsWith(".md") ? name : name + ".md";
        File file = new File(parent, fileName);
        if (file.exists()) {
            throw new IOException("Файл уже существует: " + fileName);
        }
        if (!parent.exists()) parent.mkdirs();
        file.createNewFile();
        return file;
    }

    /** Создаёт новую папку в указанной директории. */
    public File createFolder(File parent, String name) throws IOException {
        File folder = new File(parent, name);
        if (folder.exists()) {
            throw new IOException("Папка уже существует: " + name);
        }
        if (!folder.mkdirs()) {
            throw new IOException("Не удалось создать папку: " + name);
        }
        return folder;
    }

    /** Удаляет файл или папку рекурсивно. */
    public void delete(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) delete(child);
            }
        }
        file.delete();
    }
}

