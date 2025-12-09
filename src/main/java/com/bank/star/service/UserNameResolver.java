/**
 * Компонент для разрешения имен пользователей в идентификаторы и получения
 * полных имен пользователей по их ID.
 * Обеспечивает взаимодействие с базой данных для получения информации о пользователях.
 */
package com.bank.star.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserNameResolver {

  private final JdbcTemplate jdbcTemplate;  // JdbcTemplate для работы с базой данных

  /**
   * Конструктор с внедрением зависимости JdbcTemplate.
   *
   * @param jdbcTemplate JdbcTemplate для выполнения SQL-запросов
   */
  public UserNameResolver(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Находит идентификатор пользователя по его имени пользователя (username) или полному имени.
   * Поиск осуществляется по точному совпадению username или частичному совпадению полного имени.
   *
   * @param username имя пользователя или часть полного имени
   * @return UUID пользователя или null, если пользователь не найден
   */
  public UUID resolveUserId(String username) {
    try {
      String sql = "SELECT id FROM users WHERE username = ? OR full_name ILIKE ? LIMIT 1";
      String result = jdbcTemplate.queryForObject(sql, String.class, username, "%" + username + "%");
      return result != null ? UUID.fromString(result) : null;
    } catch (Exception e) {
      // В случае ошибки возвращаем null (пользователь не найден или ошибка БД)
      return null;
    }
  }

  /**
   * Получает полное имя пользователя по его идентификатору.
   * Конкатенирует first_name и last_name из базы данных.
   *
   * @param userId уникальный идентификатор пользователя
   * @return полное имя пользователя или "Уважаемый клиент", если пользователь не найден
   */
  public String getUserFullName(UUID userId) {
    try {
      // Предполагается, что в базе данных first_name и last_name хранятся раздельно
      String sql = "SELECT first_name || ' ' || last_name as full_name FROM users WHERE id = ?";
      return jdbcTemplate.queryForObject(sql, String.class, userId.toString());
    } catch (Exception e) {
      // В случае ошибки возвращаем значение по умолчанию
      return "Уважаемый клиент";
    }
  }
}