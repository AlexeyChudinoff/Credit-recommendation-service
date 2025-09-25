package com.bank.star.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.model.ProductType;
import com.bank.star.repository.RecommendationRepository;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RecommendationService {

  private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

  private final RecommendationRepository repository;

  // Предопределенные продукты для рекомендаций
  private final Map<String, ProductRecommendation> products = Map.of(
      "Invest 500", new ProductRecommendation(
          "Invest 500",
          UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"),
          "Откройте свой путь к успеху с индивидуальным инвестиционным счетом..."
      ),
      "Top Saving", new ProductRecommendation(
          "Top Saving",
          UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925"),
          "Откройте свою собственную «Копилку» с нашим банком!"
      ),
      "Простой кредит", new ProductRecommendation(
          "Простой кредит",
          UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f"),
          "Откройте мир выгодных кредитов с нами!"
      )
  );

  @Autowired
  public RecommendationService(RecommendationRepository repository) {
    this.repository = repository;
  }

  public RecommendationResponse getRecommendations(UUID userId) {
    logger.info("Getting recommendations for user: {}", userId);
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    List<ProductRecommendation> recommendations = new ArrayList<>();

    // Проверяем каждое правило рекомендаций
    if (isEligibleForInvest500(userId)) {
      recommendations.add(products.get("Invest 500"));
      logger.debug("User {} is eligible for Invest 500", userId);
    }

    if (isEligibleForTopSaving(userId)) {
      recommendations.add(products.get("Top Saving"));
      logger.debug("User {} is eligible for Top Saving", userId);
    }

    if (isEligibleForSimpleCredit(userId)) {
      recommendations.add(products.get("Простой кредит"));
      logger.debug("User {} is eligible for Simple Credit", userId);
    }

    logger.info("Found {} recommendations for user {}", recommendations.size(), userId);
    return new RecommendationResponse(userId, recommendations);
  }

  private boolean isEligibleForInvest500(UUID userId) {
    // Правило 1: Наличие DEBIT продукта
    boolean hasDebit = repository.userHasProductType(userId, ProductType.DEBIT);
    // Правило 2: Отсутствие INVEST продуктов
    boolean noInvest = !repository.userHasProductType(userId, ProductType.INVEST);
    // Правило 3: Сумма пополнений SAVING > 1000
    BigDecimal savingDeposit = repository.getTotalDepositAmountByProductType(userId, ProductType.SAVING);
    boolean savingCondition = savingDeposit.compareTo(new BigDecimal("1000")) > 0;

    return hasDebit && noInvest && savingCondition;
  }

  private boolean isEligibleForTopSaving(UUID userId) {
    // Правило 1: Наличие DEBIT продукта
    boolean hasDebit = repository.userHasProductType(userId, ProductType.DEBIT);

    // Правило 2: (Сумма пополнений DEBIT >= 50k) ИЛИ (Сумма пополнений SAVING >= 50k)
    BigDecimal debitDeposit = repository.getTotalDepositAmountByProductType(userId, ProductType.DEBIT);
    BigDecimal savingDeposit = repository.getTotalDepositAmountByProductType(userId, ProductType.SAVING);
    boolean depositCondition = debitDeposit.compareTo(new BigDecimal("50000")) >= 0 ||
        savingDeposit.compareTo(new BigDecimal("50000")) >= 0;

    // Правило 3: Сумма пополнений DEBIT > суммы трат DEBIT
    BigDecimal debitSpend = repository.getTotalSpendAmountByProductType(userId, ProductType.DEBIT);
    boolean balanceCondition = debitDeposit.compareTo(debitSpend) > 0;

    return hasDebit && depositCondition && balanceCondition;
  }

  private boolean isEligibleForSimpleCredit(UUID userId) {
    // Правило 1: Отсутствие CREDIT продуктов
    boolean noCredit = !repository.userHasProductType(userId, ProductType.CREDIT);

    // Правило 2: Сумма пополнений DEBIT > суммы трат DEBIT
    BigDecimal debitDeposit = repository.getTotalDepositAmountByProductType(userId, ProductType.DEBIT);
    BigDecimal debitSpend = repository.getTotalSpendAmountByProductType(userId, ProductType.DEBIT);
    boolean balanceCondition = debitDeposit.compareTo(debitSpend) > 0;

    // Правило 3: Сумма трат DEBIT > 100,000
    boolean spendCondition = debitSpend.compareTo(new BigDecimal("100000")) > 0;

    return noCredit && balanceCondition && spendCondition;
  }
}