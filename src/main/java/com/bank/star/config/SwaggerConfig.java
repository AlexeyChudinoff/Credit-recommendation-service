//настройка Swagger/документация OpenAPI с тремя серверами (dev, test, prod)
package com.bank.star.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("🏦 Bank Star Recommendation Service API")
            .description("""
                ## Микросервис рекомендаций банковских продуктов
                
                ### 📊 Обзор системы
                REST API для интеллектуальной рекомендации финансовых продуктов 
                на основе анализа транзакционного поведения клиентов банка «Стар».
                
                ### 🎯 Основные возможности:
                - **Персонализированные рекомендации** продуктов на основе финансового поведения
                - **Real-time анализ** транзакционной активности
                - **Интеграция** с мобильным приложением и личным кабинетом
                - **Три алгоритма** рекомендаций с бизнес-правилами
                
                ### 🔐 Безопасность:
                - API предназначен для внутреннего использования
                - Валидация входных параметров
                - Логирование всех операций
                
                ### 📋 Тестирование:
                Используйте тестовые UUID пользователей для проверки работы API.
                """)
            .version("1.0.0")
            .contact(new Contact()
                .name("Bank Star Development Team")
                .email("development@bankstar.com")
                .url("https://bankstar.com/tech"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT")))
        .servers(List.of(
            new Server()
                .url("http://localhost:8080")
                .description("🚀 Локальная среда разработки"),
            new Server()
                .url("https://api-dev.bankstar.com")
                .description("🧪 Тестовая среда"),
            new Server()
                .url("https://api.bankstar.com")
                .description("🏢 Продакшен среда")
        ));
  }
}