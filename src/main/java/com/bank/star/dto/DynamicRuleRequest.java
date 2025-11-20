//для динамических правил
package com.bank.star.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание динамического правила рекомендаций")
public class DynamicRuleRequest {

  @NotBlank(message = "Название продукта обязательно")
  @Schema(
      description = "Название рекомендуемого продукта",
      example = "Простой кредит",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private String productName;

  @NotNull(message = "ID продукта обязателен")
  @Schema(
      description = "UUID рекомендуемого продукта",
      example = "ab138afb-f3ba-4a93-b74f-0fcee86d447f",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private UUID productId;

  @NotBlank(message = "Текст рекомендации обязателен")
  @Schema(
      description = "Текст рекомендации для отображения пользователю",
      example = "Откройте мир выгодных кредитов с нами!",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private String productText;

  @Valid
  @Size(min = 1, message = "Правило должно содержать хотя бы одно условие")
  @Schema(
      description = "Список условий правила",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private List<RuleQueryRequest> rule;
}