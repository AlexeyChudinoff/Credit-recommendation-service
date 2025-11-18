package com.bank.star.exception;

import com.bank.star.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ValidationExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    logger.warn("Ошибка валидации: {}", errorMessage);

    ErrorResponse error = new ErrorResponse(
        "VALIDATION_ERROR",
        errorMessage,
        LocalDateTime.now()
    );

    return ResponseEntity.badRequest().body(error);
  }
}