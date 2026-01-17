# 🧾 FixByteBusi - Товарные чеки

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/downloads/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()
[![Version](https://img.shields.io/badge/Version-0.1.2-green.svg)](https://github.com/Artemka-devJava/FixByteBusi/releases)

# FixByteBusi

**FixByteBusi** — приложение для автоматизации торговли и управления заказами на JavaFX.

## Особенности

- **Товарные чеки и касса**
   - Добавление товаров в чек, расчет итоговой суммы
   - Печать и автосохранение чеков
   - Каталог товаров: добавление/редактирование/удаление

- **Kanban-доска заказов**
   - Перетаскивание карточек по статусам
   - Карточка содержит: Имя, Контакты, Цена, чекбокс "Оплачено"
   - Мгновенное сохранение изменений

- **Гибкие настройки**
   - Реквизиты, логотип, пути к файлам

- **Удобный интерфейс на русском языке**

---

## Быстрый старт

1. Установите **Java 17** или новее
2. Склонируйте репозиторий:
   ```
   git clone https://github.com/Artemka-devJava/FixByteBusi.git
   ```
3. Откройте проект в вашей IDE (например, IntelliJ IDEA)
4. Запустите:
   - главный класс `ru.fixbyte.FixByteBusiApp`
   - либо скомпилируйте и запустите `FixByteBusi.jar`

---

## Основные файлы и директории

- `src/main/java/ru/fixbyte/controller/` — контроллеры UI
- `src/main/java/ru/fixbyte/model/` — модели данных
- `src/main/java/ru/fixbyte/database/` — работа с БД товаров
- `src/main/resources/` — интерфейс (FXML), иконки, стили
- `products.db` — файл базы данных товаров
- `settings.properties` — настройки

---

## Вклад и связи

- Форкните репозиторий, улучшайте и отправляйте Pull Request!
- Автор: [Artemka-devJava](https://github.com/Artemka-devJava)
- Email: artem@tarabakin.ru

## Лицензия

MIT License

---

**FixByteBusi — быстрый старт для вашего бизнеса.  
Идеи и предложения всегда приветствуются!**