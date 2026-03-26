# FixByteBusi

Desktop-приложение на JavaFX для работы с товарами, печати чеков и ведения заказов в формате Kanban.

## Возможности

- управление каталогом товаров (добавление, редактирование, удаление);
- формирование и печать товарных чеков;
- Kanban-доска заказов с сохранением карточек;
- настройки компании и параметров приложения.

## Технологии

- Java 17
- JavaFX 21.0.6
- Maven
- SQLite (`sqlite-jdbc`)
- Jackson (`jackson-databind`)

## Требования

- установленный JDK 17+;
- доступ к Maven (через `mvn` или `mvnw.cmd`, если wrapper восстановлен);
- Windows (в `pom.xml` используются JavaFX зависимости с `classifier` = `win`).

## Быстрый старт (PowerShell)

### 1) Проверка Java

```powershell
java -version
```

### 2) Сборка проекта

```powershell
Set-Location C:\Project\FixByteBuss
mvn clean compile
```

### 3) Запуск приложения в dev-режиме

```powershell
Set-Location C:\Project\FixByteBuss
mvn javafx:run
```

## Сборка JAR

```powershell
Set-Location C:\Project\FixByteBuss
mvn clean package
java -jar .\target\FixByteBusi-1.0.jar
```

> Точка входа приложения: `ru.fixbyte.Launcher`.

## Maven Wrapper

Если команда `./mvnw` или `.\mvnw.cmd` не работает и сообщает про отсутствие `.mvn\wrapper\maven-wrapper.properties`, используйте системный Maven (`mvn`) или восстановите wrapper:

```powershell
Set-Location C:\Project\FixByteBuss
mvn -N wrapper:wrapper
```

После восстановления можно использовать:

```powershell
Set-Location C:\Project\FixByteBuss
.\mvnw.cmd clean compile
```

## Структура проекта

- `src/main/java/ru/fixbyte/controller/` - контроллеры JavaFX;
- `src/main/java/ru/fixbyte/model/` - модели данных;
- `src/main/java/ru/fixbyte/database/` - работа с SQLite;
- `src/main/java/ru/fixbyte/kanban/` - Kanban (model/view/controller/service);
- `src/main/resources/` - FXML и ресурсы интерфейса;
- `settings.properties` - настройки приложения;
- `data/cards.txt` - данные Kanban-карточек;
- `products.db` / `data/products.db` - файлы базы данных.

## Примечания

- для установки артефакта в локальный репозиторий запускайте lifecycle-команду `mvn install`, а не plugin goal `maven-install-plugin:install`;
- предупреждения про `sun.misc.Unsafe` в Maven (из библиотек IntelliJ) обычно не блокируют сборку.
