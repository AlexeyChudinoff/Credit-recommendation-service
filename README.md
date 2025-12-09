```markdown
# Credit Recommendation Service

Сервис, который рекомендует клиентам банка новые кредитные продукты на основе данных о клиенте, бизнес‑правил и моделей машинного обучения.

## Стек технологий

- Язык: Java 17
- Фреймворк: Spring Boot 3.x
- Сборка: Maven
- База данных: PostgreSQL
- Кеш: Redis
- ML: внешний сервис на Python / Scikit‑learn
- API: REST, OpenAPI (Swagger), Spring Boot Actuator
- Интеграции: Telegram Bot API
- Инфраструктура: Docker, Docker Compose, Flyway (миграции БД)

## Основные возможности

- Получение персонализированных кредитных рекомендаций по идентификатору пользователя.
- Применение статических и динамических бизнес‑правил.
- Интеграция с ML‑сервисом для расчёта скоринга/рекомендаций.
- Кеширование рекомендаций в Redis.
- Управление правилами и сбор статистики использования.
- Получение рекомендаций через Telegram‑бота.

## Быстрый старт (локально)

```
git clone https://github.com/AlexeyChudinoff/Credit-recommendation-service.git
cd Credit-recommendation-service

# Сборка (при необходимости без тестов)
./mvnw clean package -DskipTests
# или на Windows:
# mvnw.cmd clean package -DskipTests
```

Перед запуском нужно поднять PostgreSQL и Redis и задать переменные окружения:

```
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/credit_db
export SPRING_DATASOURCE_USERNAME=credit_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379

# при наличии:
export TELEGRAM_BOT_TOKEN=your_bot_token
export ML_SERVICE_URL=http://localhost:5000
```

Запуск приложения:

```
java -jar target/*.jar
```

Проверка:

```
curl "http://localhost:8080/api/v1/recommendations?userId=test123"
```

## Запуск через Docker Compose

Если используешь Docker Compose:

```
docker-compose up -d
```

Compose‑файл поднимает приложение, PostgreSQL и Redis, а подключение настраивается через переменные среды.

## Структура проекта

- `src/main/java/...` — исходный код сервиса (контроллеры, сервисы, правила, интеграции).
- `src/main/resources/application*.properties` — конфигурация Spring Boot.
- `src/main/resources/db/migration` — миграции Flyway (если присутствуют).
- `docs/` или `wiki` — документация и диаграммы (архитектура, требования, API).

## Документация

Расширенная документация лежит в GitHub Wiki:

- **Требования и User Story** — акторы, функциональные и нефункциональные требования.
- **Архитектура** — компонентная и activity‑диаграммы, описание взаимодействий.
- **API документация** — OpenAPI/Swagger для REST‑интерфейса.
- **Развертывание** — подробные инструкции по запуску в разных средах и настройке окружения.

## Планы развития

- Расширение набора продуктовых правил.
- Улучшение моделей ML и их конфигурируемости.
- Добавление новых каналов интеграции (например, мобильное приложение/веб‑UI).
```
