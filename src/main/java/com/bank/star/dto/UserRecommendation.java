// Простой Data Transfer Object (DTO) для передачи информации о пользователе
// Для отображения результатов пакетного анализа в читаемом формате
package com.bank.star.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * DTO для представления пользователя в результатах пакетного анализа
 * Содержит идентификатор пользователя и его полное имя
 * Используется для отображения списков подходящих клиентов в телеграм-боте
 */
@Getter
@AllArgsConstructor
public class UserRecommendation {
  private UUID userId;
  private String fullName;

  @Override
  public String toString() {
    return String.format("👤 %s\n🆔 %s", fullName, userId);
  }
}