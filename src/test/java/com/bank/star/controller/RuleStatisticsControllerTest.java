package com.bank.star.controller;

import com.bank.star.dto.RuleStatsResponse;
import com.bank.star.model.DynamicRule;
import com.bank.star.model.RuleStatistics;
import com.bank.star.repository.DynamicRuleRepository;
import com.bank.star.repository.RuleStatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RuleStatisticsController.class)
class RuleStatisticsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RuleStatisticsRepository statisticsRepository;

  @MockBean
  private DynamicRuleRepository dynamicRuleRepository;

  private UUID ruleId1;
  private UUID ruleId2;
  private DynamicRule rule1;
  private DynamicRule rule2;

  @BeforeEach
  void setUp() {
    ruleId1 = UUID.randomUUID();
    ruleId2 = UUID.randomUUID();

    // Создаем моки DynamicRule без использования setName
    rule1 = mock(DynamicRule.class);
    when(rule1.getId()).thenReturn(ruleId1);

    rule2 = mock(DynamicRule.class);
    when(rule2.getId()).thenReturn(ruleId2);
  }

  @Test
  void getRuleStats_ShouldReturnStatistics() throws Exception {
    // Given
    when(dynamicRuleRepository.findAll()).thenReturn(List.of(rule1, rule2));

    RuleStatistics stats1 = new RuleStatistics(ruleId1, 5L);
    RuleStatistics stats2 = new RuleStatistics(ruleId2, 0L);

    when(statisticsRepository.findByRuleId(ruleId1)).thenReturn(Optional.of(stats1));
    when(statisticsRepository.findByRuleId(ruleId2)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/v1/rule/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats.length()").value(2))
        .andExpect(jsonPath("$.stats[0].ruleId").value(ruleId1.toString()))
        .andExpect(jsonPath("$.stats[0].count").value(5))
        .andExpect(jsonPath("$.stats[1].ruleId").value(ruleId2.toString()))
        .andExpect(jsonPath("$.stats[1].count").value(0));

    verify(dynamicRuleRepository, times(1)).findAll();
    verify(statisticsRepository, times(1)).findByRuleId(ruleId1);
    verify(statisticsRepository, times(1)).findByRuleId(ruleId2);
  }

  @Test
  void getRuleStats_WhenNoRules_ShouldReturnEmptyList() throws Exception {
    // Given
    when(dynamicRuleRepository.findAll()).thenReturn(List.of());

    // When & Then
    mockMvc.perform(get("/api/v1/rule/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats.length()").value(0));

    verify(dynamicRuleRepository, times(1)).findAll();
    verify(statisticsRepository, never()).findByRuleId(any());
  }

  @Test
  void clearRuleStats_ShouldClearStatistics() throws Exception {
    // Given
    doNothing().when(statisticsRepository).deleteAll();

    // When & Then
    mockMvc.perform(post("/api/v1/rule/stats/clear"))
        .andExpect(status().isOk())
        .andExpect(content().string("✅ Статистика правил очищена"));

    verify(statisticsRepository, times(1)).deleteAll();
  }

  @Test
  void getRuleStats_WhenStatisticsExist_ShouldReturnCorrectCounts() throws Exception {
    // Given
    when(dynamicRuleRepository.findAll()).thenReturn(List.of(rule1));

    RuleStatistics stats = new RuleStatistics(ruleId1, 42L);
    when(statisticsRepository.findByRuleId(ruleId1)).thenReturn(Optional.of(stats));

    // When & Then
    mockMvc.perform(get("/api/v1/rule/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats[0].count").value(42));

    verify(statisticsRepository, times(1)).findByRuleId(ruleId1);
  }

  @Test
  void getRuleStats_WhenRuleHasNoStatistics_ShouldReturnZero() throws Exception {
    // Given
    when(dynamicRuleRepository.findAll()).thenReturn(List.of(rule1));
    when(statisticsRepository.findByRuleId(ruleId1)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/v1/rule/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats[0].count").value(0));

    verify(statisticsRepository, times(1)).findByRuleId(ruleId1);
  }
}