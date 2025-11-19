//атомарные правила (базовые проверки)
package com.bank.star.service.rules;

import com.bank.star.model.ProductType;
import com.bank.star.repository.RecommendationRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AtomicRules {

  private final RecommendationRepository repository;

  public AtomicRules(RecommendationRepository repository) {
    this.repository = repository;
  }

  // Атомарное правило: наличие продукта определенного типа
  public RecommendationRule hasProductType(ProductType type) {
    return new RecommendationRule() {
      @Override
      public boolean isEligible(UUID userId) {
        return repository.userHasProductType(userId, type);
      }

      @Override
      public String getRuleName() {
        return "HAS_PRODUCT_" + type.name();
      }
    };
  }

  // Атомарное правило: отсутствие продукта определенного типа
  public RecommendationRule hasNoProductType(ProductType type) {
    return new RecommendationRule() {
      @Override
      public boolean isEligible(UUID userId) {
        return !repository.userHasProductType(userId, type);
      }

      @Override
      public String getRuleName() {
        return "NO_PRODUCT_" + type.name();
      }
    };
  }

  // Атомарное правило: сумма пополнений по типу продукта больше порога
  public RecommendationRule depositGreaterThan(ProductType type, BigDecimal threshold) {
    return new RecommendationRule() {
      @Override
      public boolean isEligible(UUID userId) {
        BigDecimal deposit = repository.getTotalDepositAmountByProductType(userId, type);
        return deposit != null && deposit.compareTo(threshold) > 0;
      }

      @Override
      public String getRuleName() {
        return "DEPOSIT_" + type.name() + "_GT_" + threshold;
      }
    };
  }

  // Атомарное правило: сумма пополнений по типу продукта больше или равна порогу
  public RecommendationRule depositGreaterOrEqual(ProductType type, BigDecimal threshold) {
    return new RecommendationRule() {
      @Override
      public boolean isEligible(UUID userId) {
        BigDecimal deposit = repository.getTotalDepositAmountByProductType(userId, type);
        return deposit != null && deposit.compareTo(threshold) >= 0;
      }

      @Override
      public String getRuleName() {
        return "DEPOSIT_" + type.name() + "_GTE_" + threshold;
      }
    };
  }

  // Атомарное правило: сумма трат по типу продукта больше порога
  public RecommendationRule spendGreaterThan(ProductType type, BigDecimal threshold) {
    return new RecommendationRule() {
      @Override
      public boolean isEligible(UUID userId) {
        BigDecimal spend = repository.getTotalSpendAmountByProductType(userId, type);
        return spend != null && spend.compareTo(threshold) > 0;
      }

      @Override
      public String getRuleName() {
        return "SPEND_" + type.name() + "_GT_" + threshold;
      }
    };
  }

  // Атомарное правило: положительный баланс по типу продукта
  public RecommendationRule positiveBalance(ProductType type) {
    return new RecommendationRule() {
      @Override
      public boolean isEligible(UUID userId) {
        BigDecimal deposit = repository.getTotalDepositAmountByProductType(userId, type);
        BigDecimal spend = repository.getTotalSpendAmountByProductType(userId, type);
        return deposit != null && spend != null && deposit.compareTo(spend) > 0;
      }

      @Override
      public String getRuleName() {
        return "POSITIVE_BALANCE_" + type.name();
      }
    };
  }
}