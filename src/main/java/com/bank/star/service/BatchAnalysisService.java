// Массовый анализ всей клиентской базы для поиска целевой аудитории
//Бизнес-ценность: Позволяет отделу маркетинга получать готовые списки для рассылок
package com.bank.star.service;

import com.bank.star.dto.UserRecommendation;
import com.bank.star.service.rules.ProductRuleSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для пакетного анализа всей клиентской базы
 * Находит всех пользователей, подходящих под условия конкретных продуктов
 * Используется для массовых маркетинговых кампаний и аналитики
 */
@Service
public class BatchAnalysisService {

  private static final Logger logger = LoggerFactory.getLogger(BatchAnalysisService.class);

  private final ProductRuleSets productRuleSets;
  private final com.bank.star.repository.RecommendationRepository repository;
  private final UserNameResolver userNameResolver;

  public BatchAnalysisService(ProductRuleSets productRuleSets,
      com.bank.star.repository.RecommendationRepository repository,
      UserNameResolver userNameResolver) {
    this.productRuleSets = productRuleSets;
    this.repository = repository;
    this.userNameResolver = userNameResolver;
  }

  /**
   * Находит всех пользователей, подходящих для указанного продукта
   * @param productName название продукта ("Invest 500", "Top Saving", "Простой кредит")
   * @return список подходящих пользователей
   */
  public List<UserRecommendation> getUsersForProduct(String productName) {
    logger.info("🔍 Starting batch analysis for product: {}", productName);

    List<UUID> allUserIds = repository.getAllActiveUserIds();
    List<UserRecommendation> eligibleUsers = new ArrayList<>();

    for (UUID userId : allUserIds) {
      try {
        boolean isEligible = switch (productName) {
          case "Invest 500" -> productRuleSets.getInvest500RuleSet().isEligible(userId);
          case "Top Saving" -> productRuleSets.getTopSavingRuleSet().isEligible(userId);
          case "Простой кредит" -> productRuleSets.getSimpleCreditRuleSet().isEligible(userId);
          default -> false;
        };

        if (isEligible) {
          String fullName = userNameResolver.getUserFullName(userId);
          eligibleUsers.add(new UserRecommendation(userId, fullName));
          logger.debug("✅ User {} eligible for {}", fullName, productName);
        }
      } catch (Exception e) {
        logger.warn("Error processing user {}: {}", userId, e.getMessage());
      }
    }

    logger.info("✅ Found {} eligible users for {}", eligibleUsers.size(), productName);
    return eligibleUsers;
  }

  /**
   * Находит всех пользователей, которые не подходят ни под один из продуктов
   * @return список пользователей без рекомендаций
   */
  public List<UserRecommendation> getUsersWithoutRecommendations() {
    logger.info("🔍 Finding users without any recommendations");

    List<UUID> allUserIds = repository.getAllActiveUserIds();
    List<UserRecommendation> usersWithoutRecommendations = new ArrayList<>();

    for (UUID userId : allUserIds) {
      try {
        boolean hasAnyRecommendation =
            productRuleSets.getInvest500RuleSet().isEligible(userId) ||
                productRuleSets.getTopSavingRuleSet().isEligible(userId) ||
                productRuleSets.getSimpleCreditRuleSet().isEligible(userId);

        if (!hasAnyRecommendation) {
          String fullName = userNameResolver.getUserFullName(userId);
          usersWithoutRecommendations.add(new UserRecommendation(userId, fullName));
          logger.debug("❌ User {} has no recommendations", fullName);
        }
      } catch (Exception e) {
        logger.warn("Error processing user {}: {}", userId, e.getMessage());
      }
    }

    logger.info("✅ Found {} users without recommendations", usersWithoutRecommendations.size());
    return usersWithoutRecommendations;
  }
}