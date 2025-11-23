package com.bank.star.controller;

import com.bank.star.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ManagementControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RecommendationService recommendationService;

  @MockBean
  private BuildProperties buildProperties;

  @BeforeEach
  void setup() {
    // Мок BuildProperties для успешной загрузки ApplicationContext
    when(buildProperties.getName()).thenReturn("bank-star");
    when(buildProperties.getVersion()).thenReturn("1.0.0");
    when(buildProperties.getTime()).thenReturn(Instant.now());
    when(buildProperties.getArtifact()).thenReturn("bank-service");
    when(buildProperties.getGroup()).thenReturn("com.bank.star");

    // Мок для сервиса, чтобы clearCaches отрабатывал без ошибок
    doNothing().when(recommendationService).clearCaches();
  }

  @Test
  void clearCaches_shouldReturnOkMessage() throws Exception {
    mockMvc.perform(post("/management/clear-caches"))
        .andExpect(status().isOk())
        .andExpect(content().string("✅ Все кеши успешно очищены"));
  }

  @Test
  void getServiceInfo_shouldReturnNameAndVersion() throws Exception {
    mockMvc.perform(get("/management/info")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("recommendation-service"))
        .andExpect(jsonPath("$.version").value("1.0.0"));
  }
}
