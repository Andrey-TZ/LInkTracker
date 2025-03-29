package backend.academy.scrapper.sevices;

import backend.academy.common.Link;
import backend.academy.scrapper.repos.JDBCLinkRepository;
import backend.academy.scrapper.repos.LinkRepository;
import backend.academy.scrapper.services.link.JDBCLinkService;
import backend.academy.scrapper.services.link.LinkService;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JDBCLinkServiceTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("scrapper_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.sql");
    }

    @Autowired
    private JdbcClient jdbcClient;

    private LinkRepository repository;
    private LinkService linkService;

    @BeforeEach
    void setUp() {
        repository = new JDBCLinkRepository(jdbcClient);
        linkService = new JDBCLinkService(repository);
    }

    @Test
    void addUserThanFind() {
        long chatId = 525252L;
        linkService.addUser(chatId);
        Long actual = linkService.getUsers().iterator().next();

        Assertions.assertEquals(chatId, actual);
    }

    @Test
    void addLinkThanFind() {
        long chatId = 525252L;
        linkService.addUser(chatId);
        Link link = new Link("github.com", new String[] {"work", "job"});
        linkService.addLink(chatId, link);

        Link actual = linkService.getLinks(chatId).iterator().next();

        Assertions.assertEquals(link, actual);
    }

    @Test
    void addLinkThanDelete() {
        long chatId = 525252L;
        linkService.addUser(chatId);
        Link link = new Link("github.com", new String[] {"work", "job"});
        linkService.addLink(chatId, link);

        linkService.deleteLink(chatId, link);

        Set<Link> actual = linkService.getLinks(chatId);

        Assertions.assertTrue(actual.isEmpty());
    }
}
