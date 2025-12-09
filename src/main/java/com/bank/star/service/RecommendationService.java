/**
 * Основной сервис рекомендаций, который проверяет соответствие пользователя
 * различным наборам правил и формирует список рекомендуемых продуктов.
 */
package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.exception.UserNotFoundException;
import com.bank.star.model.ProductType;
import com.bank.star.model.RuleStatistics;
import com.bank.star.repository.DynamicRuleRepository;
import com.bank.star.repository.RuleStatisticsRepository;
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

  private final ProductRuleSets productRuleSets;              // Наборы правил для продуктов
  private final com.bank.star.repository.RecommendationRepository repository;  // Репозиторий данных пользователя
  private final DynamicRuleRepository dynamicRuleRepository;  // Репозиторий динамических правил
  private final RuleStatisticsRepository statisticsRepository; // Репозиторий статистики выполнения правил

  // Предопределенные продукты для рекомендаций с их описанием и ID
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

  /**
   * Конструктор с внедрением зависимостей.
   *
   * @param productRuleSets        наборы правил для продуктов
   * @param repository             репозиторий данных пользователя
   * @param dynamicRuleRepository  репозиторий динамических правил
   * @param statisticsRepository   репозиторий статистики выполнения правил
   */
  @Autowired
  public RecommendationService(ProductRuleSets productRuleSets,
      com.bank.star.repository.RecommendationRepository repository,
      DynamicRuleRepository dynamicRuleRepository,
      RuleStatisticsRepository statisticsRepository) {
    this.productRuleSets = productRuleSets;
    this.repository = repository;
    this.dynamicRuleRepository = dynamicRuleRepository;
    this.statisticsRepository = statisticsRepository;
  }

  /**
   * Получает рекомендации продуктов для указанного пользователя.
   *
   * @param userId уникальный идентификатор пользователя
   * @return объект RecommendationResponse с найденными рекомендациями
   * @throws IllegalArgumentException если userId равен null
   * @throws UserNotFoundException если пользователь не найден в системе
   */
  public RecommendationResponse getRecommendations(UUID userId) {
    logger.info("🔄 Getting recommendations for user: {}", userId);

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    if (!repository.userExists(userId)) {
      throw new UserNotFoundException("User not found: " + userId);
    }

    // Диагностика типов транзакций для отладки
    repository.diagnoseTransactionTypes(userId);

    List<ProductRecommendation> recommendations = new ArrayList<>();

    // Детальная диагностика правил для отладки
    logger.info("🔍 DETAILED DIAGNOSTICS for user {}:", userId);

    // Проверяем каждое условие отдельно для Simple Credit (для диагностики)
    boolean noCredit = repository.userHasProductType(userId, ProductType.CREDIT);
    BigDecimal debitDeposits = repository.getTotalDepositAmountByProductType(userId, ProductType.DEBIT);
    BigDecimal debitSpend = repository.getTotalSpendAmountByProductType(userId, ProductType.DEBIT);

    logger.info("🔍 SimpleCredit conditions:");
    logger.info("🔍   - No CREDIT products: {}", !noCredit);
    logger.info("🔍   - DEBIT deposits: {}, DEBIT spend: {}", debitDeposits, debitSpend);
    logger.info("🔍   - Deposits > Spend: {}", debitDeposits != null && debitSpend != null && debitDeposits.compareTo(debitSpend) > 0);
    logger.info("🔍   - Spend > 100K: {}", debitSpend != null && debitSpend.compareTo(new BigDecimal("100000")) > 0);

    // Проверяем eligibility для каждого набора правил
    boolean simpleCreditEligible = productRuleSets.getSimpleCreditRuleSet().isEligible(userId);
    boolean topSavingEligible = productRuleSets.getTopSavingRuleSet().isEligible(userId);
    boolean invest500Eligible = productRuleSets.getInvest500RuleSet().isEligible(userId);

    logger.info("🔍 Final eligibility - SimpleCredit: {}, TopSaving: {}, Invest500: {}",
        simpleCreditEligible, topSavingEligible, invest500Eligible);

    // Добавляем все подходящие продукты в список рекомендаций
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

    // После формирования рекомендаций обновляем статистику выполнения правил
    updateRuleStatistics(recommendations);

    logger.info("✅ Found {} recommendations for user {}", recommendations.size(), userId);
    return new RecommendationResponse(userId, recommendations);
  }

  /**
   * Обновляет статистику выполнения правил на основе предоставленных рекомендаций.
   * Для каждого рекомендованного продукта находит соответствующее правило и увеличивает счетчик.
   *
   * @param recommendations список рекомендованных продуктов
   */
  private void updateRuleStatistics(List<ProductRecommendation> recommendations) {
    for (ProductRecommendation recommendation : recommendations) {
      dynamicRuleRepository.findByProductId(recommendation.getId())
          .ifPresent(rule -> {
            // Ищем существующую статистику или создаем новую
            RuleStatistics statistics = statisticsRepository.findByRuleId(rule.getId())
                .orElseGet(() -> new RuleStatistics(rule.getId()));

            // Увеличиваем счетчик выполнения
            statistics.incrementCount();
            statisticsRepository.save(statistics);

            logger.debug("Updated statistics for rule {}: {}", rule.getId(),
                statistics.getExecutionCount());
          });
    }
  }

  /**
   * Очищает кеш статистики, удаляя все записи из репозитория.
   * Используется для сброса накопленной статистики.
   */
  public void clearCaches() {
    statisticsRepository.deleteAll();
    logger.info("🧹 Statistics cleared from database");
  }
}