package com.bank.star.service;

import com.bank.star.dto.RecommendationResponse;
import com.bank.star.model.ProductType;
import com.bank.star.repository.RecommendationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class RecommendationServiceTest {

  @MockBean
  private RecommendationRepository repository;

  @Autowired
  private RecommendationService recommendationService;

  private final UUID testUserId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

  @Test
  void whenUserEligibleForInvest500_thenReturnInvest500Recommendation() {
    // Настраиваем только необходимые моки для Invest 500
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.DEBIT))).thenReturn(true);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.INVEST))).thenReturn(false);
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.SAVING)))
        .thenReturn(new BigDecimal("5000"));
    // Добавляем моки для других методов, которые могут вызываться
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.getTotalSpendAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(BigDecimal.ZERO);

    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    assertNotNull(response);
    assertEquals(testUserId, response.getUserId());
    assertEquals(1, response.getRecommendationsCount());
    assertEquals("Invest 500", response.getRecommendations().get(0).getName());
  }

  @Test
  void whenUserEligibleForTopSaving_thenReturnTopSavingRecommendation() {
    // Настраиваем только необходимые моки для Top Saving
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.DEBIT))).thenReturn(true);
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(new BigDecimal("60000"));
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.SAVING)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.getTotalSpendAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(new BigDecimal("40000"));
    // Добавляем моки для других методов
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.INVEST))).thenReturn(false);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.CREDIT))).thenReturn(false);

    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    assertNotNull(response);
    assertEquals(1, response.getRecommendationsCount());
    assertEquals("Top Saving", response.getRecommendations().get(0).getName());
  }

  @Test
  void whenUserEligibleForSimpleCredit_thenReturnSimpleCreditRecommendation() {
    // Настраиваем только необходимые моки для Simple Credit
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.CREDIT))).thenReturn(false);
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(new BigDecimal("200000"));
    when(repository.getTotalSpendAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(new BigDecimal("150000"));
    // Добавляем моки для других методов
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.DEBIT))).thenReturn(false);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.INVEST))).thenReturn(false);
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.SAVING)))
        .thenReturn(BigDecimal.ZERO);

    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    assertNotNull(response);
    assertEquals(1, response.getRecommendationsCount());
    assertEquals("Простой кредит", response.getRecommendations().get(0).getName());
  }

  @Test
  void whenUserEligibleForMultipleProducts_thenReturnMultipleRecommendations() {
    // Настраиваем моки для нескольких продуктов
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.DEBIT))).thenReturn(true);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.INVEST))).thenReturn(false);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.CREDIT))).thenReturn(false);
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.SAVING)))
        .thenReturn(new BigDecimal("5000"));
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(new BigDecimal("60000"));
    when(repository.getTotalSpendAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(new BigDecimal("40000"));

    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    assertNotNull(response);
    assertEquals(2, response.getRecommendationsCount());
    assertTrue(response.getRecommendations().stream()
        .anyMatch(r -> r.getName().equals("Invest 500")));
    assertTrue(response.getRecommendations().stream()
        .anyMatch(r -> r.getName().equals("Top Saving")));
  }

  @Test
  void whenUserNotEligibleForAnyProduct_thenReturnEmptyRecommendations() {
    // Пользователь без дебетовой карты не подходит ни для чего
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.DEBIT))).thenReturn(false);
    // Добавляем моки для всех методов суммы, чтобы они возвращали ZERO вместо null
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.SAVING)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.getTotalSpendAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.INVEST))).thenReturn(false);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.CREDIT))).thenReturn(false);

    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    assertNotNull(response);
    assertEquals(0, response.getRecommendationsCount());
    assertTrue(response.getRecommendations().isEmpty());
  }

  @Test
  void whenUserIdIsNull_thenThrowException() {
    assertThrows(IllegalArgumentException.class, () -> {
      recommendationService.getRecommendations(null);
    });
  }

  @Test
  void invest500Eligibility_allConditionsMet_returnsTrue() {
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.DEBIT))).thenReturn(true);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.INVEST))).thenReturn(false);
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.SAVING)))
        .thenReturn(new BigDecimal("2000"));
    // Добавляем моки для других методов
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.getTotalSpendAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.CREDIT))).thenReturn(false);

    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    assertTrue(response.getRecommendations().stream()
        .anyMatch(r -> r.getName().equals("Invest 500")));
  }

  @Test
  void invest500Eligibility_noDebitProduct_returnsFalse() {
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.DEBIT))).thenReturn(false);
    // Добавляем моки для всех методов суммы
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.SAVING)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.getTotalDepositAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.getTotalSpendAmountByProductType(eq(testUserId), eq(ProductType.DEBIT)))
        .thenReturn(BigDecimal.ZERO);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.INVEST))).thenReturn(false);
    when(repository.userHasProductType(eq(testUserId), eq(ProductType.CREDIT))).thenReturn(false);

    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    assertFalse(response.getRecommendations().stream()
        .anyMatch(r -> r.getName().equals("Invest 500")));
  }
}