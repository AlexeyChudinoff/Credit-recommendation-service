package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TelegramBotServiceTest {

  @Mock
  private RecommendationService recommendationService;

  @Mock
  private UserNameResolver userNameResolver;

  @InjectMocks
  private TelegramBotService telegramBotService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);

    ReflectionTestUtils.setField(telegramBotService, "botToken", "test_token");
    ReflectionTestUtils.setField(telegramBotService, "botUsername", "test_bot");
    ReflectionTestUtils.setField(telegramBotService, "botEnabled", true);

    telegramBotService = spy(telegramBotService);

    doNothing().when(telegramBotService).sendMessage(anyLong(), anyString());
    doNothing().when(telegramBotService).sendMessageWithKeyboard(anyLong(), anyString(), any(ReplyKeyboardMarkup.class));
    doNothing().when(telegramBotService).sendMessageWithInlineKeyboard(anyLong(), anyString(), any(InlineKeyboardMarkup.class));
    doNothing().when(telegramBotService).sendHelpMessage(anyLong());
    doNothing().when(telegramBotService).sendTestUsersInfo(anyLong());
    doNothing().when(telegramBotService).sendUnknownCommandMessage(anyLong());
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

  @Test
  void testHandleStartCommand() {
    Update update = createUpdateWithMessage("/start", 123L);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendHelpMessage(123L);
  }

  @Test
  void testHandleHelpCommand() {
    Update update = createUpdateWithMessage("/help", 123L);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendHelpMessage(123L);
  }

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

  @Test
  void testHandleQuickRecommendInvest500() {
    Update update = createUpdateWithMessage("💎 Invest 500", 123L);
    UUID userId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

    when(userNameResolver.getUserFullName(userId)).thenReturn("Invest User");
    when(recommendationService.getRecommendations(userId))
        .thenReturn(new RecommendationResponse(userId, List.of(
            new ProductRecommendation("Invest 500", UUID.randomUUID(), "Описание Invest 500")
        )));

    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessageWithKeyboard(eq(123L),
        contains("Invest 500"),
        any(ReplyKeyboardMarkup.class));
  }

  @Test
  void testHandleTestUsersCommand() {
    Update update = createUpdateWithMessage("/testusers", 123L);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendTestUsersInfo(123L);
  }

  @Test
  void testHandleUnknownCommand() {
    Update update = createUpdateWithMessage("/unknown", 321L);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendUnknownCommandMessage(321L);
  }

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
    verify(telegramBotService).sendUnknownCommandMessage(123L);
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
  void testHandleUsernameOrUnknownWithException() {
    Update update = createUpdateWithMessage("erroruser", 123L);
    when(userNameResolver.resolveUserId("erroruser"))
        .thenThrow(new RuntimeException("Database error"));

    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService).sendMessage(eq(123L),
        contains("❌ Произошла ошибка при поиске пользователя"));
  }

  @Test
  void testHandleInvalidUUID() {
    Update update = createUpdateWithMessage("/recommend invalid-uuid", 123L);
    telegramBotService.onUpdateReceived(update);

    // Проверяем что отправляется сообщение об ошибке (любое сообщение)
    verify(telegramBotService).sendMessageWithKeyboard(eq(123L), anyString(), any(ReplyKeyboardMarkup.class));
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
  void testSendMessage() throws TelegramApiException {
    Long chatId = 555L;
    String text = "Test message";

    TelegramBotService realBotService = new TelegramBotService(recommendationService, userNameResolver);
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
  void testSendMessageWithKeyboard() throws TelegramApiException {
    Long chatId = 555L;
    String text = "Test message with keyboard";
    ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

    TelegramBotService realBotService = new TelegramBotService(recommendationService, userNameResolver);
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

    TelegramBotService realBotService = new TelegramBotService(recommendationService, userNameResolver);
    ReflectionTestUtils.setField(realBotService, "botToken", "test_token");
    ReflectionTestUtils.setField(realBotService, "botUsername", "test_bot");
    ReflectionTestUtils.setField(realBotService, "botEnabled", true);

    TelegramBotService spyBot = spy(realBotService);
    doThrow(new TelegramApiException("API failure")).when(spyBot).execute(any(SendMessage.class));

    assertDoesNotThrow(() -> spyBot.sendMessage(chatId, text));
    verify(spyBot).execute(any(SendMessage.class));
  }

  @Test
  void testFormatRecommendationsEmptyList() {
    TelegramBotService realBotService = new TelegramBotService(recommendationService, userNameResolver);
    String fullName = "Test User";
    RecommendationResponse response = new RecommendationResponse(UUID.randomUUID(), List.of());

    String result = invokePrivateFormatRecommendations(realBotService, fullName, response);

    assertTrue(result.contains("Здравствуйте"));
    assertTrue(result.contains("Test User"));
    assertTrue(result.contains("К сожалению, у нас пока нет подходящих продуктов"));
  }

  @Test
  void testFormatRecommendationsWithProducts() {
    TelegramBotService realBotService = new TelegramBotService(recommendationService, userNameResolver);
    String fullName = "Test User";
    UUID userId = UUID.randomUUID();
    RecommendationResponse response = new RecommendationResponse(userId, List.of(
        new ProductRecommendation("Product1", UUID.randomUUID(), "Description1"),
        new ProductRecommendation("Product2", UUID.randomUUID(), "Description2")
    ));

    String result = invokePrivateFormatRecommendations(realBotService, fullName, response);

    assertTrue(result.contains("Здравствуйте"));
    assertTrue(result.contains("Test User"));
    assertTrue(result.contains("Новые продукты"));
    assertTrue(result.contains("Product1"));
    assertTrue(result.contains("Product2"));
    assertTrue(result.contains("Description1"));
    assertTrue(result.contains("Description2"));
  }

  @Test
  void testHandleInvalidCallback() {
    Update update = createCallbackUpdate("invalid_callback_data", 123L);
    telegramBotService.onUpdateReceived(update);
    verify(telegramBotService, never()).sendMessageWithKeyboard(anyLong(), anyString(), any());
  }

  // Вспомогательные методы
  private String invokePrivateFormatRecommendations(TelegramBotService bot, String fullName, RecommendationResponse response) {
    try {
      var method = TelegramBotService.class.getDeclaredMethod("formatRecommendations", String.class, RecommendationResponse.class);
      method.setAccessible(true);
      return (String) method.invoke(bot, fullName, response);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke private method", e);
    }
  }

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