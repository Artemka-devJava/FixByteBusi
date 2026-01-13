package ru.fixbyte.model;

import java.io.*;
import java.util.Properties;

public class CompanySettings {
    private static String companyName = "ООО \"Ваша Компания\"";
    private static String address = "г. Москва, ул. Примерная, д. 1";
    private static String phone = "+7 (999) 123-45-67";
    private static String inn = "1234567890";
    private static String logoPath = "/logo.png";
    private static boolean showInn = true;
    private static boolean showBuyerSignature = true;

    // Размеры окна
    private static int windowWidth = 800;  // Новое поле
    private static int windowHeight = 600; // Новое поле

    // Загрузка настроек из файла
    public static void loadSettings() {
        try {
            File file = new File("settings.properties");
            if (file.exists()) {
                Properties properties = new Properties();
                try (FileInputStream fis = new FileInputStream(file)) {
                    properties.load(fis);

                    companyName = properties.getProperty("companyName", companyName);
                    address = properties.getProperty("address", address);
                    phone = properties.getProperty("phone", phone);
                    inn = properties.getProperty("inn", inn);
                    logoPath = properties.getProperty("logoPath", logoPath);
                    showInn = Boolean.parseBoolean(properties.getProperty("showInn", "true"));
                    showBuyerSignature = Boolean.parseBoolean(properties.getProperty("showBuyerSignature", "true"));

                    // Загружаем размеры окна
                    try {
                        windowWidth = Integer. parseInt(properties.getProperty("windowWidth", "800"));
                        windowHeight = Integer.parseInt(properties.getProperty("windowHeight", "600"));

                        // Валидация размеров (минимум 600x400, максимум размер экрана)
                        windowWidth = Math.max(600, Math.min(windowWidth, 1920));
                        windowHeight = Math.max(400, Math.min(windowHeight, 1080));
                    } catch (NumberFormatException e) {
                        windowWidth = 800;
                        windowHeight = 600;
                    }

                    System.out.println("Настройки загружены из файла");
                }
            } else {
                System.out. println("Файл настроек не найден, используются значения по умолчанию");
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
            properties. setProperty("address", address);
            properties.setProperty("phone", phone);
            properties.setProperty("inn", inn);
            properties.setProperty("logoPath", logoPath);
            properties.setProperty("showInn", String.valueOf(showInn));
            properties.setProperty("showBuyerSignature", String.valueOf(showBuyerSignature));
            properties.setProperty("windowWidth", String.valueOf(windowWidth));
            properties.setProperty("windowHeight", String.valueOf(windowHeight));

            try (FileOutputStream fos = new FileOutputStream("settings.properties")) {
                properties.store(fos, "Company Settings");
                System.out. println("Настройки сохранены в файл:  settings.properties");
            }
        } catch (Exception e) {
            System.err.println("Ошибка сохранения настроек: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters
    public static String getCompanyName() { return companyName; }
    public static String getAddress() { return address; }
    public static String getPhone() { return phone; }
    public static String getInn() { return inn; }
    public static String getLogoPath() { return logoPath; }
    public static boolean isShowInn() { return showInn; }
    public static boolean isShowBuyerSignature() { return showBuyerSignature; }
    public static int getWindowWidth() { return windowWidth; }   // Новый getter
    public static int getWindowHeight() { return windowHeight; } // Новый getter

    // Setters
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

    public static void setWindowWidth(int width) {  // Новый setter
        windowWidth = Math.max(600, Math.min(width, 1920));
        saveSettings();
    }

    public static void setWindowHeight(int height) { // Новый setter
        windowHeight = Math.max(400, Math. min(height, 1080));
        saveSettings();
    }

    // Метод для установки обоих размеров сразу
    public static void setWindowSize(int width, int height) {
        windowWidth = Math. max(600, Math.min(width, 1920));
        windowHeight = Math.max(400, Math.min(height, 1080));
        saveSettings();
    }
}