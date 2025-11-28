// Telegram Bot Service с функционалом анализа всей базы
package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.dto.UserRecommendation;
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
  private final BatchAnalysisService batchAnalysisService;

  @Value("${telegram.bot.token}")
  private String botToken;

  @Value("${telegram.bot.username}")
  private String botUsername;

  @Value("${telegram.bot.enabled:false}")
  private boolean botEnabled;

  // Тестовые пользователи для демонстрации (оставляем для обратной совместимости)
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

  /**
   * Конструктор сервиса телеграм бота
   * @param recommendationService сервис рекомендаций для отдельных пользователей
   * @param userNameResolver резолвер имен пользователей
   * @param batchAnalysisService сервис пакетного анализа всей базы
   */
  public TelegramBotService(RecommendationService recommendationService,
      UserNameResolver userNameResolver,
      BatchAnalysisService batchAnalysisService) {
    this.recommendationService = recommendationService;
    this.userNameResolver = userNameResolver;
    this.batchAnalysisService = batchAnalysisService;
  }

  /**
   * Инициализация бота после создания бина
   * Проверяет настройки и логирует информацию о боте
   */
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

  /**
   * Возвращает имя бота из конфигурации
   * @return имя бота
   */
  @Override
  public String getBotUsername() {
    return botUsername;
  }

  /**
   * Возвращает токен бота из конфигурации
   * @return токен бота
   */
  @Override
  public String getBotToken() {
    if (!botEnabled) {
      throw new IllegalStateException("Telegram Bot отключен");
    }
    return botToken;
  }

  /**
   * Основной метод обработки входящих сообщений от пользователей
   * @param update объект с данными о входящем сообщении
   */
  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      Message message = update.getMessage();
      String text = message.getText();
      Long chatId = message.getChatId();

      logger.info("📱 Telegram message from {}: {}", chatId, text);

      // Обработка команд и кнопок
      if (text.startsWith("/start") || text.startsWith("/help")) {
        sendHelpMessage(chatId);
      } else if (text.startsWith("/recommend")) {
        handleRecommendCommand(chatId, text);
      } else if (text.startsWith("/testusers")) {
        sendTestUsersInfo(chatId);
      } else if (text.equals("💎 Invest 500") || text.equals("🏦 Top Saving") ||
          text.equals("💳 Простой кредит") || text.equals("❌ Без рекомендаций")) {
        handleBatchAnalysis(chatId, text);
      } else if (text.matches("[0-9a-fA-F-]{36}")) {
        // Обработка прямого ввода UUID
        logger.info("🔍 Detected plain UUID input: {}", text);
        processUserRecommendation(chatId, text);
      } else {
        // Если не команда и не UUID - пробуем найти по имени
        handleUsernameOrUnknown(chatId, text);
      }
    } else if (update.hasCallbackQuery()) {
      // Обработка inline кнопок (для тестовых пользователей)
      String callbackData = update.getCallbackQuery().getData();
      Long chatId = update.getCallbackQuery().getMessage().getChatId();

      if (callbackData.startsWith("recommend_")) {
        String userId = callbackData.replace("recommend_", "");
        processUserRecommendation(chatId, userId);
      }
    }
  }

  /**
   * Обрабатывает нажатие кнопок для пакетного анализа базы данных
   * Запускает поиск всех подходящих пользователей для конкретного продукта
   * @param chatId идентификатор чата
   * @param buttonText текст нажатой кнопки
   */
  private void handleBatchAnalysis(Long chatId, String buttonText) {
    sendMessage(chatId, "⏳ Запускаю анализ базы данных... Это может занять несколько секунд.");

    try {
      List<UserRecommendation> users;
      String productName;

      // Исправленный switch expression без return внутри
      switch (buttonText) {
        case "💎 Invest 500":
          users = batchAnalysisService.getUsersForProduct("Invest 500");
          productName = "Invest 500";
          break;
        case "🏦 Top Saving":
          users = batchAnalysisService.getUsersForProduct("Top Saving");
          productName = "Top Saving";
          break;
        case "💳 Простой кредит":
          users = batchAnalysisService.getUsersForProduct("Простой кредит");
          productName = "Простой кредит";
          break;
        case "❌ Без рекомендаций":
          users = batchAnalysisService.getUsersWithoutRecommendations();
          productName = "без рекомендаций";
          break;
        default:
          sendMessage(chatId, "❌ Неизвестный продукт");
          return;
      }

      sendBatchAnalysisResults(chatId, users, productName);

    } catch (Exception e) {
      logger.error("Error during batch analysis", e);
      sendMessage(chatId, "❌ Произошла ошибка при анализе базы данных");
    }
  }

  /**
   * Форматирует и отправляет результаты пакетного анализа
   * @param chatId идентификатор чата
   * @param users список найденных пользователей
   * @param productName название продукта
   */
  private void sendBatchAnalysisResults(Long chatId, List<UserRecommendation> users, String productName) {
    if (users.isEmpty()) {
      sendMessageWithKeyboard(chatId,
          String.format("📭 Не найдено пользователей для продукта \"%s\"", productName),
          createMainKeyboard());
      return;
    }

    StringBuilder message = new StringBuilder();
    message.append(String.format("🎯 <b>Пользователи для \"%s\"</b>\n\n", productName));
    message.append(String.format("📊 <b>Найдено:</b> %d пользователей\n\n", users.size()));

    // Ограничим вывод первыми 15 пользователями чтобы не превысить лимиты Telegram
    int maxDisplay = Math.min(users.size(), 15);
    for (int i = 0; i < maxDisplay; i++) {
      message.append((i + 1) + ". ").append(users.get(i).toString()).append("\n\n");
    }

    if (users.size() > maxDisplay) {
      message.append(String.format("... и еще %d пользователей", users.size() - maxDisplay));
    }

    message.append("\n💡 <i>Для получения рекомендаций конкретного пользователя введите его UUID или имя</i>");

    sendMessageWithKeyboard(chatId, message.toString(), createMainKeyboard());
  }

  /**
   * Обрабатывает ввод имени пользователя или неизвестной команды
   * @param chatId идентификатор чата
   * @param input введенный пользователем текст
   */
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

  /**
   * Обрабатывает команду /recommend с параметром
   * @param chatId идентификатор чата
   * @param text полный текст команды
   */
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

  /**
   * Обрабатывает запрос рекомендаций для конкретного пользователя
   * @param chatId идентификатор чата
   * @param userInput введенные данные пользователя (UUID или имя)
   */
  private void processUserRecommendation(Long chatId, String userInput) {
    try {
      UUID userId = null;

      // Сначала пытаемся распарсить как UUID
      try {
        userId = UUID.fromString(userInput);
        // Если успешно - это валидный UUID
      } catch (IllegalArgumentException e) {
        // Если не парсится как UUID - пробуем найти как username
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

  /**
   * Форматирует рекомендации в читаемый текст для отправки пользователю
   * @param fullName полное имя пользователя
   * @param response ответ с рекомендациями
   * @return отформатированная строка с рекомендациями
   */
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

  /**
   * Отправляет справку по командам бота
   * @param chatId идентификатор чата
   */
  void sendHelpMessage(Long chatId) {
    String helpText = """
            🏦 <b>Bank Star Recommendation Bot</b>
            
            🤖 <b>Новый функционал кнопок (анализ всей базы):</b>
            • 💎 Invest 500 - найти всех подходящих клиентов
            • 🏦 Top Saving - найти всех подходящих клиентов  
            • 💳 Простой кредит - найти всех подходящих клиентов
            • ❌ Без рекомендаций - клиенты без подходящих продуктов
            
            📋 <b>Команды:</b>
            /recommend [username/UUID] - рекомендации для конкретного пользователя
            /testusers - тестовые пользователи (для демо)
            /help - эта справка
            
            💡 <b>Просто введите:</b>
            • Имя пользователя (например: Rolf Bogisich)
            • UUID пользователя (например: cd515076-5d8a-44be-930e-8d4fcb79f42d)
            """;
    sendMessageWithKeyboard(chatId, helpText, createMainKeyboard());
  }

  /**
   * Отправляет информацию о тестовых пользователях
   * @param chatId идентификатор чата
   */
  void sendTestUsersInfo(Long chatId) {
    sendMessageWithInlineKeyboard(chatId, TEST_USERS_INFO, createTestUsersInlineKeyboard());
  }

  /**
   * Отправляет сообщение о неизвестной команде
   * @param chatId идентификатор чата
   */
  void sendUnknownCommandMessage(Long chatId) {
    String message = """
            ❌ Неизвестная команда.
            
            📋 <b>Доступные команды:</b>
            /help - показать справку
            /testusers - тестовые пользователи
            /recommend [username or UUID] - рекомендации
            
            💡 <b>Или используйте кнопки для анализа всей базы клиентов!</b>
            """;
    sendMessageWithKeyboard(chatId, message, createMainKeyboard());
  }

  /**
   * Создает основную клавиатуру с кнопками для быстрого доступа
   * @return настроенная клавиатура
   */
  private ReplyKeyboardMarkup createMainKeyboard() {
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setSelective(true);
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setOneTimeKeyboard(false);

    List<KeyboardRow> keyboard = new ArrayList<>();

    // Первый ряд кнопок - анализ базы
    KeyboardRow row1 = new KeyboardRow();
    row1.add("💎 Invest 500");
    row1.add("🏦 Top Saving");

    // Второй ряд кнопок - анализ базы
    KeyboardRow row2 = new KeyboardRow();
    row2.add("💳 Простой кредит");
    row2.add("❌ Без рекомендаций");

    // Третий ряд кнопок - команды
    KeyboardRow row3 = new KeyboardRow();
    row3.add("/testusers");
    row3.add("/help");

    keyboard.add(row1);
    keyboard.add(row2);
    keyboard.add(row3);

    keyboardMarkup.setKeyboard(keyboard);
    return keyboardMarkup;
  }

  /**
   * Создает inline клавиатуру для тестовых пользователей
   * @return настроенная inline клавиатура
   */
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

  /**
   * Создает кнопку для inline клавиатуры
   * @param text текст кнопки
   * @param callbackData данные для callback
   * @return настроенная кнопка
   */
  private InlineKeyboardButton createInlineButton(String text, String callbackData) {
    InlineKeyboardButton button = new InlineKeyboardButton();
    button.setText(text);
    button.setCallbackData(callbackData);
    return button;
  }

  /**
   * Отправляет простое текстовое сообщение
   * @param chatId идентификатор чата
   * @param text текст сообщения
   */
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

  /**
   * Отправляет сообщение с основной клавиатурой
   * @param chatId идентификатор чата
   * @param text текст сообщения
   * @param keyboard клавиатура для отображения
   */
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

  /**
   * Отправляет сообщение с inline клавиатурой
   * @param chatId идентификатор чата
   * @param text текст сообщения
   * @param keyboard inline клавиатура
   */
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