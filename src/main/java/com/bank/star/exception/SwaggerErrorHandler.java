package com.bank.star.config;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SwaggerErrorHandler {

  // Перенаправление со старого пути на новый
  @GetMapping("/v3/api-docs")
  @Hidden
  public void redirectToApiDocs(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    response.setHeader("Location", "/api-docs");
  }

  // Обработчик исключений для SpringDoc
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleSpringDocException(Exception ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", "Error generating OpenAPI documentation");
    response.put("message", ex.getMessage());
    response.put("timestamp", System.currentTimeMillis());

    // Логируем ошибку для отладки
    System.err.println("SpringDoc error: " + ex.getMessage());
    ex.printStackTrace();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}