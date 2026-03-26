package ru.fixbyte.notes.controller;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import ru.fixbyte.notes.model.NoteItem;
import ru.fixbyte.notes.service.NotesFileService;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Контроллер вкладки "Заметки".
 * Слева — дерево папок и .md файлов, справа — текстовый редактор.
 * Сохранение по кнопке или Ctrl+S. Папка хранения: data/notes.
 */
public class NotesController {

    public final Tab tab;

    private final NotesFileService fileService = new NotesFileService();
    private final TreeView<NoteItem> treeView   = new TreeView<>();
    private final TextArea editorArea           = new TextArea();
    private final Label statusLabel             = new Label("Выберите заметку для редактирования");

    private File currentFile      = null;
    private boolean unsavedChanges = false;

    public NotesController() {
        // --- Панель инструментов ---
        Button newFolderBtn = makeButton("📁 Папка",        "#3498db");
        Button newFileBtn   = makeButton("📄 Заметка",      "#27ae60");
        Button saveBtn      = makeButton("💾 Сохранить",    "#e67e22");
        Button renameBtn    = makeButton("✏ Переименовать", "#9b59b6");
        Button deleteBtn    = makeButton("🗑 Удалить",      "#e74c3c");

        HBox toolbar = new HBox(8, newFolderBtn, newFileBtn, saveBtn, renameBtn, deleteBtn);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #d5d8dc; -fx-border-width: 0 0 1 0;");

        // --- Дерево файлов ---
        treeView.setShowRoot(false);
        treeView.setMinWidth(200);
        treeView.setPrefWidth(260);
        treeView.setStyle("-fx-background-color: #f8f9fa;");
        treeView.setCellFactory(tv -> new NoteTreeCell());

        // --- Редактор ---
        editorArea.setWrapText(true);
        editorArea.setPromptText("Выберите файл для редактирования...");
        editorArea.setDisable(true);
        editorArea.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                "-fx-font-size: 14px;" +
                "-fx-background-color: #fdfefe;"
        );
        VBox.setVgrow(editorArea, Priority.ALWAYS);

        // --- Разделитель ---
        SplitPane splitPane = new SplitPane(treeView, editorArea);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.27);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // --- Строка статуса ---
        statusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(4, 10, 4, 10));
        statusBar.setStyle("-fx-background-color: #f5f6fa; -fx-border-color: #d5d8dc; -fx-border-width: 1 0 0 0;");

        // --- Корневой контейнер ---
        VBox layout = new VBox(toolbar, splitPane, statusBar);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        tab = new Tab("Заметки", layout);
        tab.setClosable(false);

        // --- Обработчики кнопок ---
        newFolderBtn.setOnAction(e -> onCreateFolder());
        newFileBtn  .setOnAction(e -> onCreateFile());
        saveBtn     .setOnAction(e -> saveCurrentFile());
        renameBtn   .setOnAction(e -> onRename());
        deleteBtn   .setOnAction(e -> onDelete());

        // Выбор элемента в дереве
        treeView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> onTreeSelectionChanged(newVal));

        // Отслеживание изменений в редакторе
        editorArea.textProperty().addListener((obs, oldText, newText) -> {
            if (currentFile != null) unsavedChanges = true;
        });

        // Ctrl+S — сохранить
        editorArea.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.S) saveCurrentFile();
        });

        refreshTree();
    }

    // -------------------------------------------------------------------------
    // Дерево файлов
    // -------------------------------------------------------------------------

    private void refreshTree() {
        File root = fileService.getNotesRoot();
        TreeItem<NoteItem> rootItem = buildTreeItem(root);
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
    }

    private TreeItem<NoteItem> buildTreeItem(File file) {
        NoteItem.Type type = file.isDirectory() ? NoteItem.Type.FOLDER : NoteItem.Type.FILE;
        TreeItem<NoteItem> item = new TreeItem<>(new NoteItem(file.getName(), type, file));
        item.setExpanded(true);

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                java.util.Arrays.sort(children, (a, b) -> {
                    if (a.isDirectory() != b.isDirectory())
                        return a.isDirectory() ? -1 : 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                });
                for (File child : children) {
                    item.getChildren().add(buildTreeItem(child));
                }
            }
        }
        return item;
    }

    private void onTreeSelectionChanged(TreeItem<NoteItem> selected) {
        if (selected == null || selected.getValue().isFolder()) {
            if (!editorArea.isDisabled()) {
                promptSaveIfNeeded();
                editorArea.clear();
                editorArea.setDisable(true);
                currentFile = null;
            }
            setStatus("Выберите заметку для редактирования");
            return;
        }
        openFile(selected.getValue().getFile());
    }

    private void openFile(File file) {
        if (file.equals(currentFile)) return;
        promptSaveIfNeeded();
        try {
            String content = fileService.readFile(file);
            editorArea.setDisable(false);
            editorArea.setText(content);
            currentFile = file;
            unsavedChanges = false;
            setStatus("📝  " + file.getName());
        } catch (IOException e) {
            showError("Ошибка открытия файла: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Действия
    // -------------------------------------------------------------------------

    private void onCreateFolder() {
        File parentDir = getSelectedDirectory();
        askName("Новая папка", "Название папки:").ifPresent(name -> {
            try {
                fileService.createFolder(parentDir, name);
                refreshTree();
                setStatus("Папка создана: " + name);
            } catch (IOException e) {
                showError(e.getMessage());
            }
        });
    }

    private void onCreateFile() {
        File parentDir = getSelectedDirectory();
        askName("Новая заметка", "Название файла (расширение .md добавится автоматически):").ifPresent(name -> {
            try {
                File created = fileService.createFile(parentDir, name);
                refreshTree();
                selectInTree(treeView.getRoot(), created);
                setStatus("Заметка создана: " + created.getName());
            } catch (IOException e) {
                showError(e.getMessage());
            }
        });
    }

    private void saveCurrentFile() {
        if (currentFile == null) return;
        try {
            fileService.saveFile(currentFile, editorArea.getText());
            unsavedChanges = false;
            setStatus("✅  Сохранено: " + currentFile.getName());
        } catch (IOException e) {
            showError("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void onRename() {
        TreeItem<NoteItem> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected == treeView.getRoot()) return;
        File oldFile = selected.getValue().getFile();

        TextInputDialog dialog = new TextInputDialog(oldFile.getName());
        dialog.setTitle("Переименовать");
        dialog.setHeaderText("Новое название для: " + oldFile.getName());
        dialog.setContentText("Название:");
        dialog.showAndWait()
                .filter(name -> !name.isBlank() && !name.equals(oldFile.getName()))
                .ifPresent(newName -> {
                    File newFile = new File(oldFile.getParent(), newName);
                    if (oldFile.renameTo(newFile)) {
                        if (oldFile.equals(currentFile)) currentFile = newFile;
                        refreshTree();
                        selectInTree(treeView.getRoot(), newFile);
                        setStatus("Переименовано: " + newName);
                    } else {
                        showError("Не удалось переименовать файл");
                    }
                });
    }

    private void onDelete() {
        TreeItem<NoteItem> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected == treeView.getRoot()) return;
        NoteItem item = selected.getValue();
        String what = item.isFolder() ? "папку" : "файл";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить " + what + " «" + item.getFile().getName() + "»?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Удалить");
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (item.getFile().equals(currentFile)) {
                editorArea.clear();
                editorArea.setDisable(true);
                currentFile = null;
                unsavedChanges = false;
            }
            fileService.delete(item.getFile());
            refreshTree();
            setStatus("Удалено: " + item.getFile().getName());
        }
    }

    // -------------------------------------------------------------------------
    // Вспомогательные методы
    // -------------------------------------------------------------------------

    /** Предлагает сохранить несохранённые изменения перед сменой файла. */
    private void promptSaveIfNeeded() {
        if (!unsavedChanges || currentFile == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Сохранить изменения в «" + currentFile.getName() + "»?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Несохранённые изменения");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            saveCurrentFile();
        }
    }

    /** Возвращает директорию для создания нового элемента (из выделения в дереве). */
    private File getSelectedDirectory() {
        TreeItem<NoteItem> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected == treeView.getRoot()) {
            return fileService.getNotesRoot();
        }
        NoteItem item = selected.getValue();
        return item.isFolder() ? item.getFile() : item.getFile().getParentFile();
    }

    /** Ищет и выделяет элемент дерева по файлу. */
    private boolean selectInTree(TreeItem<NoteItem> item, File target) {
        if (item == null) return false;
        if (target.equals(item.getValue().getFile())) {
            treeView.getSelectionModel().select(item);
            return true;
        }
        for (TreeItem<NoteItem> child : item.getChildren()) {
            if (selectInTree(child, target)) return true;
        }
        return false;
    }

    private Optional<String> askName(String title, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);
        return dialog.showAndWait().filter(s -> !s.isBlank());
    }

    private void setStatus(String text) { statusLabel.setText(text); }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private Button makeButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 7;" +
                "-fx-font-size: 13px;"
        );
        return btn;
    }

    // -------------------------------------------------------------------------
    // Ячейка дерева с иконками
    // -------------------------------------------------------------------------

    private static class NoteTreeCell extends TreeCell<NoteItem> {
        @Override
        protected void updateItem(NoteItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                String icon = item.isFolder() ? "📁  " : "📝  ";
                setText(icon + item.getName());
                setStyle(item.isFolder()
                        ? "-fx-font-weight: bold; -fx-text-fill: #2d3436;"
                        : "-fx-font-weight: normal; -fx-text-fill: #636e72;");
            }
        }
    }
}

