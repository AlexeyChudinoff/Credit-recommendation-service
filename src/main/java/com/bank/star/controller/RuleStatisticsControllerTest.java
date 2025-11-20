package com.bank.star.controller;

import com.bank.star.model.DynamicRule;
import com.bank.star.repository.DynamicRuleRepository;
import com.bank.star.service.InMemoryRuleStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RuleStatisticsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private InMemoryRuleStatisticsService statisticsService;

  @MockBean
  private DynamicRuleRepository dynamicRuleRepository;

  private final UUID testRuleId = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

  @Test
  void getRuleStats_shouldReturnOk() throws Exception {
    // Given
    DynamicRule rule = new DynamicRule();
    rule.setId(testRuleId);
    when(dynamicRuleRepository.findAll()).thenReturn(List.of(rule));
    when(statisticsService.getRuleCount(testRuleId)).thenReturn(5L);

    // When & Then
    mockMvc.perform(get("/api/v1/rule/stats"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.stats").exists());
  }

  @Test
  void clearRuleStats_shouldReturnOk() throws Exception {
    // When & Then
    mockMvc.perform(post("/api/v1/rule/stats/clear"))
        .andExpect(status().isOk());

    verify(statisticsService).clearStatistics();
  }
}