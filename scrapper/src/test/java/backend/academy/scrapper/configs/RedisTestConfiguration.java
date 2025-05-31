package backend.academy.scrapper.configs;

import backend.academy.scrapper.repos.LinkRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RedisTestConfiguration {
    @Bean
    LinkRepository linkRepository() {
        return Mockito.mock(LinkRepository.class);
    }
}
