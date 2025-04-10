package backend.academy.scrapper;

import backend.academy.scrapper.clients.APIClient;
import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableScheduling
@Configuration
public class AppConfig {
    @Bean
    public ThreadPoolTaskScheduler taskScheduler(@Autowired ScrapperConfig scrapperConfig) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(scrapperConfig.scheduler().threads()); // Количество потоков
        scheduler.setThreadNamePrefix("MyScheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public APIClient gitHubClient(@Autowired ScrapperConfig scrapperConfig, @Autowired BotClient botClient) {
        return new GitHubClient(scrapperConfig.githubToken(), botClient);
    }

    @Bean
    public APIClient stackOverflowClient(@Autowired ScrapperConfig scrapperConfig, @Autowired BotClient botClient) {
        return new StackOverflowClient(scrapperConfig.stackOverflow().accessToken(), scrapperConfig.stackOverflow().key(), botClient);
    }
}
