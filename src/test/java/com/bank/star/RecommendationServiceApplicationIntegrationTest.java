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
  }

  @Test
  void getRecommendations_WhenValidUser_ShouldReturnSuccess() {
    // Arrange
    String userId = "cd515076-5d8a-44be-930e-8d4fcb79f42d";

    // Act
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/recommendation/" + userId, String.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("userId"));
  }

  @Test
  void healthEndpoint_ShouldReturnHealthy() {
    // Act
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/recommendation/health", String.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Service is healthy!", response.getBody());
  }
}