package com.bank.star.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRuleStatisticsServiceTest {

  private InMemoryRuleStatisticsService statisticsService;

  @BeforeEach
  void setUp() {
    statisticsService = new InMemoryRuleStatisticsService();
  }

  @Test
  void incrementRuleCount_andGetRuleCount() {
    UUID ruleId = UUID.randomUUID();

    assertEquals(0, statisticsService.getRuleCount(ruleId));

    statisticsService.incrementRuleCount(ruleId);
    assertEquals(1, statisticsService.getRuleCount(ruleId));

    statisticsService.incrementRuleCount(ruleId);
    statisticsService.incrementRuleCount(ruleId);
    assertEquals(3, statisticsService.getRuleCount(ruleId));
  }

  @Test
  void getAllStatistics_returnsCorrectCounts() {
    UUID ruleId1 = UUID.randomUUID();
    UUID ruleId2 = UUID.randomUUID();

    statisticsService.incrementRuleCount(ruleId1);
    statisticsService.incrementRuleCount(ruleId1);
    statisticsService.incrementRuleCount(ruleId2);

    Map<UUID, Long> allStats = statisticsService.getAllStatistics();

    assertEquals(2, allStats.get(ruleId1));
    assertEquals(1, allStats.get(ruleId2));
    assertEquals(2, allStats.size());
  }

  @Test
  void clearStatistics_clearsAllCounts() {
    UUID ruleId = UUID.randomUUID();

    statisticsService.incrementRuleCount(ruleId);
    assertEquals(1, statisticsService.getRuleCount(ruleId));

    statisticsService.clearStatistics();
    assertEquals(0, statisticsService.getRuleCount(ruleId));
    assertTrue(statisticsService.getAllStatistics().isEmpty());
  }
}
