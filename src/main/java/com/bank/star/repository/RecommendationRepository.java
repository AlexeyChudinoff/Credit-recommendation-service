package com.bank.star.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public class RecommendationRepository {

  private static final Logger logger = LoggerFactory.getLogger(RecommendationRepository.class);

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public RecommendationRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Проверяет, есть ли у пользователя продукты указанного типа
   */
  public boolean userHasProductType(UUID userId, ProductType type) {
    logger.debug("Checking if user {} has product type {}", userId, type);

    String sql = """
            SELECT COUNT(*) > 0 
            FROM transactions t 
            JOIN products p ON t.product_id = p.id 
            WHERE t.user_id = ? AND p.type = ?
            """;

    try {
      Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, userId.toString(), type.name());
      return result != null && result;
    } catch (Exception e) {
      logger.error("Error checking product type for user {}: {}", userId, e.getMessage());
      return false;
    }
  }

  /**
   * Возвращает сумму пополнений по указанному типу продукта
   */
  public BigDecimal getTotalDepositAmountByProductType(UUID userId, ProductType type) {
    logger.debug("Getting total deposits for user {} and product type {}", userId, type);

    String sql = """
            SELECT COALESCE(SUM(t.amount), 0) 
            FROM transactions t 
            JOIN products p ON t.product_id = p.id 
            WHERE t.user_id = ? AND p.type = ? AND t.transaction_type = 'DEPOSIT'
            """;

    try {
      BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString(), type.name());
      return result != null ? result : BigDecimal.ZERO;
    } catch (Exception e) {
      logger.error("Error getting deposit amount for user {}: {}", userId, e.getMessage());
      return BigDecimal.ZERO;
    }
  }

  /**
   * Возвращает сумму трат по указанному типу продукта
   */
  public BigDecimal getTotalSpendAmountByProductType(UUID userId, ProductType type) {
    logger.debug("Getting total spends for user {} and product type {}", userId, type);

    String sql = """
            SELECT COALESCE(SUM(t.amount), 0) 
            FROM transactions t 
            JOIN products p ON t.product_id = p.id 
            WHERE t.user_id = ? AND p.type = ? AND t.transaction_type = 'WITHDRAWAL'
            """;

    try {
      BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString(), type.name());
      return result != null ? result : BigDecimal.ZERO;
    } catch (Exception e) {
      logger.error("Error getting spend amount for user {}: {}", userId, e.getMessage());
      return BigDecimal.ZERO;
    }
  }
}