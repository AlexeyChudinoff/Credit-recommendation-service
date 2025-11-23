package com.bank.star.service;

import com.bank.star.dto.ProductRecommendation;
import com.bank.star.dto.RecommendationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
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

  @Captor
  private ArgumentCaptor<SendMessage> sendMessageCaptor;

  @BeforeEach
  void setup() throws TelegramApiException {
    MockitoAnnotations.openMocks(this);
    telegramBotService = spy(new TelegramBotService(recommendationService, userNameResolver));
    // Правильно мокируем execute - он не void, возвращаем null при вызове
    doAnswer(invocation -> null).when(telegramBotService).execute(any(SendMessage.class));
  }

  @Test
  void testGetBotUsernameAndToken() {
    assertNotNull(telegramBotService.getBotUsername());
    assertNotNull(telegramBotService.getBotToken());
  }

  @Test
  void testHandleStartCommand_sendsHelpMessage() throws TelegramApiException {
    Update update = createUpdateWithMessage("/start", 123L);

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendHelpMessage(123L);
    verify(telegramBotService).execute(sendMessageCaptor.capture());
    SendMessage sent = sendMessageCaptor.getValue();
    assertTrue(sent.getText().contains("Bank Star Recommendation Bot"));
  }

  @Test
  void testHandleRecommendCommand_userNotFound() throws TelegramApiException {
    Update update = createUpdateWithMessage("/recommend unknownuser", 123L);

    when(userNameResolver.resolveUserId("unknownuser")).thenReturn(null);

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessage(eq(123L), eq("❌ Пользователь не найден"));
  }

  @Test
  void testHandleRecommendCommand_returnsRecommendations() throws TelegramApiException {
    Update update = createUpdateWithMessage("/recommend testuser", 123L);

    UUID userId = UUID.randomUUID();
    UUID productId1 = UUID.randomUUID();
    UUID productId2 = UUID.randomUUID();

    when(userNameResolver.resolveUserId("testuser")).thenReturn(userId);
    when(userNameResolver.getUserFullName(userId)).thenReturn("Test User");
    when(recommendationService.getRecommendations(userId))
        .thenReturn(new RecommendationResponse(userId, List.of(
            new ProductRecommendation("Product1", productId1, "Description1"),
            new ProductRecommendation("Product2", productId2, "Description2")
        )));

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendMessage(eq(123L), argThat(msg -> msg.contains("Здравствуйте, Test User")));
    verify(telegramBotService).sendMessage(eq(123L), argThat(msg -> msg.contains("Новыe продукты для вас")));
  }

  @Test
  void testHandleUnknownCommand_sendsUnknownCommandMessage() throws TelegramApiException {
    Update update = createUpdateWithMessage("/unknown", 321L);

    telegramBotService.onUpdateReceived(update);

    verify(telegramBotService).sendUnknownCommandMessage(321L);
  }

  @Test
  void testSendMessage_executesSendMessageSuccessfully() throws TelegramApiException {
    Long chatId = 555L;
    String text = "Test message";

    telegramBotService.sendMessage(chatId, text);

    verify(telegramBotService).execute(sendMessageCaptor.capture());
    SendMessage sent = sendMessageCaptor.getValue();

    assertEquals(chatId.toString(), sent.getChatId());
    assertEquals(text, sent.getText());
  }

  @Test
  void testSendMessage_handlesTelegramApiException() throws TelegramApiException {
    Long chatId = 999L;
    String text = "Message";

    doThrow(new TelegramApiException("API failure")).when(telegramBotService).execute(any(SendMessage.class));

    telegramBotService.sendMessage(chatId, text);

    // Проверяем вызов execute, исключение обработано внутри sendMessage
    verify(telegramBotService).execute(any(SendMessage.class));
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
}
