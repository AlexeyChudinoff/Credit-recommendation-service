//управление профилями Spring
package com.bank.star.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class ProfileController {

  private final Environment environment;

  @Value("${spring.profiles.active:default}")
  private String activeProfile;

  @Value("${spring.application.name:unknown}")
  private String appName;

  public ProfileController(Environment environment) {
    this.environment = environment;
  }

  @GetMapping("/profile")
  public String getActiveProfile() {
    return "Active profile: " + activeProfile;
  }

  @GetMapping("/profile/details")
  public ProfileInfo getProfileDetails() {
    String[] activeProfiles = environment.getActiveProfiles();
    String[] defaultProfiles = environment.getDefaultProfiles();

    return new ProfileInfo(
        appName,
        activeProfile,
        Arrays.asList(activeProfiles),
        Arrays.asList(defaultProfiles)
    );
  }

  // DTO с Lombok аннотациями
  @Getter
  @AllArgsConstructor
  public static class ProfileInfo {
    private final String applicationName;
    private final String activeProfile;
    private final List<String> activeProfiles;
    private final List<String> defaultProfiles;
  }
}