package com.bank.star.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.model.ProductType;
import com.bank.star.repository.RecommendationRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

  @Mock
  private RecommendationRepository repository;

  @InjectMocks
  private RecommendationService recommendationService;

  private UUID testUserId;

  @BeforeEach
  void setUp() {
    testUserId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");
  }

  @Test
  void getRecommendations_WhenUserEligibleForInvest500_ShouldReturnInvest500() {
    // Arrange
    when(repository.userHasProductType(testUserId, ProductType.DEBIT)).thenReturn(true);
    when(repository.userHasProductType(testUserId, ProductType.INVEST)).thenReturn(false);
    when(repository.getTotalDepositAmountByProductType(testUserId, ProductType.SAVING))
        .thenReturn(new BigDecimal("5000"));

    // Act
    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    // Assert
    assertNotNull(response);
    assertEquals(testUserId, response.getUserId());
    assertEquals(1, response.getRecommendations().size());

    ProductRecommendation recommendation = response.getRecommendations().get(0);
    assertEquals("Invest 500", recommendation.getName());
    assertEquals(UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"), recommendation.getId());

    verify(repository, times(1)).userHasProductType(testUserId, ProductType.DEBIT);
    verify(repository, times(1)).userHasProductType(testUserId, ProductType.INVEST);
    verify(repository, times(1)).getTotalDepositAmountByProductType(testUserId, ProductType.SAVING);
  }

  @Test
  void getRecommendations_WhenUserEligibleForAllProducts_ShouldReturnThreeRecommendations() {
    // Arrange - пользователь подходит под все три продукта
    when(repository.userHasProductType(testUserId, ProductType.DEBIT)).thenReturn(true);
    when(repository.userHasProductType(testUserId, ProductType.INVEST)).thenReturn(false);
    when(repository.userHasProductType(testUserId, ProductType.CREDIT)).thenReturn(false);

    when(repository.getTotalDepositAmountByProductType(testUserId, ProductType.SAVING))
        .thenReturn(new BigDecimal("50000"));
    when(repository.getTotalDepositAmountByProductType(testUserId, ProductType.DEBIT))
        .thenReturn(new BigDecimal("100000"));
    when(repository.getTotalSpendAmountByProductType(testUserId, ProductType.DEBIT))
        .thenReturn(new BigDecimal("90000"));

    // Act
    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    // Assert
    assertNotNull(response);
    assertEquals(3, response.getRecommendations().size());

    // Проверяем, что все три продукта присутствуют
    assertTrue(response.getRecommendations().stream()
        .anyMatch(r -> r.getName().equals("Invest 500")));
    assertTrue(response.getRecommendations().stream()
        .anyMatch(r -> r.getName().equals("Top Saving")));
    assertTrue(response.getRecommendations().stream()
        .anyMatch(r -> r.getName().equals("Простой кредит")));
  }

  @Test
  void getRecommendations_WhenUserNotEligible_ShouldReturnEmptyList() {
    // Arrange - пользователь не подходит ни под один продукт
    when(repository.userHasProductType(testUserId, ProductType.DEBIT)).thenReturn(false);

    // Act
    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    // Assert
    assertNotNull(response);
    assertEquals(testUserId, response.getUserId());
    assertTrue(response.getRecommendations().isEmpty());
  }

  @Test
  void isEligibleForInvest500_WhenAllConditionsMet_ShouldReturnTrue() {
    // Arrange
    when(repository.userHasProductType(testUserId, ProductType.DEBIT)).thenReturn(true);
    when(repository.userHasProductType(testUserId, ProductType.INVEST)).thenReturn(false);
    when(repository.getTotalDepositAmountByProductType(testUserId, ProductType.SAVING))
        .thenReturn(new BigDecimal("1500"));

    // Act & Assert
    assertTrue(recommendationService.isEligibleForInvest500(testUserId));
  }

  @Test
  void isEligibleForInvest500_WhenNoDebitProduct_ShouldReturnFalse() {
    // Arrange
    when(repository.userHasProductType(testUserId, ProductType.DEBIT)).thenReturn(false);

    // Act & Assert
    assertFalse(recommendationService.isEligibleForInvest500(testUserId));
  }
}