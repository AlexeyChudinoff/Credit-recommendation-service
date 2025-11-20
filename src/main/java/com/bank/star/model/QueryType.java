//enum типов запросов
package com.bank.star.model;

import lombok.Getter;

@Getter
public enum QueryType {
  USER_OF("USER_OF", "Пользователь продукта",
      "Проверяет, является ли пользователь клиентом продукта указанного типа"),

  ACTIVE_USER_OF("ACTIVE_USER_OF", "Активный пользователь продукта",
      "Проверяет, является ли пользователь активным клиентом продукта (≥5 транзакций)"),

  TRANSACTION_SUM_COMPARE("TRANSACTION_SUM_COMPARE", "Сравнение суммы транзакций",
      "Сравнивает сумму транзакций с указанным числом"),

  TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW("TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW",
      "Сравнение пополнений и трат",
      "Сравнивает сумму пополнений и трат по продукту");

  private final String code;
  private final String description;
  private final String details;

  QueryType(String code, String description, String details) {
    this.code = code;
    this.description = description;
    this.details = details;
  }

  // Метод для преобразования строки в enum (без учета регистра)
  public static QueryType fromString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }
    for (QueryType type : QueryType.values()) {
      if (type.code.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown QueryType: " + value);
  }

  // Метод для проверки, поддерживается ли тип запроса
  public static boolean isValidType(String value) {
    try {
      fromString(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public String toString() {
    return code;
  }
}