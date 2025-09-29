package com.bank.star.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.dto.ErrorResponse;
import com.bank.star.service.RecommendationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendation API", description = "API для получения персонализированных рекомендаций банковских продуктов")
public class RecommendationController {

  private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

  private final RecommendationService recommendationService;
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public RecommendationController(RecommendationService recommendationService, JdbcTemplate jdbcTemplate) {
    this.recommendationService = recommendationService;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Operation(
      summary = "Получить рекомендации для пользователя",
      description = """
            ## 📊 Получение персонализированных рекомендаций
            
            Этот endpoint анализирует финансовое поведение пользователя и возвращает список 
            банковских продуктов, которые наиболее подходят клиенту на основе его транзакционной активности.
            
            ### 🎯 Бизнес-правила рекомендаций:
            
            **💰 Invest 500** (Инвестиционный счет):
            - ✅ Пользователь имеет дебетовые продукты
            - ✅ Нет текущих инвестиционных продуктов  
            - ✅ Сумма пополнений сберегательных счетов > 1,000 ₽
            
            **🏦 Top Saving** (Премиум накопительный счет):
            - ✅ Пользователь имеет дебетовые продукты
            - ✅ Сумма пополнений по дебетовым ИЛИ сберегательным счетам ≥ 50,000 ₽
            - ✅ Положительный баланс по дебетовым счетам
            
            **💳 Простой кредит** (Базовый кредитный продукт):
            - ✅ Нет текущих кредитных продуктов
            - ✅ Положительный баланс по дебетовым счетам
            - ✅ Сумма расходов по дебетовым счетам > 100,000 ₽
            
            ### 👥 Тестовые пользователи:
            - `cd515076-5d8a-44be-930e-8d4fcb79f42d` - подходит для **Invest 500**
            - `d4a4d619-9a0c-4fc5-b0cb-76c49409546b` - подходит для **Top Saving**  
            - `1f9b149c-6577-448a-bc94-16bea229b71a` - подходит для **Простой кредит**
            """
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "✅ Успешный запрос. Возвращает список рекомендаций",
          content = @Content(schema = @Schema(implementation = RecommendationResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "❌ Неверный формат UUID пользователя",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "❌ Пользователь не найден в системе",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "500",
          description = "🚨 Внутренняя ошибка сервера",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping("/{userId}")
  public ResponseEntity<RecommendationResponse> getRecommendations(
      @Parameter(
          description = "Уникальный идентификатор пользователя (UUID)",
          example = "cd515076-5d8a-44be-930e-8d4fcb79f42d",
          required = true
      )
      @PathVariable String userId) {

    logger.info("🔄 Получен запрос на рекомендации для пользователя: {}", userId);

    // Валидация будет обработана глобальным обработчиком исключений
    UUID userUuid = UUID.fromString(userId);
    RecommendationResponse response = recommendationService.getRecommendations(userUuid);

    logger.info("✅ Успешно обработан запрос для пользователя: {}. Найдено рекомендаций: {}",
        userId, response.getRecommendations().size());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Диагностика базы данных",
      description = "Проверка структуры и содержимого базы данных"
  )
  @GetMapping("/debug/database")
  public ResponseEntity<String> checkDatabase() {
    try {
      StringBuilder result = new StringBuilder();

      // 1. Получим все таблицы
      List<String> tables = jdbcTemplate.queryForList(
          "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
          String.class
      );

      result.append("📊 Таблицы в базе: ").append(tables).append("\n\n");

      // 2. Проверим структуру каждой таблицы
      for (String table : tables) {
        result.append("--- Таблица: ").append(table).append(" ---\n");

        try {
          // Получим структуру таблицы
          List<Map<String, Object>> columns = jdbcTemplate.queryForList(
              "SELECT COLUMN_NAME, TYPE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?",
              table
          );

          for (Map<String, Object> column : columns) {
            result.append("  ").append(column.get("COLUMN_NAME"))
                .append(" : ").append(column.get("TYPE_NAME"))
                .append("\n");
          }

          // Посчитаем количество записей
          Integer count = jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM " + table, Integer.class);
          result.append("  Записей: ").append(count).append("\n\n");

        } catch (Exception e) {
          result.append("  Ошибка: ").append(e.getMessage()).append("\n\n");
        }
      }

      // 3. Проверим конкретно тестового пользователя
      result.append("--- Проверка пользователя cd515076-5d8a-44be-930e-8d4fcb79f42d ---\n");
      try {
        // Проверим, есть ли транзакции для этого пользователя
        List<Map<String, Object>> userTransactions = jdbcTemplate.queryForList(
            "SELECT * FROM transactions WHERE user_id = ? LIMIT 5",
            "cd515076-5d8a-44be-930e-8d4fcb79f42d"
        );
        result.append("Транзакций пользователя: ").append(userTransactions.size()).append("\n");

        if (!userTransactions.isEmpty()) {
          result.append("Первые транзакции: ").append(userTransactions).append("\n");
        }
      } catch (Exception e) {
        result.append("Ошибка проверки пользователя: ").append(e.getMessage()).append("\n");
      }

      return ResponseEntity.ok(result.toString());

    } catch (Exception e) {
      return ResponseEntity.ok("❌ Ошибка подключения к БД: " + e.getMessage());
    }
  }

  @Operation(
      summary = "Проверка здоровья сервиса",
      description = "Health check endpoint для мониторинга работоспособности микросервиса"
  )
  @ApiResponse(
      responseCode = "200",
      description = "✅ Сервис работает корректно"
  )
  @GetMapping("/health")
  public ResponseEntity<String> health() {
    logger.debug("🔍 Проверка здоровья сервиса");
    return ResponseEntity.ok("""
                🏦 Bank Star Recommendation Service
                Status: ✅ OPERATIONAL
                Version: 1.0.0
                Timestamp: %s
                """.formatted(LocalDateTime.now()));
  }

  @Operation(
      summary = "Информация о сервисе",
      description = "Возвращает базовую информацию о версии и возможностях сервиса"
  )
  @ApiResponse(
      responseCode = "200",
      description = "✅ Информация успешно получена"
  )
  @GetMapping("/info")
  public ResponseEntity<String> info() {
    return ResponseEntity.ok("""
                🏦 Bank Star Recommendation Service v1.0.0
                
                📊 Основные возможности:
                • Персонализированные рекомендации банковских продуктов
                • Анализ транзакционного поведения в реальном времени
                • Интеграция с мобильным приложением и личным кабинетом
                • Поддержка 3 алгоритмов рекомендаций
                
                🔧 Технологический стек:
                • Java 17, Spring Boot 3.2
                • H2 Database (read-only)
                • Spring JDBC Template
                • Swagger/OpenAPI 3.0
                
                👥 Для тестирования используйте тестовые UUID пользователей
                """);
  }

  @Operation(
      summary = "Статистика сервиса",
      description = "Возвращает метрики и статистику работы сервиса"
  )
  @ApiResponse(
      responseCode = "200",
      description = "✅ Статистика успешно получена"
  )
  @GetMapping("/stats")
  public ResponseEntity<String> stats() {
    return ResponseEntity.ok("""
                📈 Статистика сервиса рекомендаций:
                
                • Алгоритмы рекомендаций: 3
                • Поддерживаемые продукты: Invest 500, Top Saving, Простой кредит
                • Максимальное время ответа: < 100ms
                • Формат данных: JSON
                • Кэширование: Включено
                
                🎯 Бизнес-логика:
                Основана на анализе финансового поведения пользователей
                и строгом соответствии бизнес-правилам банка.
                """);
  }

  /**
   * Обработчик исключений для неверного формата UUID
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    logger.warn("❌ Ошибка валидации UUID: {}", e.getMessage());

    ErrorResponse error = new ErrorResponse(
        "VALIDATION_ERROR",
        "Неверный формат UUID пользователя: " + e.getMessage(),
        LocalDateTime.now()
    );

    return ResponseEntity.badRequest().body(error);
  }

  /**
   * Глобальный обработчик всех исключений
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
    logger.error("🚨 Внутренняя ошибка сервера при обработке запроса: {}", e.getMessage(), e);

    ErrorResponse error = new ErrorResponse(
        "INTERNAL_SERVER_ERROR",
        "Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.",
        LocalDateTime.now()
    );

    return ResponseEntity.internalServerError().body(error);
  }
}