package com.bank.star.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import com.bank.star.model.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Sql(scripts = {"/schema.sql", "/test-data.sql"})
class RecommendationRepositoryTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private RecommendationRepository repository;

  private final UUID existingUserId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");
  private final UUID nonExistingUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");

  @BeforeEach
  void setUp() {
    repository = new RecommendationRepository(jdbcTemplate);
  }

  @Test
  void userHasProductType_WhenUserHasDebitProduct_ShouldReturnTrue() {
    // Act
    boolean result = repository.userHasProductType(existingUserId, ProductType.DEBIT);

    // Assert
    assertTrue(result);
  }

  @Test
  void userHasProductType_WhenUserDoesNotHaveProduct_ShouldReturnFalse() {
    // Act
    boolean result = repository.userHasProductType(existingUserId, ProductType.CREDIT);

    // Assert
    assertFalse(result);
  }

  @Test
  void userHasProductType_WhenUserNotFound_ShouldReturnFalse() {
    // Act
    boolean result = repository.userHasProductType(nonExistingUserId, ProductType.DEBIT);

    // Assert
    assertFalse(result);
  }

  @Test
  void getTotalDepositAmountByProductType_WhenUserHasDeposits_ShouldReturnCorrectAmount() {
    // Act
    BigDecimal result = repository.getTotalDepositAmountByProductType(existingUserId, ProductType.DEBIT);

    // Assert
    assertNotNull(result);
    assertEquals(0, new BigDecimal("50000.00").compareTo(result));
  }

  @Test
  void getTotalDepositAmountByProductType_WhenUserHasNoDeposits_ShouldReturnZero() {
    // Act
    BigDecimal result = repository.getTotalDepositAmountByProductType(existingUserId, ProductType.CREDIT);

    // Assert
    assertNotNull(result);
    assertEquals(BigDecimal.ZERO, result);
  }

  @Test
  void getTotalSpendAmountByProductType_WhenUserHasSpends_ShouldReturnCorrectAmount() {
    // Act
    BigDecimal result = repository.getTotalSpendAmountByProductType(
        UUID.fromString("d4a4d619-9a0c-4fc5-b0cb-76c49409546b"), ProductType.DEBIT);

    // Assert
    assertNotNull(result);
    assertEquals(0, new BigDecimal("10000.00").compareTo(result));
  }

  @Test
  void getTotalSpendAmountByProductType_WhenUserHasNoSpends_ShouldReturnZero() {
    // Act
    BigDecimal result = repository.getTotalSpendAmountByProductType(existingUserId, ProductType.DEBIT);

    // Assert
    assertNotNull(result);
    assertEquals(BigDecimal.ZERO, result);
  }
}