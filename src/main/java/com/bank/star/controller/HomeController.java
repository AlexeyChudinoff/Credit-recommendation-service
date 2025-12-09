package com.bank.star.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Home", description = "Корневой endpoint и информация о сервисе")
public class HomeController {

  @GetMapping("/")
  @Operation(summary = "Корневой endpoint", description = "Возвращает информацию о сервисе")
  public ResponseEntity<Map<String, String>> home() {
    return ResponseEntity.ok(Map.of(
        "service", "Bank Star Recommendation Service",
        "version", "1.0.0",
        "status", "RUNNING",
        "documentation", "/swagger-ui.html",
        "h2-console", "/h2-console",
        "actuator", "/actuator"
    ));
  }
}