// статистика правил
package com.bank.star.controller;

import com.bank.star.dto.RuleStatsResponse;
import com.bank.star.repository.RuleStatisticsRepository;
import com.bank.star.repository.DynamicRuleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rule")
@RequiredArgsConstructor
@Tag(name = "Rule Statistics API", description = "API для получения статистики срабатывания правил")
public class RuleStatisticsController {

  private final RuleStatisticsRepository statisticsRepository;
  private final DynamicRuleRepository dynamicRuleRepository;

  @Operation(
      summary = "Получить статистику срабатываний правил",
      description = "Возвращает статистику выполнения всех динамических правил"
  )
  @GetMapping("/stats")
  public ResponseEntity<RuleStatsResponse> getRuleStats() {
    var stats = dynamicRuleRepository.findAll().stream()
        .map(rule -> {
          Long count = statisticsRepository.findByRuleId(rule.getId())
              .map(stat -> stat.getExecutionCount())
              .orElse(0L);
          return new RuleStatsResponse.RuleStat(rule.getId(), count);
        })
        .collect(Collectors.toList());

    RuleStatsResponse response = new RuleStatsResponse(stats);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Очистить статистику правил",
      description = "Сбрасывает всю статистику срабатываний правил"
  )
  @PostMapping("/stats/clear")
  public ResponseEntity<String> clearRuleStats() {
    statisticsRepository.deleteAll();
    return ResponseEntity.ok("✅ Статистика правил очищена");
  }
}