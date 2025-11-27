// Telegram Bot Service с интерактивными кнопками
package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

  private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

  private final RecommendationService recommendationService;
  private final UserNameResolver userNameResolver;

  @Value("${telegram.bot.token}")
  private String botToken;

  @Value("${telegram.bot.username}")
  private String botUsername;

  @Value("${telegram.bot.enabled:false}")
  private boolean botEnabled;

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
  }

  @PostConstruct
  public void init() {
    if (!botEnabled) {
      logger.warn("🚫 Telegram Bot отключен в конфигурации");
      return;
    }

    if (botToken == null || botToken.isEmpty() || botToken.startsWith("${")) {
      logger.error("❌ Telegram Bot Token не настроен. Проверьте переменные окружения.");
      return;
    }

    logger.info("🤖 Telegram Bot инициализирован:");
    logger.info("   Username: {}", botUsername);
    logger.info("   Token: {}...", botToken.substring(0, Math.min(10, botToken.length())));
  }


  @Override
  public String getBotUsername() {
    return botUsername;
  }

  @Override
  public String getBotToken() {
    if (!botEnabled) {
      throw new IllegalStateException("Telegram Bot отключен");
    }
    return botToken;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      Message message = update.getMessage();
      String text = message.getText();
      Long chatId = message.getChatId();

      logger.info("📱 Telegram message from {}: {}", chatId, text);

      // 1. Проверяем известные команды и кнопки
      if (text.startsWith("/start") || text.startsWith("/help")) {
        sendHelpMessage(chatId);
      } else if (text.startsWith("/recommend")) {
        handleRecommendCommand(chatId, text);
      } else if (text.startsWith("/testusers")) {
        sendTestUsersInfo(chatId);
      } else if (text.equals("💎 Invest 500") || text.equals("🏦 Top Saving") ||
          text.equals("💳 Простой кредит") || text.equals("❌ Без рекомендаций")) {
        handleQuickRecommend(chatId, text);
      } else if (text.matches("[0-9a-fA-F-]{36}")) {
        // 2. Обработка прямого ввода UUID
        logger.info("🔍 Detected plain UUID input: {}", text);
        processUserRecommendation(chatId, text);
      } else {
        // 3. Если не команда и не UUID - пробуем найти по имени
        handleUsernameOrUnknown(chatId, text);
      }
    } else if (update.hasCallbackQuery()) {
      // Обработка inline кнопок
      String callbackData = update.getCallbackQuery().getData();
      Long chatId = update.getCallbackQuery().getMessage().getChatId();

      if (callbackData.startsWith("recommend_")) {
        String userId = callbackData.replace("recommend_", "");
        handleQuickRecommendById(chatId, userId);
      }
    }
  }

  private void handleUsernameOrUnknown(Long chatId, String input) {
    try {
      logger.info("🔍 Trying to resolve input as username: {}", input);

      UUID userId = userNameResolver.resolveUserId(input);

      if (userId != null) {
        // Найден пользователь по имени - показываем рекомендации
        logger.info("✅ User found by username '{}': {}", input, userId);
        processUserRecommendation(chatId, input);
      } else {
        // Не команда, не UUID, и не username - неизвестная команда
        logger.info("❌ Input '{}' is not a recognized command or username", input);
        sendUnknownCommandMessage(chatId);
      }

    } catch (Exception e) {
      logger.error("Error handling username input", e);
      sendMessage(chatId, "❌ Произошла ошибка при поиске пользователя");
    }
  }

  private void handleRecommendCommand(Long chatId, String text) {
    try {
      String[] parts = text.split("\\s+", 2);
      if (parts.length < 2) {
        sendMessageWithKeyboard(chatId,
            "❌ Пожалуйста, укажите username или UUID пользователя:",
            createMainKeyboard());
        return;
      }

      String userInput = parts[1].trim();
      processUserRecommendation(chatId, userInput);

    } catch (Exception e) {
      logger.error("Error handling recommend command", e);
      sendMessage(chatId, "❌ Произошла ошибка при получении рекомендаций");
    }
  }

  private void handleQuickRecommend(Long chatId, String buttonText) {
    String userId = switch (buttonText) {
      case "💎 Invest 500" -> "cd515076-5d8a-44be-930e-8d4fcb79f42d";
      case "🏦 Top Saving" -> "d4a4d619-9a0c-4fc5-b0cb-76c49409546b";
      case "💳 Простой кредит" -> "1f9b149c-6577-448a-bc94-16bea229b71a";
      case "❌ Без рекомендаций" -> "a1b2c3d4-5e6f-4890-9a0b-c1d2e3f4a5b6";
      default -> null;
    };

    if (userId != null) {
      processUserRecommendation(chatId, userId);
    }
  }

  private void handleQuickRecommendById(Long chatId, String userId) {
    processUserRecommendation(chatId, userId);
  }

  private void processUserRecommendation(Long chatId, String userInput) {
    try {
      UUID userId = null;

      // Если введен UUID
      if (userInput.matches("[0-9a-fA-F-]{36}")) {
        try {
          userId = UUID.fromString(userInput);
        } catch (IllegalArgumentException e) {
          sendMessageWithKeyboard(chatId,
              "❌ Неверный формат UUID. Используйте кнопки ниже:",
              createMainKeyboard());
          return;
        }
      } else {
        // Если введен username
        userId = userNameResolver.resolveUserId(userInput);
      }

      if (userId == null) {
        sendMessageWithKeyboard(chatId,
            "❌ Пользователь '" + userInput + "' не найден. Используйте кнопки ниже:",
            createMainKeyboard());
        return;
      }

      RecommendationResponse response = recommendationService.getRecommendations(userId);
      String fullName = userNameResolver.getUserFullName(userId);
      String message = formatRecommendations(fullName, response);

      sendMessageWithKeyboard(chatId, message, createMainKeyboard());

    } catch (Exception e) {
      logger.error("Error processing recommendation", e);
      sendMessage(chatId, "❌ Произошла ошибка при получении рекомендаций");
    }
  }

  private String formatRecommendations(String fullName, RecommendationResponse response) {
    StringBuilder sb = new StringBuilder();
    sb.append("👋 Здравствуйте, <b>").append(fullName).append("</b>!\n\n");

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
            /recommend [username or UUID] - получить рекомендации для пользователя
            /testusers - показать тестовых пользователей
            
            💡 <b>Просто введите:</b>
            • Имя пользователя (например: Rolf Bogisich)
            • UUID пользователя (например: cd515076-5d8a-44be-930e-8d4fcb79f42d)
            • Или используйте кнопки ниже!
            """;
    sendMessageWithKeyboard(chatId, helpText, createMainKeyboard());
  }

  void sendTestUsersInfo(Long chatId) {
    sendMessageWithInlineKeyboard(chatId, TEST_USERS_INFO, createTestUsersInlineKeyboard());
  }

  void sendUnknownCommandMessage(Long chatId) {
    String message = """
            ❌ Неизвестная команда.
            
            📋 <b>Доступные команды:</b>
            /help - показать справку
            /testusers - тестовые пользователи
            /recommend [username or UUID] - рекомендации
            
            💡 <b>Или просто введите:</b>
            • Имя пользователя (Rolf Bogisich)
            • UUID пользователя (cd515076-5d8a-44be-930e-8d4fcb79f42d)
            • Используйте кнопки ниже ⬇️
            """;
    sendMessageWithKeyboard(chatId, message, createMainKeyboard());
  }

  // Создание основной клавиатуры с кнопками
  private ReplyKeyboardMarkup createMainKeyboard() {
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setSelective(true);
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setOneTimeKeyboard(false);

    List<KeyboardRow> keyboard = new ArrayList<>();

    // Первый ряд кнопок
    KeyboardRow row1 = new KeyboardRow();
    row1.add("💎 Invest 500");
    row1.add("🏦 Top Saving");

    // Второй ряд кнопок
    KeyboardRow row2 = new KeyboardRow();
    row2.add("💳 Простой кредит");
    row2.add("❌ Без рекомендаций");

    // Третий ряд кнопок
    KeyboardRow row3 = new KeyboardRow();
    row3.add("/testusers");
    row3.add("/help");

    keyboard.add(row1);
    keyboard.add(row2);
    keyboard.add(row3);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
  }

  // Создание inline клавиатуры для тестовых пользователей
  private InlineKeyboardMarkup createTestUsersInlineKeyboard() {
    InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    // Первый ряд
    List<InlineKeyboardButton> row1 = new ArrayList<>();
    row1.add(createInlineButton("💎 Invest 500", "recommend_cd515076-5d8a-44be-930e-8d4fcb79f42d"));
    row1.add(createInlineButton("🏦 Top Saving", "recommend_d4a4d619-9a0c-4fc5-b0cb-76c49409546b"));

    // Второй ряд
    List<InlineKeyboardButton> row2 = new ArrayList<>();
    row2.add(createInlineButton("💳 Простой кредит", "recommend_1f9b149c-6577-448a-bc94-16bea229b71a"));
    row2.add(createInlineButton("❌ Без рекомендаций", "recommend_a1b2c3d4-5e6f-4890-9a0b-c1d2e3f4a5b6"));

    rows.add(row1);
    rows.add(row2);

    inlineKeyboard.setKeyboard(rows);
    return inlineKeyboard;
  }

  private InlineKeyboardButton createInlineButton(String text, String callbackData) {
    InlineKeyboardButton button = new InlineKeyboardButton();
    button.setText(text);
    button.setCallbackData(callbackData);
    return button;
  }

  void sendMessage(Long chatId, String text) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId.toString());
    message.setText(text);
    message.enableHtml(true);

    try {
      execute(message);
      logger.info("✅ Telegram message sent to {}", chatId);
    } catch (TelegramApiException e) {
      logger.error("❌ Failed to send Telegram message", e);
    }
  }

  void sendMessageWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId.toString());
    message.setText(text);
    message.enableHtml(true);
    message.setReplyMarkup(keyboard);

    try {
      execute(message);
      logger.info("✅ Telegram message with keyboard sent to {}", chatId);
    } catch (TelegramApiException e) {
      logger.error("❌ Failed to send Telegram message with keyboard", e);
    }
  }

  void sendMessageWithInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId.toString());
    message.setText(text);
    message.enableHtml(true);
    message.setReplyMarkup(keyboard);

    try {
      execute(message);
      logger.info("✅ Telegram message with inline keyboard sent to {}", chatId);
    } catch (TelegramApiException e) {
      logger.error("❌ Failed to send Telegram message with inline keyboard", e);
    }
  }
}