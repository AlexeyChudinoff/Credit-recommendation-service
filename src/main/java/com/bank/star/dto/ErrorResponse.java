package com.bank.star.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Objects;

@Schema(description = "Стандартизированный ответ об ошибке API")
public class ErrorResponse {

  @Schema(
      description = "Код типа ошибки",
      example = "VALIDATION_ERROR",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private String errorCode;

  @Schema(
      description = "Человекочитаемое сообщение об ошибке",
      example = "Неверный формат UUID пользователя",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private String message;

  @Schema(
      description = "Время возникновения ошибки",
      example = "2024-01-15T10:30:00",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private LocalDateTime timestamp;

  // Конструкторы
  public ErrorResponse() {
    this.timestamp = LocalDateTime.now();
  }

  public ErrorResponse(String errorCode, String message, LocalDateTime timestamp) {
    this.errorCode = Objects.requireNonNull(errorCode, "Код ошибки не может быть null");
    this.message = Objects.requireNonNull(message, "Сообщение об ошибке не может быть null");
    this.timestamp = Objects.requireNonNull(timestamp, "Временная метка не может быть null");
  }

  // Геттеры и сеттеры
  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return "ErrorResponse{" +
        "errorCode='" + errorCode + '\'' +
        ", message='" + message + '\'' +
        ", timestamp=" + timestamp +
        '}';
  }
}