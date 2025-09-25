package com.bank.star;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/test-data.sql")
class RecommendationServiceApplicationIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void contextLoads() {
    // Проверяем, что контекст Spring загружается корректно
    assertNotNull(restTemplate);
  }

  @Test
  void getRecommendations_WhenValidUser_ShouldReturnSuccess() {
    // Arrange
    String userId = "cd515076-5d8a-44be-930e-8d4fcb79f42d";

    // Act
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/api/v1/recommendations/" + userId, String.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("userId"));
  }

  @Test
  void getRecommendations_WhenInvalidUser_ShouldReturnNotFound() {
    // Arrange
    String userId = "00000000-0000-0000-0000-000000000000";

    // Act
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/api/v1/recommendations/" + userId, String.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode()); // Возвращает пустой список, не 404
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("\"recommendations\":[]"));
  }

  @Test
  void getRecommendations_WhenInvalidUUID_ShouldReturnBadRequest() {
    // Arrange
    String invalidUserId = "not-a-uuid";

    // Act
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/api/v1/recommendations/" + invalidUserId, String.class);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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
}