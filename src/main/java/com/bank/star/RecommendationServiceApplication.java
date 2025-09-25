package com.bank.star;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecommendationServiceApplication {

  private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceApplication.class);

  public static void main(String[] args) {
    logger.info("Starting Bank Star Recommendation Service...");
    SpringApplication.run(RecommendationServiceApplication.class, args);
    logger.info("Recommendation Service started successfully!");
  }
}