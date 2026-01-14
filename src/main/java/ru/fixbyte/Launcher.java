package ru.fixbyte;

/**
 * Launcher class для JavaFX приложения
 * Необходим для запуска JavaFX из fat JAR
 */
public class Launcher {
    public static void main(String[] args) {
        // Запускаем настоящий Main класс
        Main.main(args);
    }
}