package com.bank.star.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import com.bank.star.model.ProductType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Sql(scripts = "/test-data.sql")
class RecommendationRepositoryTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private RecommendationRepository repository;
  private final UUID existingUserId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");
  private final UUID nonExistingUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");

  @Test
  void userHasProductType_WhenUserHasDebitProduct_ShouldReturnTrue() {
    // Arrange
    repository = new RecommendationRepository(jdbcTemplate);

    // Act
    boolean result = repository.userHasProductType(existingUserId, ProductType.DEBIT);

    // Assert
    assertTrue(result);
  }

  @Test
  void userHasProductType_WhenUserDoesNotHaveProduct_ShouldReturnFalse() {
    // Arrange
    repository = new RecommendationRepository(jdbcTemplate);

    // Act
    boolean result = repository.userHasProductType(existingUserId, ProductType.CREDIT);

    // Assert
    assertFalse(result);
  }

  @Test
  void userHasProductType_WhenUserNotFound_ShouldReturnFalse() {
    // Arrange
    repository = new RecommendationRepository(jdbcTemplate);

    // Act
    boolean result = repository.userHasProductType(nonExistingUserId, ProductType.DEBIT);

    // Assert
    assertFalse(result);
  }
}