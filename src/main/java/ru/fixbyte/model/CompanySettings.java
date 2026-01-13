package ru.fixbyte.model;

import java.io.*;
import java.util.Properties;

public class CompanySettings {
    private static final String SETTINGS_FILE = "settings.properties";
    private static Properties properties = new Properties();

    // Значения по умолчанию
    private static String companyName = "ООО \"Ваша Компания\"";
    private static String address = "г. Москва, ул. Примерная, д. 1";
    private static String phone = "+7 (999) 123-45-67";
    private static String inn = "1234567890";
    private static String logoPath = "/logo.png";

    // Загружаем настройки при старте
    static {
        loadSettings();
    }

    // Загрузка настроек из файла
    public static void loadSettings() {
        try {
            File file = new File(SETTINGS_FILE);
            if (file. exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    properties.load(fis);

                    companyName = properties.getProperty("companyName", companyName);
                    address = properties.getProperty("address", address);
                    phone = properties.getProperty("phone", phone);
                    inn = properties.getProperty("inn", inn);
                    logoPath = properties.getProperty("logoPath", logoPath);

                    System.out.println("Настройки загружены из файла");
                }
            } else {
                System.out. println("Файл настроек не найден, используются значения по умолчанию");
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки настроек: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Сохранение настроек в файл
    public static void saveSettings() {
        try {
            properties.setProperty("companyName", companyName);
            properties. setProperty("address", address);
            properties.setProperty("phone", phone);
            properties.setProperty("inn", inn);
            properties.setProperty("logoPath", logoPath);

            try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
                properties.store(fos, "Company Settings");
                System.out. println("Настройки сохранены в файл:  " + SETTINGS_FILE);
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения настроек:  " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters
    public static String getCompanyName() { return companyName; }
    public static String getAddress() { return address; }
    public static String getPhone() { return phone; }
    public static String getInn() { return inn; }
    public static String getLogoPath() { return logoPath; }

    // Setters
    public static void setCompanyName(String name) {
        companyName = name;
        saveSettings(); // Автоматическое сохранение
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
}