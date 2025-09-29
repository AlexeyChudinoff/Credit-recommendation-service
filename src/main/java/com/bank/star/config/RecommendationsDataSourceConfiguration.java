package com.bank.star.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class RecommendationsDataSourceConfiguration {

  @Bean(name = "recommendationsDataSource")
  public DataSource recommendationsDataSource(@Value("${spring.datasource.url}") String recommendationsUrl) {
    var dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(recommendationsUrl);
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setReadOnly(true); // Устанавливаем read-only на уровне пула соединений
    dataSource.setUsername("");
    dataSource.setPassword("");

    // Отключаем валидацию соединений, которая может пытаться что-то записать
    dataSource.setConnectionTestQuery("SELECT 1");
    dataSource.setValidationTimeout(3000);

    return dataSource;
  }

  @Bean(name = "recommendationsJdbcTemplate")
  public JdbcTemplate recommendationsJdbcTemplate(
      @Qualifier("recommendationsDataSource") DataSource dataSource
  ) {
    return new JdbcTemplate(dataSource);
  }
}