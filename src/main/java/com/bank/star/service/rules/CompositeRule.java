//композитные правила (AND/OR логика)
package com.bank.star.service.rules;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CompositeRule implements RecommendationRule {

  private final List<RecommendationRule> rules;
  private final String ruleName;
  private final Operator operator;

  public enum Operator {
    AND, OR
  }

  public CompositeRule(String ruleName, Operator operator, RecommendationRule... rules) {
    this.ruleName = ruleName;
    this.operator = operator;
    this.rules = Arrays.asList(rules);
  }

  @Override
  public boolean isEligible(UUID userId) {
    if (operator == Operator.AND) {
      return rules.stream().allMatch(rule -> rule.isEligible(userId));
    } else { // OR
      return rules.stream().anyMatch(rule -> rule.isEligible(userId));
    }
  }

  @Override
  public String getRuleName() {
    return ruleName;
  }

  public List<RecommendationRule> getChildRules() {
    return rules;
  }

  // Вспомогательные методы для создания композитных правил
  public static CompositeRule and(String name, RecommendationRule... rules) {
    return new CompositeRule(name, Operator.AND, rules);
  }

  public static CompositeRule or(String name, RecommendationRule... rules) {
    return new CompositeRule(name, Operator.OR, rules);
  }
}