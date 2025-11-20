package com.bank.star.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNameResolverTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  private UserNameResolver userNameResolver;

  private final UUID testUserId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");
  private final String testUsername = "ivanov";
  private final String testFullName = "Иван Иванов";

  @BeforeEach
  void setUp() {
    userNameResolver = new UserNameResolver(jdbcTemplate);
  }

  @Test
  void resolveUserId_withExistingUsername_shouldReturnUserId() {
    // Given
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(testUsername), eq("%" + testUsername + "%")))
        .thenReturn(testUserId.toString());

    // When
    UUID result = userNameResolver.resolveUserId(testUsername);

    // Then
    assertEquals(testUserId, result);
  }

  @Test
  void resolveUserId_withNonExistentUsername_shouldReturnNull() {
    // Given
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class), anyString(), anyString()))
        .thenReturn(null);

    // When
    UUID result = userNameResolver.resolveUserId("nonexistent");

    // Then
    assertNull(result);
  }

  @Test
  void getUserFullName_withExistingUserId_shouldReturnFullName() {
    // Given
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(testUserId.toString())))
        .thenReturn(testFullName);

    // When
    String result = userNameResolver.getUserFullName(testUserId);

    // Then
    assertEquals(testFullName, result);
  }

  @Test
  void getUserFullName_withNonExistentUserId_shouldReturnDefault() {
    // Given
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class), anyString()))
        .thenReturn(null);

    // When
    String result = userNameResolver.getUserFullName(UUID.randomUUID());

    // Then
    assertEquals("Уважаемый клиент", result);
  }
}