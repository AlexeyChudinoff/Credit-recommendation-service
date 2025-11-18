package com.bank.star.model;

import lombok.Getter;

/**
 * Перечисление типов банковских продуктов
 */
@Getter
public enum ProductType {
  /**
   * Дебетовые продукты (карты, счета)
   */
  DEBIT("DEBIT", "Дебетовый продукт"),

  /**
   * Кредитные продукты (кредиты, займы)
   */
  CREDIT("CREDIT", "Кредитный продукт"),

  /**
   * Сберегательные продукты (вклады, накопления)
   */
  SAVING("SAVING", "Сберегательный продукт"),

  /**
   * Инвестиционные продукты (ИИС, брокерские счета)
   */
  INVEST("INVEST", "Инвестиционный продукт");

  private final String code;
  private final String russianName;

  ProductType(String code, String russianName) {
    this.code = code;
    this.russianName = russianName;
  }

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
    for (ProductType type : ProductType.values()) {
      if (type.code.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ProductType: " + value);
  }

  /**
   * Проверяет, является ли строка валидным ProductType
   */
  public static boolean isValidType(String value) {
    try {
      fromString(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Возвращает все возможные значения как строки
   */
  public static String[] getStringValues() {
    ProductType[] values = values();
    String[] stringValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      stringValues[i] = values[i].code;
    }
    return stringValues;
  }

  @Override
  public String toString() {
    return code;
  }
}