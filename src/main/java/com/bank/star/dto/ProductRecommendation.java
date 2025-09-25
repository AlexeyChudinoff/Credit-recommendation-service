package com.bank.star.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import java.util.UUID;

@Schema(
    description = "Детальная информация о рекомендованном банковском продукте",
    example = """
        {
          "name": "Invest 500",
          "id": "147f6a0f-3b91-413b-ab99-87f081d60d5a",
          "text": "Откройте свой путь к успеху с индивидуальным инвестиционным счетом..."
        }
        """
)
public class ProductRecommendation {

  @Schema(
      description = "Название банковского продукта",
      example = "Invest 500",
      requiredMode = Schema.RequiredMode.REQUIRED,
      allowableValues = {"Invest 500", "Top Saving", "Простой кредит"}
  )
  private String name;

  @Schema(
      description = "Уникальный идентификатор продукта в системе банка",
      example = "147f6a0f-3b91-413b-ab99-87f081d60d5a",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private UUID id;

  @Schema(
      description = "Текстовое описание продукта для отображения клиенту",
      example = "Откройте свой путь к успеху с индивидуальным инвестиционным счетом...",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private String text;

  // Конструкторы
  public ProductRecommendation() {
    logger.debug("Создан пустой ProductRecommendation");
  }

  public ProductRecommendation(String name, UUID id, String text) {
    this.name = Objects.requireNonNull(name, "Название продукта не может быть null");
    this.id = Objects.requireNonNull(id, "ID продукта не может быть null");
    this.text = Objects.requireNonNull(text, "Текст описания не может быть null");
    logger.debug("Создан ProductRecommendation: {}", name);
  }

  // Геттеры и сеттеры
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = Objects.requireNonNull(name, "Название продукта не может быть null");
    logger.debug("Установлено название продукта: {}", name);
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = Objects.requireNonNull(id, "ID продукта не может быть null");
    logger.debug("Установлен ID продукта: {}", id);
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = Objects.requireNonNull(text, "Текст описания не может быть null");
    logger.debug("Установлен текст описания для продукта: {}", name);
  }

  // equals и hashCode для корректного сравнения
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ProductRecommendation that = (ProductRecommendation) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "ProductRecommendation{" +
        "name='" + name + '\'' +
        ", id=" + id +
        ", textLength=" + (text != null ? text.length() : 0) +
        '}';
  }

  // Вспомогательные методы
  @Schema(hidden = true)
  public boolean isValid() {
    return name != null && !name.isBlank() &&
        id != null &&
        text != null && !text.isBlank();
  }

  @Schema(hidden = true)
  public String getShortDescription() {
    if (text == null || text.length() <= 100) {
      return text;
    }
    return text.substring(0, 100) + "...";
  }

  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProductRecommendation.class);
}