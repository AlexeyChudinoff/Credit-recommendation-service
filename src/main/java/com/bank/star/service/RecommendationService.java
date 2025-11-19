//основной сервис рекомендаций
package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.exception.UserNotFoundException;
import com.bank.star.model.ProductType;
import com.bank.star.service.rules.ProductRuleSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RecommendationService {

  private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

  private final ProductRuleSets productRuleSets;
  private final com.bank.star.repository.RecommendationRepository repository;

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
  public RecommendationService(ProductRuleSets productRuleSets,
      com.bank.star.repository.RecommendationRepository repository) {
    this.productRuleSets = productRuleSets;
    this.repository = repository;
  }

  public RecommendationResponse getRecommendations(UUID userId) {
    logger.info("🔄 Getting recommendations for user: {}", userId);

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    if (!repository.userExists(userId)) {
      throw new UserNotFoundException("User not found: " + userId);
    }

    // ДИАГНОСТИКА ТИПОВ ТРАНЗАКЦИЙ
    repository.diagnoseTransactionTypes(userId);

    List<ProductRecommendation> recommendations = new ArrayList<>();

    // ДЕТАЛЬНАЯ ДИАГНОСТИКА ПРАВИЛ
    logger.info("🔍 DETAILED DIAGNOSTICS for user {}:", userId);

    // Проверяем каждое условие отдельно для Simple Credit
    boolean noCredit = repository.userHasProductType(userId, ProductType.CREDIT);
    BigDecimal debitDeposits = repository.getTotalDepositAmountByProductType(userId, ProductType.DEBIT);
    BigDecimal debitSpend = repository.getTotalSpendAmountByProductType(userId, ProductType.DEBIT);

    logger.info("🔍 SimpleCredit conditions:");
    logger.info("🔍   - No CREDIT products: {}", !noCredit);
    logger.info("🔍   - DEBIT deposits: {}, DEBIT spend: {}", debitDeposits, debitSpend);
    logger.info("🔍   - Deposits > Spend: {}", debitDeposits != null && debitSpend != null && debitDeposits.compareTo(debitSpend) > 0);
    logger.info("🔍   - Spend > 100K: {}", debitSpend != null && debitSpend.compareTo(new BigDecimal("100000")) > 0);

    // Проверяем eligibility
    boolean simpleCreditEligible = productRuleSets.getSimpleCreditRuleSet().isEligible(userId);
    boolean topSavingEligible = productRuleSets.getTopSavingRuleSet().isEligible(userId);
    boolean invest500Eligible = productRuleSets.getInvest500RuleSet().isEligible(userId);

    logger.info("🔍 Final eligibility - SimpleCredit: {}, TopSaving: {}, Invest500: {}",
        simpleCreditEligible, topSavingEligible, invest500Eligible);

    // Добавляем все подходящие продукты
    if (simpleCreditEligible) {
      recommendations.add(products.get("Простой кредит"));
      logger.info("🔍 ADDED Простой кредит");
    }

    if (topSavingEligible) {
      recommendations.add(products.get("Top Saving"));
      logger.info("🔍 ADDED Top Saving");
    }

    if (invest500Eligible) {
      recommendations.add(products.get("Invest 500"));
      logger.info("🔍 ADDED Invest 500");
    }

    logger.info("✅ Found {} recommendations for user {}", recommendations.size(), userId);
    return new RecommendationResponse(userId, recommendations);
  }
}