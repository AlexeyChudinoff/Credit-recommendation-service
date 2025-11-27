// проверка настройки телеграмм бота в момент запуска
package com.bank.star.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

  private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

  @Value("${telegram.bot.token:}")
  private String botToken;

  @Value("${telegram.bot.enabled:false}")
  private boolean botEnabled;

  @PostConstruct
  public void validateConfig() {
    logger.info("🔐 Проверка конфигурации Telegram Bot...");

    // ДЕБАГ: покажи значение токена в Spring
    logger.info("🤖 Bot Token в Spring: {}",
        (botToken == null || botToken.startsWith("${") ? "НЕ НАСТРОЕН" :
            "***" + botToken.substring(Math.max(0, botToken.length() - 4))));

    if (botEnabled) {
      if (botToken == null || botToken.isEmpty() || botToken.startsWith("${")) {
        logger.error("❌ ОШИБКА: Telegram Bot Token не настроен!");
        logger.error("   Создайте файл .env из .env.example и заполните TELEGRAM_BOT_TOKEN");
        throw new IllegalStateException(
            "Telegram Bot Token не настроен. " +
                "Создайте .env файл с TELEGRAM_BOT_TOKEN"
        );
      }
      logger.info("✅ Telegram Bot Token настроен корректно");
    } else {
      logger.info("🚫 Telegram Bot отключен в настройках");
    }

    logger.info("🔐 Проверка конфигурации завершена");
  }
}