package backend.academy.scrapper.sevices;

import backend.academy.common.Link;
import backend.academy.scrapper.DataBaseMigrator;
import backend.academy.scrapper.TestsBeansContainersConfiguration;
import backend.academy.scrapper.repos.JPALinkRepository;
import backend.academy.scrapper.repos.JPATagRepository;
import backend.academy.scrapper.repos.JPAUserRepository;
import backend.academy.scrapper.services.link.JPALinkService;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestPropertySource(properties = {"app.access-type:ORM"})
@Testcontainers
@DataJpaTest
@Import(TestsBeansContainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JPALinkServiceTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JPAUserRepository userRepository;

    @Autowired
    private JPALinkRepository linkRepository;

    @Autowired
    private JPATagRepository tagRepository;

    private JPALinkService linkService;

    @BeforeEach
    void setUp() {
        DataBaseMigrator.runLiquibaseMigration(dataSource);
        linkService = new JPALinkService(userRepository, tagRepository, linkRepository);
    }

    @Test
    void addUserThanFind() {
        // Arrange
        long chatId = 525252L;
        linkService.addUser(chatId);

        // Act
        Long actual = linkService.getUsers().iterator().next();

        // Assert
        Assertions.assertEquals(chatId, actual);
    }

    @Test
    void addLinkThanFind() {
        // Arrange
        long chatId = 525252L;
        linkService.addUser(chatId);
        Link link = new Link("github.com", new String[] {"work", "job"});
        linkService.addLink(chatId, link);

        // Act
        Link actual = linkService.getLinks(chatId).iterator().next();

        // Assert
        Assertions.assertEquals(link, actual);
    }

    @Test
    void addLinkThanDelete() {
        // Arrange
        long chatId = 525252L;
        linkService.addUser(chatId);
        Link link = new Link("github.com", new String[] {"work", "job"});
        linkService.addLink(chatId, link);

        // Act
        linkService.deleteLink(chatId, link);

        // Assert
        Set<Link> actual = linkService.getLinks(chatId);
        Assertions.assertTrue(actual.isEmpty());
    }
}
