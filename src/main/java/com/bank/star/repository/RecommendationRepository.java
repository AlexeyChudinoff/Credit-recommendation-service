package com.bank.star.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.bank.star.model.ProductType;

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
            WHERE t.user_id = ? AND p.type = ? AND t.type = 'DEPOSIT'
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
   * ИСПРАВЛЕНИЕ: используем 'WITHDRAW' вместо 'WITHDRAWAL'
   */
  public BigDecimal getTotalSpendAmountByProductType(UUID userId, ProductType type) {
    logger.debug("Getting total spends for user {} and product type {}", userId, type);

    String sql = """
            SELECT COALESCE(SUM(t.amount), 0) 
            FROM transactions t 
            JOIN products p ON t.product_id = p.id 
            WHERE t.user_id = ? AND p.type = ? AND t.type = 'WITHDRAW'
            """;

    try {
      BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString(), type.name());
      return result != null ? result : BigDecimal.ZERO;
    } catch (Exception e) {
      logger.error("Error getting spend amount for user {}: {}", userId, e.getMessage());
      return BigDecimal.ZERO;
    }
  }

  /**
   * Получает общее количество транзакций пользователя по типу продукта
   */
  public int getTransactionCountByProductType(UUID userId, ProductType type) {
    logger.debug("Getting transaction count for user {} and product type {}", userId, type);

    String sql = """
            SELECT COUNT(*) 
            FROM transactions t 
            JOIN products p ON t.product_id = p.id 
            WHERE t.user_id = ? AND p.type = ?
            """;

    try {
      Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId.toString(), type.name());
      return result != null ? result : 0;
    } catch (Exception e) {
      logger.error("Error getting transaction count for user {}: {}", userId, e.getMessage());
      return 0;
    }
  }

  /**
   * Получает средний размер транзакции по типу продукта
   */
  public BigDecimal getAverageTransactionAmountByProductType(UUID userId, ProductType type) {
    logger.debug("Getting average transaction amount for user {} and product type {}", userId, type);

    String sql = """
            SELECT COALESCE(AVG(t.amount), 0) 
            FROM transactions t 
            JOIN products p ON t.product_id = p.id 
            WHERE t.user_id = ? AND p.type = ?
            """;

    try {
      BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId.toString(), type.name());
      return result != null ? result : BigDecimal.ZERO;
    } catch (Exception e) {
      logger.error("Error getting average transaction amount for user {}: {}", userId, e.getMessage());
      return BigDecimal.ZERO;
    }
  }

  /**
   * Проверяет существование пользователя
   */
  public boolean userExists(UUID userId) {
    logger.debug("Checking if user {} exists", userId);

    String sql = "SELECT COUNT(*) > 0 FROM users WHERE id = ?";

    try {
      Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, userId.toString());
      return result != null && result;
    } catch (Exception e) {
      logger.error("Error checking if user exists {}: {}", userId, e.getMessage());
      return false;
    }
  }
}