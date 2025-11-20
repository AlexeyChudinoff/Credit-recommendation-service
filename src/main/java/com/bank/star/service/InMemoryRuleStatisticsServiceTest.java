package com.bank.star.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRuleStatisticsServiceTest {

  private InMemoryRuleStatisticsService statisticsService;

  private final UUID testRuleId = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

  @BeforeEach
  void setUp() {
    statisticsService = new InMemoryRuleStatisticsService();
  }

  @Test
  void incrementRuleCount_shouldIncreaseCount() {
    // When
    statisticsService.incrementRuleCount(testRuleId);
    statisticsService.incrementRuleCount(testRuleId);

    // Then
    assertEquals(2, statisticsService.getRuleCount(testRuleId));
  }

  @Test
  void getRuleCount_forNonExistentRule_shouldReturnZero() {
    // When
    long count = statisticsService.getRuleCount(UUID.randomUUID());

    // Then
    assertEquals(0, count);
  }

  @Test
  void getAllStatistics_shouldReturnAllCounts() {
    // Given
    UUID ruleId1 = UUID.randomUUID();
    UUID ruleId2 = UUID.randomUUID();

    // When
    statisticsService.incrementRuleCount(ruleId1);
    statisticsService.incrementRuleCount(ruleId1);
    statisticsService.incrementRuleCount(ruleId2);

    var stats = statisticsService.getAllStatistics();

    // Then
    assertEquals(2, stats.size());
    assertEquals(2, stats.get(ruleId1));
    assertEquals(1, stats.get(ruleId2));
  }

  @Test
  void clearStatistics_shouldRemoveAllCounts() {
    // Given
    statisticsService.incrementRuleCount(testRuleId);

    // When
    statisticsService.clearStatistics();

    // Then
    assertEquals(0, statisticsService.getRuleCount(testRuleId));
    assertTrue(statisticsService.getAllStatistics().isEmpty());
  }
}