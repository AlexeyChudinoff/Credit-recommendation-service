// Telegram Bot Service
package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

  private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

  private final RecommendationService recommendationService;
  private final UserNameResolver userNameResolver;

  @Value("${telegram.bot.token:8216912842:AAGA3YbcEZHRHSB7QLA8i2mwGlfbQSLyzDU}")
  private String botToken;

  @Value("${telegram.bot.username:alex_Bank_Star_bot}")
  private String botUsername;

  public TelegramBotService(RecommendationService recommendationService,
      UserNameResolver userNameResolver) {
    this.recommendationService = recommendationService;
    this.userNameResolver = userNameResolver;

    logger.info("🤖 Telegram Bot инициализирован:");
    logger.info("   Username: {}", botUsername);
    logger.info("   Token: {}", botToken);
  }

  @Override
  public String getBotUsername() {
    return botUsername;
  }

  @Override
  public String getBotToken() {
    return botToken;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      Message message = update.getMessage();
      String text = message.getText();
      Long chatId = message.getChatId();

      logger.info("📱 Telegram message from {}: {}", chatId, text);

      if (text.startsWith("/start") || text.startsWith("/help")) {
        sendHelpMessage(chatId);
      } else if (text.startsWith("/recommend")) {
        handleRecommendCommand(chatId, text);
      } else {
        sendUnknownCommandMessage(chatId);
      }
    }
  }

  private void handleRecommendCommand(Long chatId, String text) {
    try {
      String[] parts = text.split("\\s+", 2);
      if (parts.length < 2) {
        sendMessage(chatId, "❌ Пожалуйста, укажите имя пользователя: /recommend username");
        return;
      }

      String username = parts[1].trim();
      UUID userId = userNameResolver.resolveUserId(username);

      if (userId == null) {
        sendMessage(chatId, "❌ Пользователь не найден");
        return;
      }

      RecommendationResponse response = recommendationService.getRecommendations(userId);
      String fullName = userNameResolver.getUserFullName(userId);
      String message = formatRecommendations(fullName, response);

      sendMessage(chatId, message);

    } catch (Exception e) {
      logger.error("Error handling recommend command", e);
      sendMessage(chatId, "❌ Произошла ошибка при получении рекомендаций");
    }
  }

  private String formatRecommendations(String fullName, RecommendationResponse response) {
    StringBuilder sb = new StringBuilder();
    sb.append("👋 Здравствуйте, ").append(fullName).append("!\n\n");

    if (response.getRecommendations().isEmpty()) {
      sb.append("📭 К сожалению, у нас пока нет подходящих продуктов для вас.\n");
      sb.append("Продолжайте пользоваться нашими услугами, и мы обязательно предложим вам что-то интересное!");
    } else {
      sb.append("🎯 Новые продукты для вас:\n\n");

      for (ProductRecommendation product : response.getRecommendations()) {
        sb.append("💎 ").append(product.getName()).append("\n");
        sb.append("📝 ").append(product.getText()).append("\n\n");
      }

      sb.append("✨ Хотите узнать больше? Обратитесь к нашему менеджеру!");
    }

    return sb.toString();
  }

  private void sendHelpMessage(Long chatId) {
    String helpText = """
            🏦 Bank Star Recommendation Bot
            
            Доступные команды:
            /start - начать работу
            /help - показать эту справку
            /recommend [username] - получить рекомендации для пользователя
            
            Пример:
            /recommend ivanov
            """;
    sendMessage(chatId, helpText);
  }

  private void sendUnknownCommandMessage(Long chatId) {
    sendMessage(chatId, "❌ Неизвестная команда. Используйте /help для списка команд.");
  }

  private void sendMessage(Long chatId, String text) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId.toString());
    message.setText(text);

    try {
      execute(message);
      logger.info("✅ Telegram message sent to {}", chatId);
    } catch (TelegramApiException e) {
      logger.error("❌ Failed to send Telegram message", e);
    }
  }
}