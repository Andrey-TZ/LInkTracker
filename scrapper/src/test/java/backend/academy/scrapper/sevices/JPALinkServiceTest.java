package backend.academy.scrapper.sevices;

import backend.academy.common.Link;
import backend.academy.scrapper.TestsBeansContainersConfiguration;
import backend.academy.scrapper.repos.JPALinkRepository;
import backend.academy.scrapper.repos.JPATagRepository;
import backend.academy.scrapper.repos.JPAUserRepository;
import backend.academy.scrapper.services.link.JPALinkService;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@Import(TestsBeansContainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class JPALinkServiceTest {
    @Autowired
    private JPAUserRepository userRepository;

    @Autowired
    private JPALinkRepository linkRepository;

    @Autowired
    private JPATagRepository tagRepository;

    private JPALinkService linkService;

    @BeforeEach
    void setUp() {
        linkService = new JPALinkService(userRepository, tagRepository, linkRepository);
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
