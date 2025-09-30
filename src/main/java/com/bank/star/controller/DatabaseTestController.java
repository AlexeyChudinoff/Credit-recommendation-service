package com.bank.star.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@RestController
@RequestMapping("/api/test")
public class DatabaseTestController {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseTestController.class);

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @GetMapping("/connection")
  public ResponseEntity<String> testConnection() {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();

      String result = "✅ Подключение успешно!\n" +
          "URL: " + metaData.getURL() + "\n" +
          "User: " + metaData.getUserName() + "\n" +
          "Database: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion() + "\n" +
          "Read Only: " + connection.isReadOnly();

      logger.info(result);
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      String error = "❌ Ошибка подключения: " + e.getMessage();
      logger.error(error, e);
      return ResponseEntity.ok(error);
    }
  }

  @GetMapping("/tables")
  public ResponseEntity<String> listTables() {
    try {
      // Получим список всех таблиц
      String tables = jdbcTemplate.queryForObject(
          "SELECT STRING_AGG(TABLE_NAME, ', ') FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
          String.class
      );

      String result = "✅ Таблицы в базе: " + (tables != null ? tables : "нет таблиц");
      logger.info(result);
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      String error = "❌ Ошибка получения таблиц: " + e.getMessage();
      logger.error(error, e);
      return ResponseEntity.ok(error);
    }
  }

  @GetMapping("/simple-test")
  public ResponseEntity<String> simpleTest() {
    try {
      // Простейший запрос для проверки работы БД
      String result = jdbcTemplate.queryForObject("SELECT 'База данных работает!'", String.class);
      return ResponseEntity.ok("✅ " + result);
    } catch (Exception e) {
      return ResponseEntity.ok("❌ Простейший запрос не работает: " + e.getMessage());
    }
  }
}