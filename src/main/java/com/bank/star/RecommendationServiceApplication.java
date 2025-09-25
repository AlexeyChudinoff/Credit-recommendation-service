package com.bank.star;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.io.File;

@SpringBootApplication
public class RecommendationServiceApplication {

  private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceApplication.class);

  @Value("${spring.datasource.url:jdbc:h2:file:./data/transaction.mv.db}")
  private String databaseUrl;

  public static void main(String[] args) {
    SpringApplication.run(RecommendationServiceApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void checkDatabasePath() {
    logger.info("🔍 Проверка подключения к базе данных...");
    logger.info("Database URL: {}", databaseUrl);

    try {
      // Извлекаем путь к файлу из URL
      String filePath = databaseUrl.replace("jdbc:h2:file:", "").split(";")[0];
      File dbFile = new File(filePath);

      logger.info("Путь к файлу базы: {}", dbFile.getAbsolutePath());
      logger.info("Файл базы существует: {}", dbFile.exists());

      if (dbFile.exists()) {
        logger.info("✅ База данных успешно найдена! Размер: {} байт", dbFile.length());
      } else {
        logger.error("❌ Файл базы данных не найден! Проверьте путь: {}", filePath);
        logger.info("💡 Текущая рабочая директория: {}", System.getProperty("user.dir"));

        // Покажем, где ищем файл
        File currentDir = new File(".");
        logger.info("💡 Содержимое текущей директории:");
        File[] files = currentDir.listFiles();
        if (files != null) {
          for (File file : files) {
            logger.info("   - {} ({})", file.getName(),
                file.isDirectory() ? "dir" : "file");
          }
        }
      }

    } catch (Exception e) {
      logger.error("❌ Ошибка при проверке пути к базе данных: {}", e.getMessage());
    }
  }
}