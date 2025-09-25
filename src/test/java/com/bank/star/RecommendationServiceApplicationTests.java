package com.bank.star;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class RecommendationServiceApplicationTests {

  @Test
  void contextLoads() {
    // Простой тест для проверки загрузки контекста Spring
    assertTrue(true, "Контекст Spring должен загружаться успешно");
  }

  @Test
  void applicationStartsSuccessfully() {
    // Дополнительный тест для проверки запуска приложения
    RecommendationServiceApplication.main(new String[]{});
    assertTrue(true, "Приложение должно запускаться без ошибок");
  }
}