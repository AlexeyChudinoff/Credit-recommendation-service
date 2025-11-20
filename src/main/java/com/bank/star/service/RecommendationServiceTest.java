package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.exception.UserNotFoundException;
import com.bank.star.repository.DynamicRuleRepository;
import com.bank.star.repository.RecommendationRepository;
import com.bank.star.service.rules.ProductRuleSets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

  @Mock
  private ProductRuleSets productRuleSets;

  @Mock
  private RecommendationRepository repository;

  @Mock
  private DynamicRuleRepository dynamicRuleRepository;

  @Mock
  private InMemoryRuleStatisticsService statisticsService;

  private RecommendationService recommendationService;

  private final UUID testUserId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

  @BeforeEach
  void setUp() {
    recommendationService = new RecommendationService(
        productRuleSets, repository, dynamicRuleRepository, statisticsService
    );
  }

  @Test
  void getRecommendations_withValidUser_shouldReturnRecommendations() {
    // Given
    when(repository.userExists(testUserId)).thenReturn(true);
    when(productRuleSets.getSimpleCreditRuleSet()).thenReturn(mock(com.bank.star.service.rules.RecommendationRule.class));
    when(productRuleSets.getTopSavingRuleSet()).thenReturn(mock(com.bank.star.service.rules.RecommendationRule.class));
    when(productRuleSets.getInvest500RuleSet()).thenReturn(mock(com.bank.star.service.rules.RecommendationRule.class));

    // When
    RecommendationResponse response = recommendationService.getRecommendations(testUserId);

    // Then
    assertNotNull(response);
    assertEquals(testUserId, response.getUserId());
    assertNotNull(response.getRecommendations());
    verify(repository).userExists(testUserId);
  }

  @Test
  void getRecommendations_withNonExistentUser_shouldThrowException() {
    // Given
    when(repository.userExists(testUserId)).thenReturn(false);

    // When & Then
    assertThrows(UserNotFoundException.class, () -> {
      recommendationService.getRecommendations(testUserId);
    });
  }

  @Test
  void getRecommendations_withNullUserId_shouldThrowException() {
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      recommendationService.getRecommendations(null);
    });
  }

  @Test
  void clearCaches_shouldCallStatisticsService() {
    // When
    recommendationService.clearCaches();

    // Then
    verify(statisticsService).clearStatistics();
  }
}