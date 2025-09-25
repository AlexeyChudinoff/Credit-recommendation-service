package com.bank.star.model;

/**
 * Перечисление типов банковских продуктов
 */
public enum ProductType {
  /**
   * Дебетовые продукты (карты, счета)
   */
  DEBIT,

  /**
   * Кредитные продукты (кредиты, займы)
   */
  CREDIT,

  /**
   * Сберегательные продукты (вклады, накопления)
   */
  SAVING,

  /**
   * Инвестиционные продукты (ИИС, брокерские счета)
   */
  INVEST;

  /**
   * Проверяет, является ли тип продукта дебетовым
   */
  public boolean isDebit() {
    return this == DEBIT;
  }

  /**
   * Проверяет, является ли тип продукта кредитным
   */
  public boolean isCredit() {
    return this == CREDIT;
  }

  /**
   * Проверяет, является ли тип продукта сберегательным
   */
  public boolean isSaving() {
    return this == SAVING;
  }

  /**
   * Проверяет, является ли тип продукта инвестиционным
   */
  public boolean isInvest() {
    return this == INVEST;
  }

  /**
   * Преобразует строку в ProductType (без учета регистра)
   */
  public static ProductType fromString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }
    return ProductType.valueOf(value.toUpperCase());
  }

  /**
   * Возвращает русское название типа продукта
   */
  public String getRussianName() {
    return switch (this) {
      case DEBIT -> "Дебетовый продукт";
      case CREDIT -> "Кредитный продукт";
      case SAVING -> "Сберегательный продукт";
      case INVEST -> "Инвестиционный продукт";
    };
  }
}