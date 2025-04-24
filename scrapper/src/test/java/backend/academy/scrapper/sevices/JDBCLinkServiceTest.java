package backend.academy.scrapper.sevices;

import backend.academy.common.Link;
import backend.academy.scrapper.TestsBeansContainersConfiguration;
import backend.academy.scrapper.repos.JDBCLinkRepository;
import backend.academy.scrapper.services.link.JDBCLinkService;
import backend.academy.scrapper.services.link.LinkService;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestPropertySource(properties = {"app.access-type:SQL"})
@Testcontainers
@JdbcTest
@Import(TestsBeansContainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JDBCLinkServiceTest {

    @Autowired
    private JdbcClient jdbcClient;

    private JDBCLinkRepository repository;

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
