package com.bank.star.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BuildInfoController {

  private final BuildProperties buildProperties;

  public BuildInfoController(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  @GetMapping("/build-info")
  public Map<String, String> getBuildInfo() {
    return Map.of(
        "name", buildProperties.getName(),
        "version", buildProperties.getVersion(),
        "time", buildProperties.getTime().toString(),
        "artifact", buildProperties.getArtifact(),
        "group", buildProperties.getGroup()
    );
  }

  @GetMapping("/version")
  public String getVersion() {
    return String.format("🏦 Bank Star Recommendation Service v%s",
        buildProperties.getVersion());
  }
}