package com.bank.star.repository;

import com.bank.star.model.DynamicRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DynamicRuleRepository extends JpaRepository<DynamicRule, UUID> {

  /**
   * Найти правило по ID продукта
   */
  Optional<DynamicRule> findByProductId(UUID productId);

  /**
   * Найти все правила по названию продукта (поиск без учета регистра)
   */
  List<DynamicRule> findByProductNameContainingIgnoreCase(String productName);

  /**
   * Проверить существование правила по ID продукта
   */
  boolean existsByProductId(UUID productId);

  /**
   * Получить все активные правила (все правила считаются активными)
   * Можно добавить поле 'active' в будущем
   */
  @Query("SELECT dr FROM DynamicRule dr ORDER BY dr.productName")
  List<DynamicRule> findAllActiveRules();

  /**
   * Удалить правило по ID продукта
   */
  void deleteByProductId(UUID productId);

  /**
   * Найти правила, содержащие определенный тип запроса
   */
  @Query("SELECT DISTINCT dr FROM DynamicRule dr JOIN dr.queries q WHERE q.query = :queryType")
  List<DynamicRule> findByQueryType(com.bank.star.model.QueryType queryType);

  /**
   * Получить количество всех правил
   */
  @Query("SELECT COUNT(dr) FROM DynamicRule dr")
  long countAllRules();
}