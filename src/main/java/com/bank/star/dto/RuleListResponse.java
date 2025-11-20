//ответы правил
package com.bank.star.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ со списком всех динамических правил")
public class RuleListResponse {

  @Schema(description = "Список динамических правил")
  private List<DynamicRuleResponse> data;
}