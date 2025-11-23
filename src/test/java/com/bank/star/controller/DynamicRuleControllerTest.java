package com.bank.star.controller;

import com.bank.star.dto.DynamicRuleRequest;
import com.bank.star.dto.DynamicRuleResponse;
import com.bank.star.dto.RuleQueryRequest;
import com.bank.star.model.QueryType;
import com.bank.star.repository.DynamicRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DynamicRuleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private DynamicRuleRepository dynamicRuleRepository;

  @MockBean
  private BuildProperties buildProperties; // Мок BuildProperties

  private final UUID testRuleId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

  private final UUID testProductId = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

  @BeforeEach
  void setupBuildProperties() {
    when(buildProperties.getName()).thenReturn("bank-star");
    when(buildProperties.getVersion()).thenReturn("1.0.0");
    when(buildProperties.getTime()).thenReturn(java.time.Instant.now());
    when(buildProperties.getArtifact()).thenReturn("bank-service");
    when(buildProperties.getGroup()).thenReturn("com.bank.star");
  }

  @Test
  void getAllRules_shouldReturnOk() throws Exception {
    when(dynamicRuleRepository.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/rules"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").exists());
  }

  @Test
  void createRule_withValidRequest_shouldReturnOk() throws Exception {
    DynamicRuleRequest request = createValidRuleRequest();
    when(dynamicRuleRepository.save(any())).thenAnswer(invocation -> {
      // Для имитации успешного сохранения просто вернуть первый аргумент
      return invocation.getArgument(0);
    });

    mockMvc.perform(post("/api/v1/rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.productName").value("Test Product"));
  }

  @Test
  void deleteRule_withExistingRule_shouldReturnNoContent() throws Exception {
    when(dynamicRuleRepository.existsById(testRuleId)).thenReturn(true);

    mockMvc.perform(delete("/api/v1/rules/{ruleId}", testRuleId))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteRule_withNonExistentRule_shouldReturnNotFound() throws Exception {
    when(dynamicRuleRepository.existsById(testRuleId)).thenReturn(false);

    mockMvc.perform(delete("/api/v1/rules/{ruleId}", testRuleId))
        .andExpect(status().isNotFound());
  }

  private DynamicRuleRequest createValidRuleRequest() {
    RuleQueryRequest queryRequest = new RuleQueryRequest(
        QueryType.USER_OF,
        List.of("DEBIT"),
        false
    );
    return new DynamicRuleRequest(
        "Test Product",
        testProductId,
        "Test product description",
        List.of(queryRequest)
    );
  }
}
