```markdown
# Credit Recommendation Service

Сервис, который рекомендует клиентам банка новые кредитные продукты на основе
данных о клиенте, бизнес‑правил и моделей машинного обучения.[web:1]

## Стек технологий

- Язык: Java 17
- Фреймворк: Spring Boot 3.x
- Сборка: Maven
- База данных: PostgreSQL
- Кеш: Redis
- ML: внешний сервис на Python / Scikit‑learn
- API: REST, OpenAPI (Swagger), Spring Boot Actuator
- Интеграции: Telegram Bot API
- Инфраструктура: Docker, Docker Compose, Flyway

## Основные возможности

- Получение персонализированных кредитных рекомендаций по идентификатору пользователя.[web:1]
- Применение статических и динамических бизнес‑правил.
- Интеграция с ML‑сервисом для расчёта скоринга/рекомендаций.
- Кеширование рекомендаций в Redis.
- Управление правилами и сбор статистики использования.
- Получение рекомендаций через Telegram‑бота.[web:1]

## Быстрый старт (локально)

```
git clone https://github.com/AlexeyChudinoff/Credit-recommendation-service.git
cd Credit-recommendation-service
```

Сборка (при необходимости без тестов):

```
./mvnw clean package -DskipTests
# или на Windows:
# mvnw.cmd clean package -DskipTests
```

Перед запуском нужно поднять PostgreSQL и Redis и задать переменные окружения:[web:1]

```
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/credit_db
export SPRING_DATASOURCE_USERNAME=credit_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379
```

При наличии дополнительных сервисов:

```
export TELEGRAM_BOT_TOKEN=your_bot_token
export ML_SERVICE_URL=http://localhost:5000
```

Запуск приложения:

```
java -jar target/*.jar
```

Проверка:

```
# 1. Path Variable (корректный UUID)
curl "http://localhost:8080/api/v1/recommendations/cd515076-5d8a-44be-930e-8d4fcb79f42d"

# 2. Query Parameter (корректный UUID)  
curl "http://localhost:8080/api/v1/recommendations?userId=cd515076-5d8a-44be-930e-8d4fcb79f42d"

# 3. Path Variable (некорректный UUID) - должен вернуть 400
curl "http://localhost:8080/api/v1/recommendations/test123"

# 4. Query Parameter (некорректный UUID) - должен вернуть 400
curl "http://localhost:8080/api/v1/recommendations?userId=test123"

# 5. Несуществующий пользователь - должен вернуть 404
curl "http://localhost:8080/api/v1/recommendations/00000000-0000-0000-0000-000000000000"
```

## Запуск через Docker Compose

Если используете Docker Compose:

```
docker-compose up -d
```

Compose‑файл поднимает приложение, PostgreSQL и Redis, а подключение настраивается через переменные среды.[web:1]

## Структура проекта

- `src/main/java/...` — исходный код сервиса (контроллеры, сервисы, правила, интеграции).[web:1]
- `src/main/resources/application*.properties` — конфигурация Spring Boot.
- `src/main/resources/db/migration` — миграции Flyway (если присутствуют).
- `data/` — обучающие/тестовые данные и артефакты для ML‑части.
- `wiki` (GitHub Wiki) — архитектурная и проектная документация.[web:2]

## Документация (Wiki)

Расширенная документация находится в GitHub Wiki репозитория:[web:2]

- **Требования и User Story** — акторы, функциональные и нефункциональные требования:  
  <https://github.com/AlexeyChudinoff/Credit-recommendation-service/wiki/Требования.md>
- **Архитектура** — компонентная диаграмма и диаграмма деятельности алгоритма рекомендаций:  
  <https://github.com/AlexeyChudinoff/Credit-recommendation-service/wiki/Архитектура>
- **API документация** — описание REST API в формате OpenAPI/Swagger:  
  <https://github.com/AlexeyChudinoff/Credit-recommendation-service/wiki/API-Документация>
- **Развертывание** — требования к окружению и подробная инструкция по запуску в разных средах:  
  <https://github.com/AlexeyChudinoff/Credit-recommendation-service/wiki/Развертывание>
- **Главная (обзор проекта)** — краткое описание, стек и навигация по документации:  
  <https://github.com/AlexeyChudinoff/Credit-recommendation-service/wiki/Главная>

## Планы развития

- Расширение набора продуктовых правил и сценариев рекомендаций.
- Улучшение моделей ML и их конфигурируемости.
- Добавление новых каналов интеграции (мобильное приложение, веб‑UI для менеджера).
