package com.bank.star.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Стандартизированный ответ об ошибке API")
public class ErrorResponse {

  @Schema(description = "Код типа ошибки", example = "VALIDATION_ERROR", requiredMode = Schema.RequiredMode.REQUIRED)
  private String errorCode;

  @Schema(description = "Человекочитаемое сообщение об ошибке", example = "Неверный формат UUID пользователя", requiredMode = Schema.RequiredMode.REQUIRED)
  private String message;

  @Schema(description = "Время возникновения ошибки", example = "2024-01-15T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDateTime timestamp;

  public ErrorResponse(String errorCode, String message) {
    this.errorCode = errorCode;
    this.message = message;
    this.timestamp = LocalDateTime.now();
  }
}