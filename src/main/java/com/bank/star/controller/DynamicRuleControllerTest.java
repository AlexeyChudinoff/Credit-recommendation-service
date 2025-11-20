package com.bank.star.controller;

import com.bank.star.dto.DynamicRuleRequest;
import com.bank.star.dto.DynamicRuleResponse;
import com.bank.star.dto.RuleListResponse;
import com.bank.star.dto.RuleQueryRequest;
import com.bank.star.model.QueryType;
import com.bank.star.repository.DynamicRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

  private final UUID testRuleId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
  private final UUID testProductId = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

  @Test
  void getAllRules_shouldReturnOk() throws Exception {
    // Given
    when(dynamicRuleRepository.findAll()).thenReturn(List.of());

    // When & Then
    mockMvc.perform(get("/api/v1/rules"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").exists());
  }

  @Test
  void createRule_withValidRequest_shouldReturnOk() throws Exception {
    // Given
    DynamicRuleRequest request = createValidRuleRequest();

    when(dynamicRuleRepository.save(any())).thenReturn(any());

    // When & Then
    mockMvc.perform(post("/api/v1/rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  void deleteRule_withExistingRule_shouldReturnNoContent() throws Exception {
    // Given
    when(dynamicRuleRepository.existsById(testRuleId)).thenReturn(true);

    // When & Then
    mockMvc.perform(delete("/api/v1/rules/{ruleId}", testRuleId))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteRule_withNonExistentRule_shouldReturnNotFound() throws Exception {
    // Given
    when(dynamicRuleRepository.existsById(testRuleId)).thenReturn(false);

    // When & Then
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