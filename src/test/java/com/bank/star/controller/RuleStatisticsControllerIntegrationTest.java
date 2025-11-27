package com.bank.star.controller;

import com.bank.star.model.DynamicRule;
import com.bank.star.model.RuleStatistics;
import com.bank.star.repository.DynamicRuleRepository;
import com.bank.star.repository.RuleStatisticsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class RuleStatisticsControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private RuleStatisticsRepository statisticsRepository;

  @Autowired
  private DynamicRuleRepository dynamicRuleRepository;

  @Test
  void getRuleStats_IntegrationTest() throws Exception {
    // Given - создаем правило с обязательным productId
    DynamicRule rule = new DynamicRule();

    // Устанавливаем обязательные поля
    try {
      // Вариант 1: через рефлексию (если нет сеттеров)
      java.lang.reflect.Field productIdField = DynamicRule.class.getDeclaredField("productId");
      productIdField.setAccessible(true);
      productIdField.set(rule, UUID.randomUUID());

      // Если есть другие обязательные поля, установите их тоже
      java.lang.reflect.Field nameField = DynamicRule.class.getDeclaredField("name");
      nameField.setAccessible(true);
      nameField.set(rule, "Test Rule");

    } catch (Exception e) {
      // Если рефлексия не работает, используем существующее правило из БД
      rule = dynamicRuleRepository.findAll().stream()
          .findFirst()
          .orElseThrow(() -> new RuntimeException("No rules found in database"));
    }

    DynamicRule savedRule = dynamicRuleRepository.save(rule);

    RuleStatistics stats = new RuleStatistics(savedRule.getId(), 10L);
    statisticsRepository.save(stats);

    // When & Then
    mockMvc.perform(get("/api/v1/rule/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats[0].ruleId").value(savedRule.getId().toString()))
        .andExpect(jsonPath("$.stats[0].count").value(10));
  }

  @Test
  void getRuleStats_WhenNoStatistics_ShouldReturnZero() throws Exception {
    // Given - используем существующее правило из БД
    DynamicRule existingRule = dynamicRuleRepository.findAll().stream()
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No rules found in database"));

    // When & Then - для правила без статистики должен вернуть 0
    mockMvc.perform(get("/api/v1/rule/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats[0].ruleId").value(existingRule.getId().toString()))
        .andExpect(jsonPath("$.stats[0].count").value(0));
  }
}