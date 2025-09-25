package com.bank.star.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.service.RecommendationService;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RecommendationService recommendationService;

  private final UUID testUserId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

  @Test
  void getRecommendations_WhenValidUserId_ShouldReturnRecommendations() throws Exception {
    // Arrange
    ProductRecommendation product = new ProductRecommendation(
        "Invest 500",
        UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"),
        "Test description"
    );

    RecommendationResponse response = new RecommendationResponse(
        testUserId,
        Arrays.asList(product)
    );

    when(recommendationService.getRecommendations(testUserId)).thenReturn(response);

    // Act & Assert
    mockMvc.perform(get("/recommendation/{userId}", testUserId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.userId").value(testUserId.toString()))
        .andExpect(jsonPath("$.recommendations[0].name").value("Invest 500"))
        .andExpect(jsonPath("$.recommendations[0].id").value("147f6a0f-3b91-413b-ab99-87f081d60d5a"));
  }

  @Test
  void getRecommendations_WhenInvalidUserId_ShouldReturnBadRequest() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/recommendation/{userId}", "invalid-uuid"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getRecommendations_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    // Arrange
    when(recommendationService.getRecommendations(any(UUID.class)))
        .thenThrow(new RuntimeException("Database error"));

    // Act & Assert
    mockMvc.perform(get("/recommendation/{userId}", testUserId))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void healthCheck_ShouldReturnHealthyStatus() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/recommendation/health"))
        .andExpect(status().isOk())
        .andExpect(content().string("Service is healthy!"));
  }
}