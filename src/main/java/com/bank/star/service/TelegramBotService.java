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

  // Тестовые пользователи для демонстрации
  private static final String TEST_USERS_INFO = """
      👥 <b>Тестовые пользователи для демонстрации:</b>
      
      💎 <b>Invest 500</b> (инвестиции)
      👤 <code>cd515076-5d8a-44be-930e-8d4fcb79f42d</code>
      📊 Имеет дебетовые продукты + сбережения > 1,000 ₽
      
      🏦 <b>Top Saving</b> (премиальные накопления)  
      👤 <code>d4a4d619-9a0c-4fc5-b0cb-76c49409546b</code>
      📊 Большие пополнения (≥ 50,000 ₽) + положительный баланс
      
      💳 <b>Простой кредит</b> (базовый кредит)
      👤 <code>1f9b149c-6577-448a-bc94-16bea229b71a</code>
      📊 Большие траты (> 100,000 ₽) + нет текущих кредитов
      
      ❌ <b>Без рекомендаций</b>
      👤 <code>a1b2c3d4-5e6f-4890-9a0b-c1d2e3f4a5b6</code>
      📊 Не подходит под правила рекомендаций
      """;

  public TelegramBotService(RecommendationService recommendationService,
      UserNameResolver userNameResolver) {
    this.recommendationService = recommendationService;
    this.userNameResolver = userNameResolver;

    logger.info("🤖 Telegram Bot инициализирован:");
    logger.info("   Username: {}", botUsername);
    logger.info("   Token: {}", botToken != null ? botToken.substring(0, 10) + "..." : "null");
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
      } else if (text.startsWith("/testusers")) {
        sendTestUsersInfo(chatId);
      } else {
        sendUnknownCommandMessage(chatId);
      }
    }
  }

  private void handleRecommendCommand(Long chatId, String text) {
    try {
      String[] parts = text.split("\\s+", 2);
      if (parts.length < 2) {
        sendMessage(chatId, "❌ Пожалуйста, укажите ID пользователя: /recommend user_id");
        sendMessage(chatId, "📋 Используйте /testusers чтобы посмотреть тестовые ID");
        return;
      }

      String userInput = parts[1].trim();

      // Пытаемся найти пользователя по username или UUID
      UUID userId = null;

      // Если введен UUID
      if (userInput.matches("[0-9a-fA-F-]{36}")) {
        try {
          userId = UUID.fromString(userInput);
        } catch (IllegalArgumentException e) {
          sendMessage(chatId, "❌ Неверный формат UUID. Используйте /testusers для примеров");
          return;
        }
      } else {
        // Если введен username
        userId = userNameResolver.resolveUserId(userInput);
      }

      if (userId == null) {
        sendMessage(chatId, "❌ Пользователь не найден. Проверьте ID или username");
        sendMessage(chatId, "📋 Используйте /testusers чтобы посмотреть тестовые ID");
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
      sb.append("🎯 <b>Новые продукты для вас:</b>\n\n");

      for (ProductRecommendation product : response.getRecommendations()) {
        sb.append("💎 <b>").append(product.getName()).append("</b>\n");
        sb.append("📝 ").append(product.getText()).append("\n\n");
      }

      sb.append("✨ Хотите узнать больше? Обратитесь к нашему менеджеру!");
    }

    return sb.toString();
  }

  void sendHelpMessage(Long chatId) {
    String helpText = """
            🏦 <b>Bank Star Recommendation Bot</b>
            
            🤖 <b>Доступные команды:</b>
            /start - начать работу
            /help - показать эту справку
            /recommend [user_id] - получить рекомендации для пользователя
            /testusers - показать тестовых пользователей
            
            📝 <b>Примеры использования:</b>
            <code>/recommend cd515076-5d8a-44be-930e-8d4fcb79f42d</code>
            <code>/recommend invest_user</code>
            
            👥 Для тестирования используйте команду <b>/testusers</b> чтобы увидеть всех тестовых пользователей!
            """;
    sendMessage(chatId, helpText);
  }

  void sendTestUsersInfo(Long chatId) {
    sendMessage(chatId, TEST_USERS_INFO);
  }

  void sendUnknownCommandMessage(Long chatId) {
    String message = "❌ Неизвестная команда.\n\n" +
        "📋 Используйте:\n" +
        "/help - список команд\n" +
        "/testusers - тестовые пользователи";
    sendMessage(chatId, message);
  }

  void sendMessage(Long chatId, String text) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId.toString());
    message.setText(text);
    message.enableHtml(true); // Включаем HTML разметку для красивого форматирования

    try {
      execute(message);
      logger.info("✅ Telegram message sent to {}", chatId);
    } catch (TelegramApiException e) {
      logger.error("❌ Failed to send Telegram message", e);
    }
  }
}