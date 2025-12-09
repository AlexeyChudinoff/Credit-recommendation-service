package com.bank.star.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.Optional;

@RestController
public class BuildInfoController {

  private final Optional<BuildProperties> buildProperties;

  //конструктор для работы с Optional
  public BuildInfoController(Optional<BuildProperties> buildProperties) {
    this.buildProperties = buildProperties;
  }

  @GetMapping("/build-info")
  public Map<String, String> getBuildInfo() {
    return buildProperties.map(props -> Map.of(
        "name", props.getName(),
        "version", props.getVersion(),
        "time", props.getTime().toString(),
        "artifact", props.getArtifact(),
        "group", props.getGroup()
    )).orElse(Map.of(
        "name", "recommendation-service",
        "version", "unknown",
        "time", "unknown",
        "artifact", "unknown",
        "group", "unknown"
    ));
  }

  @GetMapping("/version")
  public String getVersion() {
    String version = buildProperties.map(BuildProperties::getVersion)
        .orElse("unknown");
    return String.format("🏦 Bank Star Recommendation Service v%s", version);
  }
}