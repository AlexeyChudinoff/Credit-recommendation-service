package com.bank.star;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class RecommendationServiceApplication {

  private static final Logger logger = LoggerFactory.getLogger(
      RecommendationServiceApplication.class);

  @Value("${spring.datasource.url:jdbc:h2:file:./data/transaction.mv.db}")
  private String databaseUrl;

  public static void main(String[] args) {
    SpringApplication.run(RecommendationServiceApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void checkDatabasePath() {
    logger.info("🔍 Детальная проверка пути к базе данных...");
    logger.info("Database URL: {}", databaseUrl);

    try {
      // Извлекаем путь к файлу из URL и добавляем расширение .mv.db
      String filePath = databaseUrl.replace("jdbc:h2:file:", "").split(";")[0] + ".mv.db";
      File dbFile = new File(filePath);

      logger.info("Полный путь к файлу: {}", dbFile.getAbsolutePath());
      logger.info("Файл существует: {}", dbFile.exists());
      logger.info("Это файл: {}", dbFile.isFile());
      logger.info("Это директория: {}", dbFile.isDirectory());
      logger.info("Размер файла: {} байт", dbFile.length());
      logger.info("Можно читать: {}", dbFile.canRead());

      // Проверим родительскую директорию
      File parentDir = dbFile.getParentFile();
      logger.info("Родительская директория: {}", parentDir.getAbsolutePath());
      logger.info("Директория существует: {}", parentDir.exists());

      if (parentDir.exists()) {
        logger.info("Содержимое папки data:");
        File[] files = parentDir.listFiles();
        if (files != null) {
          for (File file : files) {
            logger.info("   - {} ({} байт, файл: {})",
                file.getName(), file.length(), file.isFile());
          }
        }
      }

    } catch (Exception e) {
      logger.error("❌ Ошибка при проверке пути к базе данных: {}", e.getMessage(), e);
    }
  }

}//