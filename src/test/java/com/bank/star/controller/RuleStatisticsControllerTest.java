package com.bank.star.controller;

import com.bank.star.service.InMemoryRuleStatisticsService;
import com.bank.star.repository.DynamicRuleRepository;
import com.bank.star.model.DynamicRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
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

  @MockBean
  private BuildProperties buildProperties;

  private final UUID testRuleId1 = UUID.randomUUID();
  private final UUID testRuleId2 = UUID.randomUUID();

  private final DynamicRule rule1 = mock(DynamicRule.class);
  private final DynamicRule rule2 = mock(DynamicRule.class);

  @BeforeEach
  void setup() {
    when(buildProperties.getName()).thenReturn("bank-star");
    when(buildProperties.getVersion()).thenReturn("1.0.0");
    when(buildProperties.getTime()).thenReturn(Instant.now());
    when(buildProperties.getArtifact()).thenReturn("bank-service");
    when(buildProperties.getGroup()).thenReturn("com.bank.star");

    when(rule1.getId()).thenReturn(testRuleId1);
    when(rule2.getId()).thenReturn(testRuleId2);
    when(dynamicRuleRepository.findAll()).thenReturn(List.of(rule1, rule2));

    when(statisticsService.getRuleCount(testRuleId1)).thenReturn(5L);
    when(statisticsService.getRuleCount(testRuleId2)).thenReturn(10L);
    doNothing().when(statisticsService).clearStatistics();
  }

  @Test
  void getRuleStats_shouldReturnStats() throws Exception {
    mockMvc.perform(get("/api/v1/rule/stats")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats").isArray())
        .andExpect(jsonPath("$.stats[0].ruleId").value(testRuleId1.toString()))
        .andExpect(jsonPath("$.stats[0].count").value(5))
        .andExpect(jsonPath("$.stats[1].ruleId").value(testRuleId2.toString()))
        .andExpect(jsonPath("$.stats[1].count").value(10));
  }

  @Test
  void clearRuleStats_shouldReturnOkMessage() throws Exception {
    mockMvc.perform(post("/api/v1/rule/stats/clear"))
        .andExpect(status().isOk())
        .andExpect(content().string("✅ Статистика правил очищена"));

    verify(statisticsService, times(1)).clearStatistics();
  }
}
