package com.bank.star.controller;

import com.bank.star.dto.RecommendationResponse;
import com.bank.star.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RecommendationService recommendationService;

  private final UUID testUserId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

  @Test
  void getRecommendations_withValidUserId_shouldReturnOk() throws Exception {
    // Given
    RecommendationResponse mockResponse = new RecommendationResponse(testUserId, java.util.List.of());
    when(recommendationService.getRecommendations(any(UUID.class))).thenReturn(mockResponse);

    // When & Then
    mockMvc.perform(get("/api/v1/recommendations/{userId}", testUserId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.userId").exists())
        .andExpect(jsonPath("$.recommendations").exists());
  }

  @Test
  void getRecommendations_withInvalidUserId_shouldReturnBadRequest() throws Exception {
    // Given
    String invalidUserId = "invalid-uuid";

    // When & Then
    mockMvc.perform(get("/api/v1/recommendations/{userId}", invalidUserId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void healthCheck_shouldReturnOk() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/recommendations/health"))
        .andExpect(status().isOk());
  }

  @Test
  void infoEndpoint_shouldReturnOk() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/recommendations/info"))
        .andExpect(status().isOk());
  }

  @Test
  void statsEndpoint_shouldReturnOk() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/recommendations/stats"))
        .andExpect(status().isOk());
  }
}