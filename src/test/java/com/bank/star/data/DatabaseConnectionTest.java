package com.bank.star.data;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class DatabaseConnectionTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void testDatabaseConnection() {
    // Простой запрос для проверки подключения
    String result = jdbcTemplate.queryForObject("SELECT 'Database is connected'", String.class);
    assertNotNull(result);
    assertTrue(result.contains("connected"));
    System.out.println("✅ " + result);
  }

  @Test
  void testTablesExist() {
    // Проверяем, что таблицы существуют
    Integer tableCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
        Integer.class
    );
    assertNotNull(tableCount);
    assertTrue(tableCount > 0);
    System.out.println("✅ Количество таблиц в базе: " + tableCount);
  }

  @Test
  void testUsersTableHasData() {
    // Проверяем, что в таблице users есть данные
    Integer userCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users",
        Integer.class
    );
    assertNotNull(userCount);
    assertTrue(userCount > 0);
    System.out.println("✅ Количество пользователей в базе: " + userCount);
  }
}