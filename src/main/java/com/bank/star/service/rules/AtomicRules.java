// Атомарные правила (базовые проверки): наличие продукта, суммы операций и другие базовые условия
package com.bank.star.service.rules;

import com.bank.star.model.ProductType;
import com.bank.star.repository.RecommendationRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Компонент, содержащий атомарные (базовые) правила для рекомендательной системы.
 * Атомарные правила представляют собой неделимые проверки, которые могут комбинироваться
 * для создания более сложных условий рекомендаций.
 */
@Component
public class AtomicRules {

  private final RecommendationRepository repository;

  /**
   * Конструктор с внедрением зависимости репозитория.
   *
   * @param repository репозиторий для доступа к данным пользователей и продуктов
   */
  public AtomicRules(RecommendationRepository repository) {
    this.repository = repository;
  }

  /**
   * Атомарное правило: проверяет, есть ли у пользователя продукт указанного типа.
   *
   * @param type тип продукта для проверки
   * @return правило RecommendationRule, проверяющее наличие продукта
   */
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

  /**
   * Атомарное правило: проверяет отсутствие у пользователя продукта указанного типа.
   *
   * @param type тип продукта для проверки
   * @return правило RecommendationRule, проверяющее отсутствие продукта
   */
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

  /**
   * Атомарное правило: проверяет, что сумма пополнений по продукту типа type
   * больше заданного порога threshold.
   *
   * @param type      тип продукта
   * @param threshold пороговая сумма (исключительно)
   * @return правило RecommendationRule для проверки суммы пополнений
   */
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

  /**
   * Атомарное правило: проверяет, что сумма пополнений по продукту типа type
   * больше или равна заданному порогу threshold.
   *
   * @param type      тип продукта
   * @param threshold пороговая сумма (включительно)
   * @return правило RecommendationRule для проверки суммы пополнений
   */
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

  /**
   * Атомарное правило: проверяет, что сумма трат по продукту типа type
   * больше заданного порога threshold.
   *
   * @param type      тип продукта
   * @param threshold пороговая сумма (исключительно)
   * @return правило RecommendationRule для проверки суммы трат
   */
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

  /**
   * Атомарное правило: проверяет, что баланс по продукту типа type положительный
   * (сумма пополнений больше суммы трат).
   *
   * @param type тип продукта
   * @return правило RecommendationRule для проверки положительного баланса
   */
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