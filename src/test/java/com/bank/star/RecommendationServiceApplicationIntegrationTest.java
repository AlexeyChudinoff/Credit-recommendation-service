package com.bank.star;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bank.star.dto.ErrorResponse;
import com.bank.star.dto.RecommendationResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RecommendationServiceApplicationIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void debugDatabaseContents() {
    // Проверим, что данные загрузились
    List<Map<String, Object>> transactions = jdbcTemplate.queryForList(
        "SELECT * FROM transactions LIMIT 10");

    List<Map<String, Object>> products = jdbcTemplate.queryForList(
        "SELECT * FROM products LIMIT 10");

    List<Map<String, Object>> users = jdbcTemplate.queryForList(
        "SELECT * FROM users LIMIT 10");

    System.out.println("=== DEBUG DATABASE ===");
    System.out.println("Transactions: " + transactions.size());
    System.out.println("Products: " + products.size());
    System.out.println("Users: " + users.size());

    // Выведем примеры транзакций
    System.out.println("=== SAMPLE TRANSACTIONS ===");
    transactions.forEach(tx -> System.out.println("TX: " + tx));

    System.out.println("=== SAMPLE PRODUCTS ===");
    products.forEach(prod -> System.out.println("PRODUCT: " + prod));

    System.out.println("=== SAMPLE USERS ===");
    users.forEach(user -> System.out.println("USER: " + user));
  }

  @Test
  void contextLoads() {
    // Проверяем, что контекст Spring загружается корректно
    assertNotNull(restTemplate);
    assertNotNull(jdbcTemplate);
  }

  @Test
  void getRecommendations_WhenUserEligibleForInvest500_ShouldReturnInvest500() {
    // Arrange - пользователь cd515076-5d8a-44be-930e-8d4fcb79f42d подходит для Invest 500
    String userId = "cd515076-5d8a-44be-930e-8d4fcb79f42d";

    // Act
    ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
        "/api/v1/recommendations/" + userId,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<RecommendationResponse>() {
        });

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userId, response.getBody().getUserId().toString());

    // Проверяем, что вернулась рекомендация Invest 500
    assertTrue(response.getBody().hasRecommendations());
    assertEquals(1, response.getBody().getRecommendationsCount());
    assertEquals("Invest 500", response.getBody().getRecommendations().get(0).getName());
  }

  @Test
  void getRecommendations_WhenUserEligibleForTopSaving_ShouldReturnTopSaving() {
    // Arrange - пользователь d4a4d619-9a0c-4fc5-b0cb-76c49409546b подходит для Top Saving
    String userId = "d4a4d619-9a0c-4fc5-b0cb-76c49409546b";

    // Act
    ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
        "/api/v1/recommendations/" + userId,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<RecommendationResponse>() {
        });

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    // Проверяем, что вернулась рекомендация Top Saving
    assertTrue(response.getBody().hasRecommendations());
    assertEquals(1, response.getBody().getRecommendationsCount());
    assertEquals("Top Saving", response.getBody().getRecommendations().get(0).getName());
  }

  @Test
  void getRecommendations_WhenUserEligibleForSimpleCredit_ShouldReturnSimpleCredit() {
    // Arrange - пользователь 1f9b149c-6577-448a-bc94-16bea229b71a подходит для Простого кредита
    String userId = "1f9b149c-6577-448a-bc94-16bea229b71a";

    // Act
    ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
        "/api/v1/recommendations/" + userId,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<RecommendationResponse>() {
        });

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    // Проверяем, что вернулась рекомендация Простой кредит (может быть среди других)
    assertTrue(response.getBody().hasRecommendations());
    assertTrue(response.getBody().getRecommendations().stream()
        .anyMatch(rec -> "Простой кредит".equals(rec.getName())));
  }

  @Test
  void getRecommendations_WhenUserNotEligible_ShouldReturnEmptyList() {
    // Arrange - пользователь a1b2c3d4-5e6f-4890-9a0b-c1d2e3f4a5b6 не подходит ни для чего
    String userId = "a1b2c3d4-5e6f-4890-9a0b-c1d2e3f4a5b6";

    // Act
    ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
        "/api/v1/recommendations/" + userId,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<RecommendationResponse>() {
        });

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userId, response.getBody().getUserId().toString());
    assertFalse(response.getBody().hasRecommendations());
    assertEquals(0, response.getBody().getRecommendationsCount());
  }

  @Test
  void getRecommendations_WhenInvalidUUID_ShouldReturnBadRequest() {
    // Arrange
    String invalidUserId = "not-a-uuid";

    // Act
    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        "/api/v1/recommendations/" + invalidUserId,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<ErrorResponse>() {
        });

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    assertTrue(response.getBody().getMessage().contains("Неверный формат UUID"));
  }

  @Test
  void getRecommendations_WhenNonExistentUser_ShouldReturnEmptyList() {
    // Arrange - пользователь, которого нет в тестовых данных
    String userId = "00000000-0000-0000-0000-000000000000";

    // Act
    ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
        "/api/v1/recommendations/" + userId,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<RecommendationResponse>() {
        });

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userId, response.getBody().getUserId().toString());
    assertFalse(response.getBody().hasRecommendations());
  }

  @Test
  void healthEndpoint_ShouldReturnHealthy() {
    // Act
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/api/v1/recommendations/health", String.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("OPERATIONAL"));
  }

  @Test
  void infoEndpoint_ShouldReturnServiceInfo() {
    // Act
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/api/v1/recommendations/info", String.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("Bank Star Recommendation Service"));
  }

  @Test
  void statsEndpoint_ShouldReturnStatistics() {
    // Act
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/api/v1/recommendations/stats", String.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("Статистика сервиса"));
  }

  @Test
  void databaseDebugEndpoint_ShouldReturnDatabaseInfo() {
    // Act
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/api/v1/recommendations/debug/database", String.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    // Проверяем, что endpoint возвращает какую-то информацию о БД
    assertTrue(response.getBody().contains("Таблицы") || response.getBody().contains("таблиц"));
  }
}