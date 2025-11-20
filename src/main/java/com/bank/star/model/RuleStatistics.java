// Сущность для статистики правил
package com.bank.star.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "rule_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RuleStatistics {

  @Id
  @Column(name = "rule_id")
  private UUID ruleId;

  @Column(name = "execution_count", nullable = false)
  private Long executionCount = 0L;

  @OneToOne
  @JoinColumn(name = "rule_id", insertable = false, updatable =false)
  private DynamicRule rule;

  // Конструктор для создания новой статистики
  public RuleStatistics(UUID ruleId) {
    this.ruleId = ruleId;
    this.executionCount = 0L;
  }

  public RuleStatistics(UUID ruleId, Long executionCount) {
    this.ruleId = ruleId;
    this.executionCount = executionCount;
  }

  public void incrementCount() {
    this.executionCount++;
  }
}