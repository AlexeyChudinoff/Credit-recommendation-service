// DTO для статистики
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
@Schema(description = "Ответ со статистикой срабатываний правил")
public class RuleStatsResponse {

  @Schema(description = "Список статистики по правилам")
  private List<RuleStat> stats;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Статистика одного правила")
  public static class RuleStat {

    @Schema(description = "ID правила", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID ruleId;

    @Schema(description = "Количество срабатываний", example = "42")
    private Long count;
  }
}