/**
 * Компонент, содержащий наборы правил для конкретных банковских продуктов.
 * Определяет бизнес-логику рекомендаций в виде композитных правил,
 * объединяющих атомарные проверки.
 */
package com.bank.star.service.rules;

import com.bank.star.model.ProductType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductRuleSets {

  private final AtomicRules atomicRules;  // Сервис атомарных правил

  // Константы для пороговых значений суммы
  private static final BigDecimal SAVING_THRESHOLD_1K = new BigDecimal("1000");
  private static final BigDecimal DEPOSIT_THRESHOLD_50K = new BigDecimal("50000");
  private static final BigDecimal SPEND_THRESHOLD_100K = new BigDecimal("100000");

  /**
   * Конструктор с внедрением зависимости сервиса атомарных правил.
   *
   * @param atomicRules сервис атомарных правил
   */
  public ProductRuleSets(AtomicRules atomicRules) {
    this.atomicRules = atomicRules;
  }

  /**
   * Возвращает набор правил для продукта "Invest 500".
   * Условия:
   * 1. Пользователь имеет дебетовый продукт (DEBIT)
   * 2. Пользователь НЕ имеет инвестиционного продукта (INVEST)
   * 3. Сумма пополнений сберегательного продукта (SAVING) > 1,000
   *
   * @return композитное правило для рекомендации "Invest 500"
   */
  public RecommendationRule getInvest500RuleSet() {
    return CompositeRule.and("INVEST_500_RULES",
        atomicRules.hasProductType(ProductType.DEBIT),
        atomicRules.hasNoProductType(ProductType.INVEST),
        atomicRules.depositGreaterThan(ProductType.SAVING, SAVING_THRESHOLD_1K)
    );
  }

  /**
   * Возвращает набор правил для продукта "Top Saving".
   * Условия:
   * 1. Пользователь имеет дебетовый продукт (DEBIT)
   * 2. Сумма пополнений по DEBIT или SAVING >= 50,000 (оператор OR)
   * 3. Положительный баланс по DEBIT (пополнения > траты)
   *
   * @return композитное правило для рекомендации "Top Saving"
   */
  public RecommendationRule getTopSavingRuleSet() {
    return CompositeRule.and("TOP_SAVING_RULES",
        atomicRules.hasProductType(ProductType.DEBIT),
        CompositeRule.or("HIGH_DEPOSITS",
            atomicRules.depositGreaterOrEqual(ProductType.DEBIT, DEPOSIT_THRESHOLD_50K),
            atomicRules.depositGreaterOrEqual(ProductType.SAVING, DEPOSIT_THRESHOLD_50K)
        ),
        atomicRules.positiveBalance(ProductType.DEBIT)
    );
  }

  /**
   * Возвращает набор правил для продукта "Простой кредит".
   * Условия:
   * 1. Пользователь НЕ имеет кредитного продукта (CREDIT)
   * 2. Положительный баланс по DEBIT (пополнения > траты)
   * 3. Сумма трат по DEBIT > 100,000
   *
   * @return композитное правило для рекомендации "Простой кредит"
   */
  public RecommendationRule getSimpleCreditRuleSet() {
    return CompositeRule.and("SIMPLE_CREDIT_RULES",
        atomicRules.hasNoProductType(ProductType.CREDIT),
        atomicRules.positiveBalance(ProductType.DEBIT),
        atomicRules.spendGreaterThan(ProductType.DEBIT, SPEND_THRESHOLD_100K)
    );
  }
}