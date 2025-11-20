//условия правил
package com.bank.star.dto;

import com.bank.star.model.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос условия правила")
public class RuleQueryRequest {

  @NotNull(message = "Тип запроса обязателен")
  @Schema(
      description = "Тип запроса",
      example = "USER_OF",
      requiredMode = Schema.RequiredMode.REQUIRED,
      allowableValues = {"USER_OF", "ACTIVE_USER_OF", "TRANSACTION_SUM_COMPARE", "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW"}
  )
  private QueryType query;

  @NotEmpty(message = "Аргументы запроса не могут быть пустыми")
  @Schema(
      description = "Аргументы запроса",
      example = "[\"CREDIT\"]",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private List<String> arguments;

  @Schema(
      description = "Отрицание условия (true = условие НЕ должно выполняться)",
      example = "true",
      defaultValue = "false"
  )
  private boolean negate = false;

  // Убираем геттеры для отдельных аргументов - они создают проблемы в Swagger
  // Вместо этого будем использовать прямую работу с arguments
}