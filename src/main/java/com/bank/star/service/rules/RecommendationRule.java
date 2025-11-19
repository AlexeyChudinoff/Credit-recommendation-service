//интерфейс правила
package com.bank.star.service.rules;

import java.util.UUID;

public interface RecommendationRule {
  boolean isEligible(UUID userId);
  String getRuleName();
}