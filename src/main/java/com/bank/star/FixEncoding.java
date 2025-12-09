package com.bank.star;
import java.nio.file.*;
import java.util.*;

public class FixEncoding {
  public static void main(String[] args) throws Exception {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("Используем", "Используем");
    replacements.put("Инвестиционные", "Инвестиционные");
    replacements.put("ИИС", "ИИС");
    replacements.put("Инвестиционный", "Инвестиционный");
    replacements.put("ИЗМЕНИЛИ", "ИЗМЕНИЛИ");
    replacements.put("Извлекаем", "Извлекаем");
    replacements.put("Инициализация", "Инициализация");
    replacements.put("Исправленный", "Исправленный");
    replacements.put("Используйте", "Используйте");
    replacements.put("Имя", "Имя");
    replacements.put("Или", "Или");
    replacements.put("Информация", "Информация");
    replacements.put("Интеграция", "Интеграция");
    replacements.put("ДИАГНОСТИКА", "ДИАГНОСТИКА");
    replacements.put("ТИПОВ", "ТИПОВ");
    replacements.put("ТРАНЗАКЦИЙ", "ТРАНЗАКЦИЙ");
    replacements.put("ПРАВИЛ", "ПРАВИЛ");
    replacements.put("ОШИБКА", "ОШИБКА");
    replacements.put("ОШИБКИ", "ОШИБКИ");
    replacements.put("ИТОГО", "ИТОГО");
    replacements.put("Или", "Или");

    System.out.println("Fixing encoding in all Java files...");

    Files.walk(Paths.get("src"))
        .filter(p -> p.toString().endsWith(".java"))
        .forEach(p -> fixFile(p, replacements));

    System.out.println("Все файлы исправлены!");
  }

  static void fixFile(Path file, Map<String, String> replacements) {
    try {
      String content = new String(Files.readAllBytes(file), "UTF-8");

      for (Map.Entry<String, String> entry : replacements.entrySet()) {
        content = content.replace(entry.getKey(), entry.getValue());
      }

      // Удаляем оставшиеся 
      content = content.replace("", "");

      Files.write(file, content.getBytes("UTF-8"));
      System.out.println("✓ Исправлен: " + file.getFileName());
    } catch (Exception e) {
      System.err.println("✗ Ошибка в " + file + ": " + e.getMessage());
    }
  }
}