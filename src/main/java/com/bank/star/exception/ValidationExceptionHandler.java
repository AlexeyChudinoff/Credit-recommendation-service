//валидация запросов
package com.bank.star.exception;

import com.bank.star.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(1) // Высший приоритет - будет выполняться первым
public class ValidationExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    logger.warn("Ошибка валидации полей: {}", errorMessage);

    ErrorResponse error = new ErrorResponse(
        "VALIDATION_ERROR",
        errorMessage,
        LocalDateTime.now()
    );

    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    String message = e.getMessage();

    // Форматируем сообщение для UUID
    if (message.contains("Invalid UUID") || message.contains("invalid UUID")) {
      message = "Неверный формат UUID пользователя. Пример корректного UUID: cd515076-5d8a-44be-930e-8d4fcb79f42d";
      logger.warn("Неверный UUID формат: {}", e.getMessage());
    } else {
      logger.warn("Ошибка валидации аргумента: {}", message);
    }

    ErrorResponse error = new ErrorResponse(
        "VALIDATION_ERROR",
        message,
        LocalDateTime.now()
    );

    return ResponseEntity.badRequest().body(error);
  }
}