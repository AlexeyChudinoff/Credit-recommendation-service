package com.bank.star.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для тестирования базы данных и отладки рекомендаций
 */
@RestController
@RequestMapping("/api/test")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Database Test", description = "API для тестирования базы данных и отладки системы рекомендаций")
public class DatabaseTestController {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseTestController.class);

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private com.bank.star.repository.RecommendationRepository recommendationRepository;

  /**
   * Тестирование подключения к базе данных
   */
  @GetMapping("/connection")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Проверка подключения к БД",
      description = "Проверяет возможность подключения к базе данных и возвращает информацию о подключении"
  )
  public ResponseEntity<String> testConnection() {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();

      String result = "✅ Подключение успешно!\n" +
          "URL: " + metaData.getURL() + "\n" +
          "User: " + metaData.getUserName() + "\n" +
          "Database: " + metaData.getDatabaseProductName() + " "
          + metaData.getDatabaseProductVersion() + "\n" +
          "Read Only: " + connection.isReadOnly();

      logger.info(result);
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      String error = "❌ Ошибка подключения: " + e.getMessage();
      logger.error(error, e);
      return ResponseEntity.ok(error);
    }
  }

  /**
   * Получение списка таблиц в базе данных
   */
  @GetMapping("/tables")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Список таблиц БД",
      description = "Возвращает список всех таблиц в базе данных и их количество"
  )
  public ResponseEntity<String> listTables() {
    try {
      Integer tableCount = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
          Integer.class
      );

      String tablesList = jdbcTemplate.queryForObject(
          "SELECT GROUP_CONCAT(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
          String.class
      );

      String result = "✅ Количество таблиц в базе: " + tableCount + "\n" +
          "Таблицы: " + (tablesList != null ? tablesList : "нет таблиц");

      logger.info(result);
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      String error = "❌ Ошибка получения таблиц: " + e.getMessage();
      logger.error(error, e);
      return ResponseEntity.ok(error);
    }
  }

  /**
   * Простейший тест работы базы данных
   */
  @GetMapping("/simple-test")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Простой тест БД",
      description = "Выполняет простой SQL запрос для проверки работоспособности базы данных"
  )
  public ResponseEntity<String> simpleTest() {
    try {
      String result = jdbcTemplate.queryForObject("SELECT 'База данных работает!' AS message",
          String.class);
      return ResponseEntity.ok("✅ " + result);
    } catch (Exception e) {
      return ResponseEntity.ok("❌ Простейший запрос не работает: " + e.getMessage());
    }
  }


  /**
   * Получение информации о базе данных
   */
  @GetMapping("/database-info")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Информация о БД",
      description = "Возвращает информацию о версии H2 и режиме работы базы данных"
  )
  public ResponseEntity<String> databaseInfo() {
    try {
      String version = jdbcTemplate.queryForObject("SELECT H2VERSION()", String.class);

      // Альтернативные способы получения информации о режиме
      String mode = "Не удалось определить";
      try {
        // Попробуем получить режим из URL базы данных
        mode = jdbcTemplate.queryForObject(
            "SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME = 'MODE'",
            String.class
        );
      } catch (Exception e) {
        // Если не сработало, попробуем другой вариант
        try {
          mode = jdbcTemplate.queryForObject(
              "SELECT VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME = 'MODE'",
              String.class
          );
        } catch (Exception ex) {
          // Если и это не сработало, используем информацию из properties
          mode = "PostgreSQL (из настроек URL)";
        }
      }

      String result = "✅ Информация о базе:\n" +
          "Версия H2: " + version + "\n" +
          "Режим: " + mode;

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.ok("❌ Ошибка получения информации: " + e.getMessage());
    }
  }

  /**
   * Получение схемы таблиц transactions и products
   */
  @GetMapping("/schema")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Схема таблиц",
      description = "Возвращает структуру таблиц transactions и products с типами данных колонок"
  )
  public ResponseEntity<String> getSchema() {
    try {
      List<Map<String, Object>> transactionsColumns = jdbcTemplate.queryForList(
          "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'TRANSACTIONS' ORDER BY ORDINAL_POSITION"
      );

      List<Map<String, Object>> productsColumns = jdbcTemplate.queryForList(
          "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PRODUCTS' ORDER BY ORDINAL_POSITION"
      );

      StringBuilder result = new StringBuilder();
      result.append("=== TRANSACTIONS TABLE ===\n");
      for (Map<String, Object> column : transactionsColumns) {
        result.append(column.get("COLUMN_NAME")).append(" - ").append(column.get("DATA_TYPE"))
            .append("\n");
      }

      result.append("\n=== PRODUCTS TABLE ===\n");
      for (Map<String, Object> column : productsColumns) {
        result.append(column.get("COLUMN_NAME")).append(" - ").append(column.get("DATA_TYPE"))
            .append("\n");
      }

      return ResponseEntity.ok(result.toString());

    } catch (Exception e) {
      return ResponseEntity.ok("❌ Ошибка получения схемы: " + e.getMessage());
    }
  }

  /**
   * Получение примеров данных из таблиц
   */
  @GetMapping("/sample-data")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Примеры данных",
      description = "Возвращает первые 5 записей из таблиц transactions и products для ознакомления со структурой данных"
  )
  public ResponseEntity<String> getSampleData() {
    try {
      List<Map<String, Object>> sampleTransactions = jdbcTemplate.queryForList(
          "SELECT * FROM TRANSACTIONS LIMIT 5"
      );

      List<Map<String, Object>> sampleProducts = jdbcTemplate.queryForList(
          "SELECT * FROM PRODUCTS LIMIT 5"
      );

      StringBuilder result = new StringBuilder();
      result.append("=== SAMPLE TRANSACTIONS ===\n");
      for (Map<String, Object> row : sampleTransactions) {
        result.append(row.toString()).append("\n");
      }

      result.append("\n=== SAMPLE PRODUCTS ===\n");
      for (Map<String, Object> row : sampleProducts) {
        result.append(row.toString()).append("\n");
      }

      return ResponseEntity.ok(result.toString());

    } catch (Exception e) {
      return ResponseEntity.ok("❌ Ошибка получения данных: " + e.getMessage());
    }
  }

  /**
   * Тестовые запросы для проверки работы БД
   */
  @GetMapping("/test-queries")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Тестовые запросы",
      description = "Выполняет серию тестовых запросов для проверки корректности работы базы данных и данных"
  )
  public ResponseEntity<String> testQueries() {
    try {
      StringBuilder result = new StringBuilder();

      Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
      result.append("Всего пользователей: ").append(userCount).append("\n");

      Integer transactionCount = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM TRANSACTIONS t JOIN PRODUCTS p ON t.product_id = p.id",
          Integer.class
      );
      result.append("Всего транзакций: ").append(transactionCount).append("\n");

      List<String> productTypes = jdbcTemplate.queryForList(
          "SELECT DISTINCT type FROM PRODUCTS",
          String.class
      );
      result.append("Типы продуктов: ").append(productTypes).append("\n");

      return ResponseEntity.ok(result.toString());

    } catch (Exception e) {
      return ResponseEntity.ok("❌ Ошибка тестовых запросов: " + e.getMessage());
    }
  }

  /**
   * Детальная статистика пользователя для анализа рекомендаций
   */
  @GetMapping("/user-stats/{userId}")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Статистика пользователя",
      description = "Возвращает детальную финансовую статистику пользователя и анализ условий для рекомендаций продуктов"
  )
  @io.swagger.v3.oas.annotations.Parameter(
      name = "userId",
      description = "UUID пользователя для анализа",
      required = true,
      example = "cd515076-5d8a-44be-930e-8d4fcb79f42d"
  )
  public ResponseEntity<String> getUserStats(@PathVariable UUID userId) {
    try {
      BigDecimal debitDeposit = recommendationRepository.getTotalDepositAmountByProductType(userId,
          com.bank.star.model.ProductType.DEBIT);
      BigDecimal debitSpend = recommendationRepository.getTotalSpendAmountByProductType(userId,
          com.bank.star.model.ProductType.DEBIT);
      BigDecimal savingDeposit = recommendationRepository.getTotalDepositAmountByProductType(userId,
          com.bank.star.model.ProductType.SAVING);

      boolean hasCredit = recommendationRepository.userHasProductType(userId,
          com.bank.star.model.ProductType.CREDIT);
      boolean hasInvest = recommendationRepository.userHasProductType(userId,
          com.bank.star.model.ProductType.INVEST);
      boolean hasDebit = recommendationRepository.userHasProductType(userId,
          com.bank.star.model.ProductType.DEBIT);

      boolean invest500Condition1 = hasDebit;
      boolean invest500Condition2 = !hasInvest;
      boolean invest500Condition3 = savingDeposit.compareTo(new BigDecimal("1000")) > 0;

      boolean topSavingCondition1 = hasDebit;
      boolean topSavingCondition2 = debitDeposit.compareTo(new BigDecimal("50000")) >= 0
          || savingDeposit.compareTo(new BigDecimal("50000")) >= 0;
      boolean topSavingCondition3 = debitDeposit.compareTo(debitSpend) > 0;

      boolean simpleCreditCondition1 = !hasCredit;
      boolean simpleCreditCondition2 = debitDeposit.compareTo(debitSpend) > 0;
      boolean simpleCreditCondition3 = debitSpend.compareTo(new BigDecimal("100000")) > 0;

      StringBuilder result = new StringBuilder();
      result.append("=== ДЕТАЛЬНАЯ СТАТИСТИКА ПОЛЬЗОВАТЕЛЯ ").append(userId).append(" ===\n\n");

      result.append("💰 ФИНАНСОВЫЕ ДАННЫЕ:\n");
      result.append("Пополнения DEBIT: ").append(debitDeposit).append("\n");
      result.append("Траты DEBIT: ").append(debitSpend).append("\n");
      result.append("Пополнения SAVING: ").append(savingDeposit).append("\n");
      result.append("Баланс DEBIT: ").append(debitDeposit.subtract(debitSpend)).append("\n\n");

      result.append("💳 НАЛИЧИЕ ПРОДУКТОВ:\n");
      result.append("DEBIT: ").append(hasDebit ? "✅" : "❌").append("\n");
      result.append("CREDIT: ").append(hasCredit ? "✅" : "❌").append("\n");
      result.append("INVEST: ").append(hasInvest ? "✅" : "❌").append("\n\n");

      result.append("📊 АНАЛИЗ РЕКОМЕНДАЦИЙ:\n");

      result.append("Invest 500:\n");
      result.append("  DEBIT продукт: ").append(invest500Condition1 ? "✅" : "❌").append("\n");
      result.append("  Нет INVEST: ").append(invest500Condition2 ? "✅" : "❌").append("\n");
      result.append("  SAVING > 1,000: ").append(invest500Condition3 ? "✅" : "❌ (")
          .append(savingDeposit).append(")").append("\n");
      result.append("  ИТОГО: ").append(
          invest500Condition1 && invest500Condition2 && invest500Condition3 ? "✅ РЕКОМЕНДОВАНО"
              : "❌ НЕ РЕКОМЕНДОВАНО").append("\n\n");

      result.append("Top Saving:\n");
      result.append("  DEBIT продукт: ").append(topSavingCondition1 ? "✅" : "❌").append("\n");
      result.append("  Пополнения ≥ 50k: ").append(topSavingCondition2 ? "✅" : "❌ (")
          .append("DEBIT:").append(debitDeposit).append(" SAVING:").append(savingDeposit)
          .append(")").append("\n");
      result.append("  Пополнения > Трат: ").append(topSavingCondition3 ? "✅" : "❌ (")
          .append("разница:").append(debitDeposit.subtract(debitSpend)).append(")").append("\n");
      result.append("  ИТОГО: ").append(
          topSavingCondition1 && topSavingCondition2 && topSavingCondition3 ? "✅ РЕКОМЕНДОВАНО"
              : "❌ НЕ РЕКОМЕНДОВАНО").append("\n\n");

      result.append("Простой кредит:\n");
      result.append("  Нет CREDIT: ").append(simpleCreditCondition1 ? "✅" : "❌").append("\n");
      result.append("  Пополнения > Трат: ").append(simpleCreditCondition2 ? "✅" : "❌ (")
          .append("разница:").append(debitDeposit.subtract(debitSpend)).append(")").append("\n");
      result.append("  Траты > 100,000: ").append(simpleCreditCondition3 ? "✅" : "❌ (")
          .append(debitSpend).append(")").append("\n");
      result.append("  ИТОГО: ").append(
          simpleCreditCondition1 && simpleCreditCondition2 && simpleCreditCondition3
              ? "✅ РЕКОМЕНДОВАНО" : "❌ НЕ РЕКОМЕНДОВАНО").append("\n");

      return ResponseEntity.ok(result.toString());

    } catch (Exception e) {
      return ResponseEntity.ok("❌ Ошибка получения статистики: " + e.getMessage());
    }
  }

  /**
   * Получение списка продуктов пользователя
   */
  @GetMapping("/user-products/{userId}")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Продукты пользователя",
      description = "Возвращает список всех банковских продуктов, которыми пользуется указанный пользователь"
  )
  @io.swagger.v3.oas.annotations.Parameter(
      name = "userId",
      description = "UUID пользователя для получения списка продуктов",
      required = true,
      example = "cd515076-5d8a-44be-930e-8d4fcb79f42d"
  )
  public ResponseEntity<String> getUserProducts(@PathVariable UUID userId) {
    try {
      List<Map<String, Object>> userProducts = jdbcTemplate.queryForList(
          "SELECT DISTINCT p.id, p.name, p.type " +
              "FROM transactions t " +
              "JOIN products p ON t.product_id = p.id " +
              "WHERE t.user_id = ?",
          userId.toString()
      );

      StringBuilder result = new StringBuilder();
      result.append("=== ПРОДУКТЫ ПОЛЬЗОВАТЕЛЯ ").append(userId).append(" ===\n");

      if (userProducts.isEmpty()) {
        result.append("❌ У пользователя нет продуктов\n");
      } else {
        for (Map<String, Object> product : userProducts) {
          result.append("• ").append(product.get("name"))
              .append(" (").append(product.get("type")).append(")")
              .append(" [ID: ").append(product.get("id")).append("]\n");
        }
      }

      return ResponseEntity.ok(result.toString());

    } catch (Exception e) {
      return ResponseEntity.ok("❌ Ошибка получения продуктов пользователя: " + e.getMessage());
    }
  }

  /**
   * Проверка существования пользователя в системе
   */
  @GetMapping("/user-exists/{userId}")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Проверка пользователя",
      description = "Проверяет существует ли пользователь с указанным UUID в базе данных"
  )
  @io.swagger.v3.oas.annotations.Parameter(
      name = "userId",
      description = "UUID пользователя для проверки",
      required = true,
      example = "cd515076-5d8a-44be-930e-8d4fcb79f42d"
  )
  public ResponseEntity<String> checkUserExists(@PathVariable UUID userId) {
    try {
      boolean exists = recommendationRepository.userExists(userId);
      String result = exists ?
          "✅ Пользователь " + userId + " существует в системе" :
          "❌ Пользователь " + userId + " не найден в системе";

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      return ResponseEntity.ok("❌ Ошибка проверки пользователя: " + e.getMessage());
    }
  }
}