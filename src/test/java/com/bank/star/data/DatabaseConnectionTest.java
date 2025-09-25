package com.bank.star.data;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    System.out.println("✅ " + result);
  }

  @Test
  void testTablesExist() {
    // Проверяем, что таблицы существуют
    Integer userCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
        Integer.class
    );
    assertNotNull(userCount);
    System.out.println("✅ Количество таблиц в базе: " + userCount);
  }

}