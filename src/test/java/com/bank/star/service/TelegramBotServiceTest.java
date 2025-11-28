package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import com.bank.star.dto.UserRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TelegramBotServiceTest {

  private static final Logger logger = LoggerFactory.getLogger(TelegramBotServiceTest.class);

  @Mock
  private RecommendationService recommendationService;

  @Mock
  private UserNameResolver userNameResolver;

  @Mock
  private BatchAnalysisService batchAnalysisService;

  @InjectMocks
  private TelegramBotService telegramBotService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);

    ReflectionTestUtils.setField(telegramBotService, "botToken", "test_token");
    ReflectionTestUtils.setField(telegramBotService, "botUsername", "test_bot");
    ReflectionTestUtils.setField(telegramBotService, "botEnabled", true);

    telegramBotService = spy(telegramBotService);

    // Мокаем только публичные методы отправки сообщений
    doNothing().when(telegramBotService).sendMessage(anyLong(), anyString());
    doNothing().when(telegramBotService).sendMessageWithKeyboard(anyLong(), anyString(), any(ReplyKeyboardMarkup.class));
    doNothing().when(telegramBotService).sendMessageWithInlineKeyboard(anyLong(), anyString(), any(InlineKeyboardMarkup.class));
  }

  // ===== ПРОСТЫЕ ТЕСТЫ НА КОНСТРУКТОР И ГЕТТЕРЫ =====

  @Test
  void testBotInitialization() {
    assertNotNull(telegramBotService);
  }

  @Test
  void testGetBotUsername() {
    assertEquals("test_bot", telegramBotService.getBotUsername());
  }

  @Test
  void testGetBotTokenWhenEnabled() {
    ReflectionTestUtils.setField(telegramBotService, "botEnabled", true);
    assertEquals("test_token", telegramBotService.getBotToken());
  }

  @Test
  void testGetBotTokenWhenDisabled() {
    ReflectionTestUtils.setField(telegramBotService, "botEnabled", false);
    assertThrows(IllegalStateException.class, () -> telegramBotService.getBotToken());
  }

  // ===== ПРОСТЫЕ ТЕСТЫ НА КОМАНДЫ =====

  @Test
  void testHandleStartCommand() {
    Update update = createUpdateWithMessage("/start", 123L);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessageWithKeyboard(eq(123L), anyString(), any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleHelpCommand() {
    Update update = createUpdateWithMessage("/help", 123L);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessageWithKeyboard(eq(123L), anyString(), any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleTestUsersCommand() {
    Update update = createUpdateWithMessage("/testusers", 123L);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessageWithInlineKeyboard(eq(123L), anyString(), any(InlineKeyboardMarkup.class));
  }

  // ===== ПРОСТЫЕ ТЕСТЫ НА РЕКОМЕНДАЦИИ =====

  @Test
  void testHandleRecommendCommandWithoutArguments() {
    Update update = createUpdateWithMessage("/recommend", 123L);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        contains("укажите username или UUID пользователя"),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleRecommendCommandUserNotFound() {
    Update update = createUpdateWithMessage("/recommend unknownuser", 123L);
    when(userNameResolver.resolveUserId("unknownuser")).thenReturn(null);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        contains("Пользователь 'unknownuser' не найден"),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleRecommendCommandWithUsername() {
    Update update = createUpdateWithMessage("/recommend testuser", 123L);
    UUID userId = UUID.randomUUID();

    when(userNameResolver.resolveUserId("testuser")).thenReturn(userId);
    when(userNameResolver.getUserFullName(userId)).thenReturn("Test User");
    when(recommendationService.getRecommendations(userId))
        .thenReturn(new RecommendationResponse(userId, List.of(
            new ProductRecommendation("Product1", UUID.randomUUID(), "Description1")
        )));

    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        contains("Test User"),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleRecommendCommandWithUUID() {
    UUID userId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");
    Update update = createUpdateWithMessage("/recommend " + userId, 123L);

    when(userNameResolver.getUserFullName(userId)).thenReturn("Test User");
    when(recommendationService.getRecommendations(userId))
        .thenReturn(new RecommendationResponse(userId, List.of(
            new ProductRecommendation("Product1", UUID.randomUUID(), "Description1")
        )));

    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        contains("Test User"),
        any(ReplyKeyboardMarkup.class));
  }

  // ===== ТЕСТЫ НА НЕВАЛИДНЫЕ UUID =====

  @Test
  void testHandleMalformedUUID() {
    // Строка выглядит как UUID но с неверными символами - должна обрабатываться как username
    Update update = createUpdateWithMessage("/recommend xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", 123L);

    // Настраиваем поведение - этот "username" не найден
    when(userNameResolver.resolveUserId("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")).thenReturn(null);

    telegramBotService.onUpdateReceived(update);

    // Должно отправляться сообщение "Пользователь не найден"
    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(message -> message.contains("Пользователь 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx' не найден")),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleValidUUID() {
    // Валидный UUID - должен обрабатываться как UUID
    String validUUID = "cd515076-5d8a-44be-930e-8d4fcb79f42d";
    Update update = createUpdateWithMessage("/recommend " + validUUID, 123L);

    UUID userId = UUID.fromString(validUUID);
    when(userNameResolver.getUserFullName(userId)).thenReturn("Test User");
    when(recommendationService.getRecommendations(userId))
        .thenReturn(new RecommendationResponse(userId, List.of()));

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        contains("Test User"),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleUsername() {
    // Обычное имя пользователя
    Update update = createUpdateWithMessage("/recommend alex", 123L);

    UUID userId = UUID.randomUUID();
    when(userNameResolver.resolveUserId("alex")).thenReturn(userId);
    when(userNameResolver.getUserFullName(userId)).thenReturn("Алексей Чудинов");
    when(recommendationService.getRecommendations(userId))
        .thenReturn(new RecommendationResponse(userId, List.of()));

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        contains("Алексей Чудинов"),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleInvalidUUIDFormat() {
    // Строка, которая не проходит проверку регулярного выражения для UUID
    Update update = createUpdateWithMessage("/recommend not-a-uuid-format", 123L);

    // "not-a-uuid-format" не проходит regex [0-9a-fA-F-]{36}, поэтому ищется как username
    when(userNameResolver.resolveUserId("not-a-uuid-format")).thenReturn(null);

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(message -> message.contains("Пользователь 'not-a-uuid-format' не найден")),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleShortInvalidUUID() {
    Update update = createUpdateWithMessage("/recommend 123", 123L);

    // "123" не проходит regex [0-9a-fA-F-]{36}, поэтому ищется как username
    when(userNameResolver.resolveUserId("123")).thenReturn(null);

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(message -> message.contains("Пользователь '123' не найден")),
        any(ReplyKeyboardMarkup.class));
  }

  // ===== ПРОСТЫЕ ТЕСТЫ НА КНОПКИ АНАЛИЗА БАЗЫ =====

  @Test
  void testHandleBatchAnalysisInvest500() {
    Update update = createUpdateWithMessage("💎 Invest 500", 123L);

    List<UserRecommendation> mockUsers = List.of(
        new UserRecommendation(UUID.randomUUID(), "User 1"),
        new UserRecommendation(UUID.randomUUID(), "User 2")
    );

    when(batchAnalysisService.getUsersForProduct("Invest 500"))
        .thenReturn(mockUsers);

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(msg -> msg.contains("Invest 500") && msg.contains("2 пользователей")),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleBatchAnalysisTopSaving() {
    Update update = createUpdateWithMessage("🏦 Top Saving", 123L);

    when(batchAnalysisService.getUsersForProduct("Top Saving"))
        .thenReturn(List.of(new UserRecommendation(UUID.randomUUID(), "User 1")));

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(msg -> msg.contains("Top Saving")),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleBatchAnalysisSimpleCredit() {
    Update update = createUpdateWithMessage("💳 Простой кредит", 123L);

    when(batchAnalysisService.getUsersForProduct("Простой кредит"))
        .thenReturn(List.of());

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(msg -> msg.contains("Не найдено пользователей") && msg.contains("Простой кредит")),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleBatchAnalysisNoRecommendations() {
    Update update = createUpdateWithMessage("❌ Без рекомендаций", 123L);

    when(batchAnalysisService.getUsersWithoutRecommendations())
        .thenReturn(List.of(
            new UserRecommendation(UUID.randomUUID(), "User A"),
            new UserRecommendation(UUID.randomUUID(), "User B")
        ));

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(msg -> msg.contains("без рекомендаций") && msg.contains("2 пользователей")),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleBatchAnalysisEmptyResults() {
    Update update = createUpdateWithMessage("💎 Invest 500", 123L);

    when(batchAnalysisService.getUsersForProduct("Invest 500"))
        .thenReturn(List.of());

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(msg -> msg.contains("Не найдено пользователей") && msg.contains("Invest 500")),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleBatchAnalysisWithManyUsers() {
    Update update = createUpdateWithMessage("💎 Invest 500", 123L);

    List<UserRecommendation> manyUsers = new ArrayList<>();
    for (int i = 1; i <= 20; i++) {
      manyUsers.add(new UserRecommendation(UUID.randomUUID(), "User " + i));
    }

    when(batchAnalysisService.getUsersForProduct("Invest 500"))
        .thenReturn(manyUsers);

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(msg -> msg.contains("20 пользователей") && msg.contains("еще 5 пользователей")),
        any(ReplyKeyboardMarkup.class));
  }

  // ===== ПРОСТЫЕ ТЕСТЫ НА ОШИБКИ =====

  @Test
  void testHandleUsernameOrUnknownWithException() {
    Update update = createUpdateWithMessage("erroruser", 123L);
    when(userNameResolver.resolveUserId("erroruser"))
        .thenThrow(new RuntimeException("Database error"));

    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessage(eq(123L),
        contains("❌ Произошла ошибка при поиске пользователя"));
  }

  @Test
  void testHandleBatchAnalysisException() {
    Update update = createUpdateWithMessage("💎 Invest 500", 123L);

    when(batchAnalysisService.getUsersForProduct("Invest 500"))
        .thenThrow(new RuntimeException("Database error"));

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessage(eq(123L),
        contains("❌ Произошла ошибка при анализе базы данных"));
  }

  // ===== ПРОСТЫЕ ТЕСТЫ НА ОТПРАВКУ СООБЩЕНИЙ =====

  @Test
  void testSendMessageSuccess() throws TelegramApiException {
    Long chatId = 555L;
    String text = "Test message";

    TelegramBotService realBotService = new TelegramBotService(recommendationService, userNameResolver, batchAnalysisService);
    ReflectionTestUtils.setField(realBotService, "botToken", "test_token");
    ReflectionTestUtils.setField(realBotService, "botUsername", "test_bot");
    ReflectionTestUtils.setField(realBotService, "botEnabled", true);

    TelegramBotService spyBot = spy(realBotService);
    Message mockMessage = mock(Message.class);
    doReturn(mockMessage).when(spyBot).execute(any(SendMessage.class));

    spyBot.sendMessage(chatId, text);

    verify(spyBot).execute(argThat((SendMessage sendMessage) ->
        sendMessage.getChatId().equals(chatId.toString()) &&
            sendMessage.getText().equals(text)
    ));
  }

  @Test
  void testSendMessageWithKeyboardSuccess() throws TelegramApiException {
    Long chatId = 555L;
    String text = "Test message with keyboard";
    ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

    TelegramBotService realBotService = new TelegramBotService(recommendationService, userNameResolver, batchAnalysisService);
    ReflectionTestUtils.setField(realBotService, "botToken", "test_token");
    ReflectionTestUtils.setField(realBotService, "botUsername", "test_bot");
    ReflectionTestUtils.setField(realBotService, "botEnabled", true);

    TelegramBotService spyBot = spy(realBotService);
    Message mockMessage = mock(Message.class);
    doReturn(mockMessage).when(spyBot).execute(any(SendMessage.class));

    spyBot.sendMessageWithKeyboard(chatId, text, keyboard);

    verify(spyBot).execute(argThat((SendMessage sendMessage) ->
        sendMessage.getChatId().equals(chatId.toString()) &&
            sendMessage.getText().equals(text) &&
            sendMessage.getReplyMarkup() == keyboard
    ));
  }

  @Test
  void testSendMessageHandlesException() throws TelegramApiException {
    Long chatId = 999L;
    String text = "Message";

    TelegramBotService realBotService = new TelegramBotService(recommendationService, userNameResolver, batchAnalysisService);
    ReflectionTestUtils.setField(realBotService, "botToken", "test_token");
    ReflectionTestUtils.setField(realBotService, "botUsername", "test_bot");
    ReflectionTestUtils.setField(realBotService, "botEnabled", true);

    TelegramBotService spyBot = spy(realBotService);
    doThrow(new TelegramApiException("API failure")).when(spyBot).execute(any(SendMessage.class));

    assertDoesNotThrow(() -> spyBot.sendMessage(chatId, text));
    verify(spyBot).execute(any(SendMessage.class));
  }

  // ===== ПРОСТЫЕ ТЕСТЫ НА РАЗНЫЕ ВХОДНЫЕ ДАННЫЕ =====

  @Test
  void testHandlePlainUsernameFound() {
    Update update = createUpdateWithMessage("alex", 123L);
    UUID userId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

    when(userNameResolver.resolveUserId("alex")).thenReturn(userId);
    when(userNameResolver.getUserFullName(userId)).thenReturn("Алексей Чудинов");
    when(recommendationService.getRecommendations(userId))
        .thenReturn(new RecommendationResponse(userId, List.of(
            new ProductRecommendation("Invest 500", UUID.randomUUID(), "Описание Invest 500")
        )));

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        argThat(msg -> msg.contains("Алексей Чудинов") && msg.contains("Invest 500")),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandlePlainUsernameNotFound() {
    Update update = createUpdateWithMessage("nonexistent", 123L);
    when(userNameResolver.resolveUserId("nonexistent")).thenReturn(null);

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        contains("Неизвестная команда"),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandlePlainUUID() {
    String uuidString = "cd515076-5d8a-44be-930e-8d4fcb79f42d";
    Update update = createUpdateWithMessage(uuidString, 123L);
    UUID userId = UUID.fromString(uuidString);

    when(userNameResolver.getUserFullName(userId)).thenReturn("Test User");
    when(recommendationService.getRecommendations(userId))
        .thenReturn(new RecommendationResponse(userId, List.of(
            new ProductRecommendation("Product1", UUID.randomUUID(), "Description1")
        )));

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        contains("Test User"),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleCallbackQuery() {
    Update update = createCallbackUpdate("recommend_cd515076-5d8a-44be-930e-8d4fcb79f42d", 123L);
    UUID userId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

    when(userNameResolver.getUserFullName(userId)).thenReturn("Test User");
    when(recommendationService.getRecommendations(userId))
        .thenReturn(new RecommendationResponse(userId, List.of()));

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessageWithKeyboard(eq(123L), anyString(), any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleInvalidCallback() {
    Update update = createCallbackUpdate("invalid_callback_data", 123L);
    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService, never()).sendMessageWithKeyboard(anyLong(), anyString(), any());
  }

  // ===== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====

  private Update createUpdateWithMessage(String text, Long chatId) {
    Update update = new Update();
    Message message = new Message();
    Chat chat = new Chat();

    chat.setId(chatId);
    message.setChat(chat);
    message.setText(text);
    update.setMessage(message);

    return update;
  }

  private Update createCallbackUpdate(String callbackData, Long chatId) {
    Update update = new Update();
    CallbackQuery callbackQuery = new CallbackQuery();
    Message message = new Message();
    Chat chat = new Chat();

    chat.setId(chatId);
    message.setChat(chat);
    callbackQuery.setData(callbackData);
    callbackQuery.setMessage(message);
    update.setCallbackQuery(callbackQuery);

    return update;
  }
}