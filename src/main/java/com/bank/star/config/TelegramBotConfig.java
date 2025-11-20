// TelegramBotConfig
package com.bank.star.config;

import com.bank.star.service.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
public class TelegramBotConfig {

  private static final Logger logger = LoggerFactory.getLogger(TelegramBotConfig.class);

  @Bean
  @ConditionalOnProperty(name = "telegram.bot.token")
  public TelegramBotsApi telegramBotsApi(TelegramBotService botService) {
    try {
      TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
      botsApi.registerBot(botService);
      logger.info("✅ Telegram Bot успешно зарегистрирован");
      return botsApi;
    } catch (TelegramApiException e) {
      logger.error("❌ Ошибка регистрации Telegram Bot: {}", e.getMessage());
      return null;
    }
  }
}