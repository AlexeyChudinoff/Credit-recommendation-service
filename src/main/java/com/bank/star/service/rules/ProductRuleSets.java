//наборы правил для конкретных продуктов
package com.bank.star.service.rules;

import com.bank.star.model.ProductType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductRuleSets {

  private final AtomicRules atomicRules;

  // Константы для порогов
  private static final BigDecimal SAVING_THRESHOLD_1K = new BigDecimal("1000");
  private static final BigDecimal DEPOSIT_THRESHOLD_50K = new BigDecimal("50000");
  private static final BigDecimal SPEND_THRESHOLD_100K = new BigDecimal("100000");

  public ProductRuleSets(AtomicRules atomicRules) {
    this.atomicRules = atomicRules;
  }

  public RecommendationRule getInvest500RuleSet() {
    return CompositeRule.and("INVEST_500_RULES",
        atomicRules.hasProductType(ProductType.DEBIT),
        atomicRules.hasNoProductType(ProductType.INVEST),
        atomicRules.depositGreaterThan(ProductType.SAVING, SAVING_THRESHOLD_1K)
    );
  }

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

  public RecommendationRule getSimpleCreditRuleSet() {
    return CompositeRule.and("SIMPLE_CREDIT_RULES",
        atomicRules.hasNoProductType(ProductType.CREDIT),
        atomicRules.positiveBalance(ProductType.DEBIT),
        atomicRules.spendGreaterThan(ProductType.DEBIT, SPEND_THRESHOLD_100K)
    );
  }
}