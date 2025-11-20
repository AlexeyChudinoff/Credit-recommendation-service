//ответ с рекомендациями
package com.bank.star.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(
    description = "Ответ сервиса с персонализированными рекомендациями банковских продуктов",
    example = """
        {
          "userId": "cd515076-5d8a-44be-930e-8d4fcb79f42d",
          "recommendations": [
            {
              "name": "Invest 500",
              "id": "147f6a0f-3b91-413b-ab99-87f081d60d5a",
              "text": "Откройте свой путь к успеху с индивидуальным инвестиционным счетом..."
            }
          ]
        }
        """
)
public class RecommendationResponse {

  @Schema(
      description = "Уникальный идентификатор пользователя",
      example = "cd515076-5d8a-44be-930e-8d4fcb79f42d",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private UUID userId;

  @Schema(
      description = "Список рекомендованных банковских продуктов. " +
          "Может быть пустым, если для пользователя нет подходящих рекомендаций",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private List<ProductRecommendation> recommendations;

  // Конструкторы
  public RecommendationResponse() {
    logger.debug("Создан пустой RecommendationResponse");
  }

  public RecommendationResponse(UUID userId, List<ProductRecommendation> recommendations) {
    this.userId = userId;
    this.recommendations = recommendations != null ? recommendations : List.of();
    logger.debug("Создан RecommendationResponse для пользователя {} с {} рекомендациями",
        userId, this.recommendations.size());
  }

  // Геттеры и сеттеры
  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
    logger.debug("Установлен userId: {}", userId);
  }

  public List<ProductRecommendation> getRecommendations() {
    return recommendations != null ? recommendations : List.of();
  }

  public void setRecommendations(List<ProductRecommendation> recommendations) {
    this.recommendations = recommendations != null ? recommendations : List.of();
    logger.debug("Установлено {} рекомендаций", this.recommendations.size());
  }

  // Вспомогательные методы
  @Schema(hidden = true)
  public boolean hasRecommendations() {
    return recommendations != null && !recommendations.isEmpty();
  }

  @Schema(hidden = true)
  public int getRecommendationsCount() {
    return recommendations != null ? recommendations.size() : 0;
  }

  @Override
  public String toString() {
    return "RecommendationResponse{" +
        "userId=" + userId +
        ", recommendationsCount=" + getRecommendationsCount() +
        '}';
  }

  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(
      RecommendationResponse.class);
}