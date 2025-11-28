// Массовый анализ всей клиентской базы для поиска целевой аудитории
// Бизнес-ценность: Позволяет отделу маркетинга получать готовые списки для рассылок
package com.bank.star.service;

import com.bank.star.dto.UserRecommendation;
import com.bank.star.service.rules.ProductRuleSets;
import com.bank.star.repository.RecommendationRepository;
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
  private final RecommendationRepository repository;
  private final UserNameResolver userNameResolver;

  // Константы для названий продуктов
  private static final String INVEST_500 = "Invest 500";
  private static final String TOP_SAVING = "Top Saving";
  private static final String SIMPLE_CREDIT = "Простой кредит";

  public BatchAnalysisService(ProductRuleSets productRuleSets,
      RecommendationRepository repository,
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
        boolean isEligible = isUserEligibleForProduct(userId, productName);

        if (isEligible) {
          String fullName = userNameResolver.getUserFullName(userId);
          eligibleUsers.add(new UserRecommendation(userId, fullName));
          logger.debug("✅ User {} eligible for {}", fullName, productName);
        }
      } catch (Exception e) {
        logger.warn("Error processing user {}: {}", userId, e.getMessage(), e);
      }
    }

    logger.info("✅ Found {} eligible users for {}", eligibleUsers.size(), productName);
    return eligibleUsers;
  }

  /**
   * Проверяет, подходит ли пользователь для указанного продукта
   * @param userId идентификатор пользователя
   * @param productName название продукта
   * @return true если пользователь подходит для продукта
   */
  private boolean isUserEligibleForProduct(UUID userId, String productName) {
    return switch (productName) {
      case INVEST_500 -> productRuleSets.getInvest500RuleSet().isEligible(userId);
      case TOP_SAVING -> productRuleSets.getTopSavingRuleSet().isEligible(userId);
      case SIMPLE_CREDIT -> productRuleSets.getSimpleCreditRuleSet().isEligible(userId);
      default -> {
        logger.warn("Unknown product name: {}", productName);
        yield false;
      }
    };
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
        if (!hasAnyRecommendation(userId)) {
          String fullName = userNameResolver.getUserFullName(userId);
          usersWithoutRecommendations.add(new UserRecommendation(userId, fullName));
          logger.debug("❌ User {} has no recommendations", fullName);
        }
      } catch (Exception e) {
        logger.warn("Error processing user {}: {}", userId, e.getMessage(), e);
      }
    }

    logger.info("✅ Found {} users without recommendations", usersWithoutRecommendations.size());
    return usersWithoutRecommendations;
  }

  /**
   * Проверяет, есть ли у пользователя рекомендации для любого из продуктов
   * @param userId идентификатор пользователя
   * @return true если есть хотя бы одна рекомендация
   */
  private boolean hasAnyRecommendation(UUID userId) {
    return productRuleSets.getInvest500RuleSet().isEligible(userId) ||
        productRuleSets.getTopSavingRuleSet().isEligible(userId) ||
        productRuleSets.getSimpleCreditRuleSet().isEligible(userId);
  }
}