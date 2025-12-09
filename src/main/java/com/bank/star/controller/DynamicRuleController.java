//API для управление динамическими правилами
package com.bank.star.controller;

import com.bank.star.dto.*;
import com.bank.star.model.DynamicRule;
import com.bank.star.model.RuleQuery;
import com.bank.star.model.QueryType;
import com.bank.star.repository.DynamicRuleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Tag(name = "Dynamic Rules API", description = "API для управления динамическими правилами рекомендаций")
public class DynamicRuleController {

  private static final Logger logger = LoggerFactory.getLogger(DynamicRuleController.class);

  private final DynamicRuleRepository dynamicRuleRepository;

  @Operation(
      summary = "Создать новое динамическое правило",
      description = "Создает новое правило для рекомендаций банковских продуктов"
  )
  @PostMapping
  public ResponseEntity<DynamicRuleResponse> createRule(@RequestBody DynamicRuleRequest request) {
    logger.info("🔄 Создание нового динамического правила для продукта: {}", request.getProductName());

    try {
      // Преобразуем DTO в Entity
      DynamicRule dynamicRule = convertToEntity(request);

      // Сохраняем в базу
      DynamicRule savedRule = dynamicRuleRepository.save(dynamicRule);
      logger.info("✅ Динамическое правило создано с ID: {}", savedRule.getId());

      // Преобразуем обратно в DTO для ответа
      DynamicRuleResponse response = convertToResponse(savedRule);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("❌ Ошибка при создании правила: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(
      summary = "Получить все динамические правила",
      description = "Возвращает список всех созданных динамических правил рекомендаций"
  )
  @GetMapping
  public ResponseEntity<RuleListResponse> getAllRules() {
    logger.info("🔄 Получение списка всех динамических правил");

    try {
      List<DynamicRule> rules = dynamicRuleRepository.findAll();
      logger.info("✅ Найдено правил: {}", rules.size());

      List<DynamicRuleResponse> ruleResponses = rules.stream()
          .map(this::convertToResponse)
          .collect(Collectors.toList());

      RuleListResponse response = new RuleListResponse(ruleResponses);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("❌ Ошибка при получении списка правил: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Удалить правило по ID",
      description = "Удаляет динамическое правило рекомендаций по его идентификатору"
  )
  @DeleteMapping("/{ruleId}")
  public ResponseEntity<Void> deleteRule(@PathVariable UUID ruleId) {
    logger.info("🔄 Удаление правила с ID: {}", ruleId);

    try {
      if (!dynamicRuleRepository.existsById(ruleId)) {
        logger.warn("❌ Правило с ID {} не найдено", ruleId);
        return ResponseEntity.notFound().build();
      }

      dynamicRuleRepository.deleteById(ruleId);
      logger.info("✅ Правило с ID {} успешно удалено", ruleId);
      return ResponseEntity.noContent().build();

    } catch (Exception e) {
      logger.error("❌ Ошибка при удалении правила: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  // Вспомогательные методы для преобразования между DTO и Entity

  private DynamicRule convertToEntity(DynamicRuleRequest request) {
    DynamicRule rule = new DynamicRule();
    rule.setProductName(request.getProductName());
    rule.setProductId(request.getProductId());
    rule.setProductText(request.getProductText());

    // Преобразуем RuleQueryRequest в RuleQuery
    List<RuleQuery> queries = request.getRule().stream()
        .map(this::convertQueryToEntity)
        .collect(Collectors.toList());

    rule.setQueries(queries);
    return rule;
  }

  private RuleQuery convertQueryToEntity(RuleQueryRequest queryRequest) {
    RuleQuery query = new RuleQuery();
    query.setQuery(queryRequest.getQuery());
    query.setArguments(queryRequest.getArguments());
    query.setNegate(queryRequest.isNegate());
    return query;
  }

  // Вспомогательный метод для получения аргументов
  private String getArgument(List<String> arguments, int index) {
    return arguments != null && arguments.size() > index ? arguments.get(index) : null;
  }

  private DynamicRuleResponse convertToResponse(DynamicRule rule) {
    DynamicRuleResponse response = new DynamicRuleResponse();
    response.setId(rule.getId());
    response.setProductName(rule.getProductName());
    response.setProductId(rule.getProductId());
    response.setProductText(rule.getProductText());

    // Преобразуем RuleQuery в RuleQueryRequest
    List<RuleQueryRequest> queryRequests = rule.getQueries().stream()
        .map(this::convertQueryToRequest)
        .collect(Collectors.toList());

    response.setRule(queryRequests);
    return response;
  }

  private RuleQueryRequest convertQueryToRequest(RuleQuery query) {
    RuleQueryRequest request = new RuleQueryRequest();
    request.setQuery(query.getQuery());
    request.setArguments(query.getArguments());
    request.setNegate(query.isNegate());
    return request;
  }
}