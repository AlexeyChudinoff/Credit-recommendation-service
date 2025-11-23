package com.bank.star.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserNameResolverTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  private UserNameResolver userNameResolver;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    userNameResolver = new UserNameResolver(jdbcTemplate);
  }

  @Test
  void resolveUserId_returnsUUID_whenUsernameFound() {
    UUID expectedUuid = UUID.randomUUID();
    when(jdbcTemplate.queryForObject(
        anyString(), eq(String.class), anyString(), anyString()))
        .thenReturn(expectedUuid.toString());

    UUID result = userNameResolver.resolveUserId("testuser");

    assertNotNull(result);
    assertEquals(expectedUuid, result);
  }

  @Test
  void resolveUserId_returnsNull_whenNoUserFound() {
    when(jdbcTemplate.queryForObject(
        anyString(), eq(String.class), anyString(), anyString()))
        .thenThrow(new RuntimeException("No data found"));

    UUID result = userNameResolver.resolveUserId("unknown");

    assertNull(result);
  }

  @Test
  void getUserFullName_returnsFullName_whenUserExists() {
    String fullName = "John Doe";
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class), anyString()))
        .thenReturn(fullName);

    String result = userNameResolver.getUserFullName(UUID.randomUUID());

    assertEquals(fullName, result);
  }

  @Test
  void getUserFullName_returnsDefaultMessage_whenErrorOccurs() {
    when(jdbcTemplate.queryForObject(anyString(), eq(String.class), anyString()))
        .thenThrow(new RuntimeException("DB error"));

    String result = userNameResolver.getUserFullName(UUID.randomUUID());

    assertEquals("Уважаемый клиент", result);
  }
}
