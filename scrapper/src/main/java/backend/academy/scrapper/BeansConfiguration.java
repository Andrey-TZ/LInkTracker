package backend.academy.scrapper;

import backend.academy.scrapper.clients.APIClient;
import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.repos.JPALinkRepository;
import backend.academy.scrapper.repos.JPATagRepository;
import backend.academy.scrapper.repos.JPAUserRepository;
import backend.academy.scrapper.repos.LinkRepository;
import backend.academy.scrapper.services.link.JDBCLinkService;
import backend.academy.scrapper.services.link.JPALinkService;
import backend.academy.scrapper.services.link.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableJpaRepositories
@EnableScheduling
@Configuration
public class BeansConfiguration {
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
        return new StackOverflowClient(
                scrapperConfig.stackOverflow().accessToken(),
                scrapperConfig.stackOverflow().key(),
                botClient);
    }

    @Bean(name = "jdbcLinkService")
    @ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
    public LinkService jdbcLinkService(LinkRepository linkRepository) {
        return new JDBCLinkService(linkRepository);
    }

    @Bean(name = "jpaLinkService")
    @ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
    public LinkService jpaLinkService(
            JPAUserRepository userRepository, JPALinkRepository linkRepository, JPATagRepository tagRepository) {
        return new JPALinkService(userRepository, tagRepository, linkRepository);
    }
}
