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
@Table(name = "rule_queries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RuleQuery {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  // Тип запроса: USER_OF, ACTIVE_USER_OF и т.д.
  @Enumerated(EnumType.STRING)
  @Column(name = "query_type", nullable = false)
  private QueryType query;

  // Аргументы для запроса (например: ["DEBIT"], ["DEBIT", ">", "100000"])
  @ElementCollection
  @CollectionTable(name = "query_arguments", joinColumns = @JoinColumn(name = "query_id"))
  @Column(name = "argument")
  private List<String> arguments = new ArrayList<>();

  // Отрицание: true = условие НЕ должно выполняться, false = должно выполняться
  @Column(name = "is_negated", nullable = false)
  private boolean negate;

  // Конструктор для удобного создания (без ID)
  public RuleQuery(QueryType query, List<String> arguments, boolean negate) {
    this.query = query;
    this.arguments = arguments;
    this.negate = negate;
  }

  // Вспомогательный метод для получения аргумента по индексу
  public String getArgument(int index) {
    if (arguments == null || index < 0 || index >= arguments.size()) {
      return null;
    }
    return arguments.get(index);
  }

  // Вспомогательный метод для добавления аргумента
  public void addArgument(String argument) {
    if (this.arguments == null) {
      this.arguments = new ArrayList<>();
    }
    this.arguments.add(argument);
  }

  // Вспомогательный метод для проверки валидности запроса
  public boolean isValid() {
    return query != null && arguments != null && !arguments.isEmpty();
  }

  @Override
  public String toString() {
    return "RuleQuery{" +
        "id=" + id +
        ", query=" + query +
        ", arguments=" + arguments +
        ", negate=" + negate +
        '}';
  }
}