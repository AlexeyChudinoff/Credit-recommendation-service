// UserNameResolver
package com.bank.star.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserNameResolver {

  private final JdbcTemplate jdbcTemplate;

  public UserNameResolver(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public UUID resolveUserId(String username) {
    try {
      String sql = "SELECT id FROM users WHERE username = ? OR full_name ILIKE ? LIMIT 1";
      String result = jdbcTemplate.queryForObject(sql, String.class, username, "%" + username + "%");
      return result != null ? UUID.fromString(result) : null;
    } catch (Exception e) {
      return null;
    }
  }

  public String getUserFullName(UUID userId) {
    try {
      // Если в базе first_name и last_name раздельно
      String sql = "SELECT first_name || ' ' || last_name as full_name FROM users WHERE id = ?";
      return jdbcTemplate.queryForObject(sql, String.class, userId.toString());
    } catch (Exception e) {
      return "Уважаемый клиент";
    }
  }
}