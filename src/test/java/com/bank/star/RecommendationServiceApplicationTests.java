package com.bank.star;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RecommendationServiceApplicationTests {

  @Test
  void contextLoads() {
    // Простой тест для проверки загрузки контекста Spring
    assertTrue(true, "Контекст Spring должен загружаться успешно");
  }
}