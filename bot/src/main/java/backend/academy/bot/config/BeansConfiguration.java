package backend.academy.bot.config;

import backend.academy.bot.CommandProcessor;
import backend.academy.bot.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BeansConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "scrapper", ignoreUnknownFields = false)
    public WebClient webClient(@Autowired BotConfig botConfig) {
        return WebClient.builder().baseUrl(botConfig.scrapperURL()).build();
    }

    @Bean
    public TelegramBotService telegramBotService(BotConfig botConfig, CommandProcessor commandProcessor) {
        return new TelegramBotService(botConfig.telegramToken(), commandProcessor);
    }
}
