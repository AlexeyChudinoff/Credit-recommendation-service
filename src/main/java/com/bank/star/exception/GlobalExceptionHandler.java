package com.bank.star.exception;

import com.bank.star.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
    logger.warn("User not found: {}", e.getMessage());

    ErrorResponse error = new ErrorResponse(
        "USER_NOT_FOUND",
        e.getMessage(),
        LocalDateTime.now()
    );

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    logger.warn("Validation error: {}", e.getMessage());

    ErrorResponse error = new ErrorResponse(
        "VALIDATION_ERROR",
        "Неверный формат UUID пользователя: " + e.getMessage(),
        LocalDateTime.now()
    );

    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
    logger.error("Internal server error: {}", e.getMessage(), e);

    ErrorResponse error = new ErrorResponse(
        "INTERNAL_SERVER_ERROR",
        "Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.",
        LocalDateTime.now()
    );

    return ResponseEntity.internalServerError().body(error);
  }
}