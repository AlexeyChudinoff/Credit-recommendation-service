package com.bank.star.controller;

import com.bank.star.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ManagementControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RecommendationService recommendationService;

  @Test
  void clearCaches_shouldReturnOk() throws Exception {
    // When & Then
    mockMvc.perform(post("/management/clear-caches"))
        .andExpect(status().isOk())
        .andExpect(content().string("✅ Все кеши успешно очищены"));

    verify(recommendationService).clearCaches();
  }

  @Test
  void getServiceInfo_shouldReturnServiceInfo() throws Exception {
    // When & Then
    mockMvc.perform(get("/management/info"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("recommendation-service"))
        .andExpect(jsonPath("$.version").value("1.0.0"));
  }
}