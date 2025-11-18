package com.bank.star.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dynamic_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DynamicRule {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;  // Уникальный ID правила

  @Column(name = "product_name", nullable = false)
  private String productName;    // Название продукта ("Простой кредит")

  @Column(name = "product_id", nullable = false)
  private UUID productId;        // ID продукта в системе банка

  @Column(name = "product_text", nullable = false, length = 1000)
  private String productText;    // Текст рекомендации для пользователя

  // Список условий правила (например: "имеет дебетовую карту", "нет кредитов" и т.д.)
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "rule_id")  // Связываем с таблицей rule_queries
  private List<RuleQuery> queries = new ArrayList<>();

  // Конструктор для удобного создания (без ID)
  public DynamicRule(String productName, UUID productId, String productText, List<RuleQuery> queries) {
    this.productName = productName;
    this.productId = productId;
    this.productText = productText;
    this.queries = queries;
  }

  // Вспомогательный метод для добавления условия
  public void addQuery(RuleQuery query) {
    if (this.queries == null) {
      this.queries = new ArrayList<>();
    }
    this.queries.add(query);
  }

  // Вспомогательный метод для проверки валидности правила
  public boolean isValid() {
    return productName != null && !productName.isBlank() &&
        productId != null &&
        productText != null && !productText.isBlank() &&
        queries != null && !queries.isEmpty();
  }

  @Override
  public String toString() {
    return "DynamicRule{" +
        "id=" + id +
        ", productName='" + productName + '\'' +
        ", productId=" + productId +
        ", queriesCount=" + (queries != null ? queries.size() : 0) +
        '}';
  }
}