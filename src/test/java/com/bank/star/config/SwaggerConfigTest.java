package com.bank.star.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void swaggerUI_ShouldBeAccessible() throws Exception {
    mockMvc.perform(get("/swagger-ui/index.html"))
        .andExpect(status().isOk());
  }

  @Test
  void apiDocs_ShouldReturnOpenAPISpec() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi").exists())
        .andExpect(jsonPath("$.info.title").value("🏦 Bank Star Recommendation Service API"));
  }

  @Test
  void customApiDocsEndpoint_ShouldBeAccessible() throws Exception {
    mockMvc.perform(get("/api-docs"))
        .andExpect(status().isOk());
  }
}