//для динамических правил
package com.bank.star.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с созданным динамическим правилом")
public class DynamicRuleResponse {

  @Schema(
      description = "UUID созданного правила",
      example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  )
  private UUID id;

  @Schema(
      description = "Название рекомендуемого продукта",
      example = "Простой кредит"
  )
  private String productName;

  @Schema(
      description = "UUID рекомендуемого продукта",
      example = "ab138afb-f3ba-4a93-b74f-0fcee86d447f"
  )
  private UUID productId;

  @Schema(
      description = "Текст рекомендации",
      example = "Откройте мир выгодных кредитов с нами!"
  )
  private String productText;

  @Schema(description = "Список условий правила")
  private List<RuleQueryRequest> rule;
}