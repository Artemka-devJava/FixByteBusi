package ru.fixbyte.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class CompanySettings {
    private static String companyName = "ООО \"ФиксБайт\"";
    private static String address = "г.Павлово, ул. Нижегородская 11А, 7";
    private static String phone = "+7 (996) 566-40-46";
    private static String inn = "1234567890";
    private static String logoPath = "/logomain.png";
    private static boolean showInn = true;
    private static boolean showBuyerSignature = true;
    private static String kanbanSavePath = "data/cards.txt";
    private static boolean showReceiptsTab = true;
    private static boolean showKanbanTab = true;
    private static boolean showMetrikaTab = true;
    private static boolean showNotesTab = true;

    // Размеры окна
    private static int windowWidth = 800;
    private static int windowHeight = 600;

    // --- Новые поля ---
    private static String receiptSaveDir = "";
    private static boolean autoSaveReceipts = true;

    // Авторизация
    private static boolean authEnabled = false;
    private static String authLogin = "admin";
    // SHA-256("admin")
    private static String authPasswordHash = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918";

    // Загрузка настроек из файла
    public static void loadSettings() {
        try {
            File file = new File("settings.properties");
            if (file.exists()) {
                Properties properties = new Properties();
                try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    properties.load(reader);

                    companyName = properties.getProperty("companyName", companyName);
                    address = properties.getProperty("address", address);
                    phone = properties.getProperty("phone", phone);
                    inn = properties.getProperty("inn", inn);
                    logoPath = properties.getProperty("logoPath", logoPath);
                    showInn = Boolean.parseBoolean(properties.getProperty("showInn", "true"));
                    showBuyerSignature = Boolean.parseBoolean(properties.getProperty("showBuyerSignature", "true"));
                    kanbanSavePath = properties.getProperty("kanban_save_path", kanbanSavePath);
                    showReceiptsTab = Boolean.parseBoolean(properties.getProperty("show_receipts_tab", "true"));
                    showKanbanTab = Boolean.parseBoolean(properties.getProperty("show_kanban_tab", "true"));
                    showMetrikaTab = Boolean.parseBoolean(properties.getProperty("show_metrika_tab", "true"));
                    showNotesTab = Boolean.parseBoolean(properties.getProperty("show_notes_tab", "true"));

                    // Размеры окна
                    try {
                        windowWidth = Integer.parseInt(properties.getProperty("windowWidth", "800"));
                        windowHeight = Integer.parseInt(properties.getProperty("windowHeight", "600"));
                        windowWidth = Math.max(600, Math.min(windowWidth, 1920));
                        windowHeight = Math.max(400, Math.min(windowHeight, 1080));
                    } catch (NumberFormatException e) {
                        windowWidth = 800;
                        windowHeight = 600;
                    }

                    // Новые параметры
                    receiptSaveDir = properties.getProperty("receipt_save_dir", "");
                    autoSaveReceipts = Boolean.parseBoolean(properties.getProperty("auto_save_receipts", "true"));

                    authEnabled = Boolean.parseBoolean(properties.getProperty("auth_enabled", "false"));
                    authLogin = properties.getProperty("auth_login", "admin");
                    authPasswordHash = properties.getProperty("auth_password_hash", authPasswordHash);

                    System.out.println("Настройки загружены из файла");
                }
            } else {
                System.out.println("Файл настроек не найден, используются значения по умолчанию");
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки настроек: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Сохранение настроек в файл
    public static void saveSettings() {
        try {
            Properties properties = new Properties();
            properties.setProperty("companyName", companyName);
            properties.setProperty("address", address);
            properties.setProperty("phone", phone);
            properties.setProperty("inn", inn);
            properties.setProperty("logoPath", logoPath);
            properties.setProperty("showInn", String.valueOf(showInn));
            properties.setProperty("showBuyerSignature", String.valueOf(showBuyerSignature));
            properties.setProperty("show_receipts_tab", String.valueOf(showReceiptsTab));
            properties.setProperty("show_kanban_tab", String.valueOf(showKanbanTab));
            properties.setProperty("show_metrika_tab", String.valueOf(showMetrikaTab));
            properties.setProperty("show_notes_tab", String.valueOf(showNotesTab));
            properties.setProperty("windowWidth", String.valueOf(windowWidth));
            properties.setProperty("windowHeight", String.valueOf(windowHeight));
            properties.setProperty("receipt_save_dir", receiptSaveDir == null ? "" : receiptSaveDir);
            properties.setProperty("auto_save_receipts", String.valueOf(autoSaveReceipts));
            properties.setProperty("kanban_save_path", kanbanSavePath == null ? "" : kanbanSavePath);

            properties.setProperty("auth_enabled", String.valueOf(authEnabled));
            properties.setProperty("auth_login", authLogin == null ? "" : authLogin);
            properties.setProperty("auth_password_hash", authPasswordHash == null ? "" : authPasswordHash);

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("settings.properties"), StandardCharsets.UTF_8)) {
                properties.store(writer, "Company Settings");
                System.out.println("Настройки сохранены в файл:  settings.properties");
            }
        } catch (Exception e) {
            System.err.println("Ошибка сохранения настроек: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static String getKanbanArchivePath() {
        return "data/kanban-archive.txt";
    }

    // --- Геттеры ---
    public static String getCompanyName() { return companyName; }
    public static String getAddress() { return address; }
    public static String getPhone() { return phone; }
    public static String getInn() { return inn; }
    public static String getLogoPath() { return logoPath; }
    public static boolean isShowInn() { return showInn; }
    public static boolean isShowBuyerSignature() { return showBuyerSignature; }
    public static int getWindowWidth() { return windowWidth; }
    public static int getWindowHeight() { return windowHeight; }

    public static String getReceiptSaveDir() { return receiptSaveDir; }
    public static boolean isAutoSaveReceipts() { return autoSaveReceipts; }
    public static String getKanbanSavePath() { return kanbanSavePath; }
    public static boolean isShowReceiptsTab() { return showReceiptsTab; }
    public static boolean isShowKanbanTab() { return showKanbanTab; }
    public static boolean isShowMetrikaTab() { return showMetrikaTab; }
    public static boolean isShowNotesTab() { return showNotesTab; }

    public static boolean isAuthEnabled() { return authEnabled; }
    public static String getAuthLogin() { return authLogin; }

    // --- Сеттеры ---

    public static void setCompanyName(String name) {
        companyName = name;
        saveSettings();
    }
    public static void setAddress(String addr) {
        address = addr;
        saveSettings();
    }
    public static void setPhone(String ph) {
        phone = ph;
        saveSettings();
    }
    public static void setInn(String i) {
        inn = i;
        saveSettings();
    }
    public static void setLogoPath(String path) {
        logoPath = path;
        saveSettings();
    }
    public static void setShowInn(boolean show) {
        showInn = show;
        saveSettings();
    }
    public static void setShowBuyerSignature(boolean show) {
        showBuyerSignature = show;
        saveSettings();
    }


    public static void setWindowSize(int width, int height) {
        windowWidth = Math.max(600, Math.min(width, 1920));
        windowHeight = Math.max(400, Math.min(height, 1080));
        saveSettings();
    }
    public static void setReceiptSaveDir(String dir) {
        receiptSaveDir = (dir == null ? "" : dir);
        saveSettings();
    }
    public static void setAutoSaveReceipts(boolean enable) {
        autoSaveReceipts = enable;
        saveSettings();
    }

    public static void setShowReceiptsTab(boolean show) {
        showReceiptsTab = show;
        saveSettings();
    }

    public static void setShowKanbanTab(boolean show) {
        showKanbanTab = show;
        saveSettings();
    }

    public static void setShowMetrikaTab(boolean show) {
        showMetrikaTab = show;
        saveSettings();
    }

    public static void setShowNotesTab(boolean show) {
        showNotesTab = show;
        saveSettings();
    }

    public static void setAuthEnabled(boolean enabled) {
        authEnabled = enabled;
        saveSettings();
    }

    public static void setAuthLogin(String login) {
        authLogin = login == null ? "" : login.trim();
        saveSettings();
    }

    public static void setAuthPasswordPlain(String password) {
        authPasswordHash = hashPassword(password == null ? "" : password);
        saveSettings();
    }

    public static boolean verifyCredentials(String login, String password) {
        String loginSafe = login == null ? "" : login.trim();
        String passSafe = password == null ? "" : password;
        return loginSafe.equals(authLogin) && hashPassword(passSafe).equals(authPasswordHash);
    }

    public static boolean hasAuthPassword() {
        return authPasswordHash != null && !authPasswordHash.isBlank();
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 недоступен", e);
        }
    }
}