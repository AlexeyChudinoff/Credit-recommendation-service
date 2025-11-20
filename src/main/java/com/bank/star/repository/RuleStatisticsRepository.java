// Репозиторий для статистики
package com.bank.star.repository;

import com.bank.star.model.RuleStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleStatisticsRepository extends JpaRepository<RuleStatistics, UUID> {

  Optional<RuleStatistics> findByRuleId(UUID ruleId);

  @Query("SELECT rs FROM RuleStatistics rs ORDER BY rs.executionCount DESC")
  List<RuleStatistics> findAllOrderByCountDesc();

  @Modifying
  @Query("DELETE FROM RuleStatistics rs WHERE rs.ruleId = :ruleId")
  void deleteByRuleId(UUID ruleId);

  // Вспомогательный метод для проверки существования
  boolean existsByRuleId(UUID ruleId);
}