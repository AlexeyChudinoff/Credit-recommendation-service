/**
 * Класс композитных правил, реализующий логические операции AND/OR над несколькими правилами.
 * Позволяет создавать сложные условия из атомарных или других композитных правил.
 */
package com.bank.star.service.rules;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CompositeRule implements RecommendationRule {

  private final List<RecommendationRule> rules;  // Дочерние правила
  private final String ruleName;                 // Имя композитного правила
  private final Operator operator;               // Логический оператор (AND/OR)

  /**
   * Перечисление логических операторов для композитных правил.
   */
  public enum Operator {
    AND,  // Все условия должны быть истинны
    OR    // Хотя бы одно условие должно быть истинно
  }

  /**
   * Конструктор композитного правила.
   *
   * @param ruleName   имя правила для идентификации
   * @param operator   логический оператор (AND/OR)
   * @param rules      массив дочерних правил
   */
  public CompositeRule(String ruleName, Operator operator, RecommendationRule... rules) {
    this.ruleName = ruleName;
    this.operator = operator;
    this.rules = Arrays.asList(rules);
  }

  /**
   * Проверяет соответствие пользователя композитному правилу.
   * В зависимости от оператора применяет логику "все" (AND) или "хотя бы одно" (OR).
   *
   * @param userId уникальный идентификатор пользователя
   * @return true, если пользователь соответствует условиям композитного правила
   */
  @Override
  public boolean isEligible(UUID userId) {
    if (operator == Operator.AND) {
      // Для AND: все правила должны вернуть true
      return rules.stream().allMatch(rule -> rule.isEligible(userId));
    } else { // OR
      // Для OR: хотя бы одно правило должно вернуть true
      return rules.stream().anyMatch(rule -> rule.isEligible(userId));
    }
  }

  /**
   * Возвращает имя композитного правила.
   *
   * @return имя правила
   */
  @Override
  public String getRuleName() {
    return ruleName;
  }

  /**
   * Возвращает список дочерних правил.
   *
   * @return неизменяемый список дочерних правил
   */
  public List<RecommendationRule> getChildRules() {
    return rules;
  }

  /**
   * Создает композитное правило с оператором AND.
   *
   * @param name   имя правила
   * @param rules  дочерние правила
   * @return новое композитное правило с оператором AND
   */
  public static CompositeRule and(String name, RecommendationRule... rules) {
    return new CompositeRule(name, Operator.AND, rules);
  }

  /**
   * Создает композитное правило с оператором OR.
   *
   * @param name   имя правила
   * @param rules  дочерние правила
   * @return новое композитное правило с оператором OR
   */
  public static CompositeRule or(String name, RecommendationRule... rules) {
    return new CompositeRule(name, Operator.OR, rules);
  }
}