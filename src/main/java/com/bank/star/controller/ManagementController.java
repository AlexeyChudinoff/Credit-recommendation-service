// управление сервисом
package com.bank.star.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/management")
@RequiredArgsConstructor
@Tag(name = "Management API", description = "API для управления сервисом")
public class ManagementController {

  private final com.bank.star.service.RecommendationService recommendationService;

  @Operation(
      summary = "Сброс кешей",
      description = "Очищает все закешированные результаты рекомендаций"
  )
  @PostMapping("/clear-caches")
  public ResponseEntity<String> clearCaches() {
    recommendationService.clearCaches();
    return ResponseEntity.ok("✅ Все кеши успешно очищены");
  }

  @Operation(
      summary = "Информация о сервисе",
      description = "Возвращает название и версию сервиса"
  )
  @GetMapping("/info")
  public ResponseEntity<Map<String, String>> getServiceInfo() {
    return ResponseEntity.ok(Map.of(
        "name", "recommendation-service",
        "version", "1.0.0"
    ));
  }
}