package com.bank.star.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test API", description = "Тестовые endpoints")
public class TestController {

  @GetMapping("/ping")
  @Operation(summary = "Проверка работы API")
  public String ping() {
    return "API работает! " + new java.util.Date();
  }

  @GetMapping("/health")
  @Operation(summary = "Проверка здоровья сервиса")
  public String health() {
    return "{\"status\": \"UP\"}";
  }
}