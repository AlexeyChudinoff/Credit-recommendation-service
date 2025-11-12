package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.exception.UserNotFoundException;
import com.bank.star.service.rules.ProductRuleSets;
import com.bank.star.service.rules.RecommendationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // Проверка существования пользователя
    if (!repository.userExists(userId)) {
      throw new UserNotFoundException("User not found: " + userId);
    }

    List<ProductRecommendation> recommendations = new ArrayList<>();
    Map<String, Boolean> eligibilityAnalysis = new HashMap<>();

    // Проверяем правила для каждого продукта
    checkProductEligibility("Invest 500", productRuleSets.getInvest500RuleSet(),
        userId, recommendations, eligibilityAnalysis);
    checkProductEligibility("Top Saving", productRuleSets.getTopSavingRuleSet(),
        userId, recommendations, eligibilityAnalysis);
    checkProductEligibility("Простой кредит", productRuleSets.getSimpleCreditRuleSet(),
        userId, recommendations, eligibilityAnalysis);

    logger.info("✅ Found {} recommendations for user {}", recommendations.size(), userId);
    logger.debug("Eligibility analysis for user {}: {}", userId, eligibilityAnalysis);

    return new RecommendationResponse(userId, recommendations);
  }

  private void checkProductEligibility(String productName, RecommendationRule rule,
      UUID userId, List<ProductRecommendation> recommendations,
      Map<String, Boolean> eligibilityAnalysis) {
    boolean isEligible = rule.isEligible(userId);
    eligibilityAnalysis.put(productName, isEligible);

    if (isEligible) {
      recommendations.add(products.get(productName));
      logger.debug("User {} is eligible for {}", userId, productName);
    }
  }
}