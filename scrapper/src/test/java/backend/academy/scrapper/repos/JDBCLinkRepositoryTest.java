package backend.academy.scrapper.repos;

import backend.academy.common.Link;
import backend.academy.scrapper.DataBaseMigrator;
import backend.academy.scrapper.TestsBeansContainersConfiguration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
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
class JDBCLinkRepositoryTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcClient jdbcClient;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    private LinkRepository linkRepository;

    @BeforeEach
    void setUp() {
        DataBaseMigrator.runLiquibaseMigration(dataSource);
        linkRepository = new JDBCLinkRepository(jdbcClient);
    }

    @Test
    void findAllUsers() {
        Long chatId = 1231213L;
        jdbcClient
                .sql("INSERT INTO chat (chat_id) VALUES (:chatId)")
                .param("chatId", chatId)
                .update();
        Set<Long> chats = linkRepository.findAllUsers();

        Assertions.assertEquals(chatId, chats.iterator().next());
    }

    @Test
    void addLinkThanFind() {
        long chatId = 1231213L;
        String url = "github.com";
        LocalDateTime dateTime = LocalDateTime.parse(LocalDateTime.now().format(formatter));
        Optional<Long> userId = linkRepository.addChat(chatId);
        linkRepository.addLink(userId.orElseThrow(), url, dateTime);
        Link expected = linkRepository
                .findLinksByUserId(userId.orElseThrow())
                .iterator()
                .next();
        Assertions.assertEquals(url, expected.url());
        Assertions.assertEquals(dateTime, expected.updatedAt());
    }

    @Test
    void findLinksByUserIdAndTag() {
        long chatId = 1231213L;
        String url = "github.com";
        String tag = "study";
        LocalDateTime dateTime = LocalDateTime.parse(LocalDateTime.now().format(formatter));
        Optional<Long> userId = linkRepository.addChat(chatId);
        Optional<Long> linkId = linkRepository.addLink(userId.orElseThrow(), url, dateTime);
        Optional<Long> tagId = linkRepository.addTag(tag);

        linkRepository.addLinkTag(linkId.orElseThrow(), tagId.orElseThrow());

        Link actual = linkRepository
                .findLinksByUserIdAndTag(userId.orElseThrow(), tagId.orElseThrow())
                .iterator()
                .next();

        Assertions.assertEquals(url, actual.url());
        Assertions.assertEquals(dateTime, actual.updatedAt());
    }

    @Test
    void addThanDeleteLink() {
        long chatId = 1231214L;
        String url = "github.com";
        String tag = "study";
        LocalDateTime dateTime = LocalDateTime.parse(LocalDateTime.now().format(formatter));
        Optional<Long> userId = linkRepository.addChat(chatId);

        Optional<Long> linkId = linkRepository.addLink(userId.orElseThrow(), url, dateTime);
        Optional<Long> tagId = linkRepository.addTag(tag);

        linkRepository.addLinkTag(linkId.orElseThrow(), tagId.orElseThrow());

        linkRepository.deleteLinkTag(linkId.orElseThrow(), tagId.orElseThrow());
        linkRepository.deleteLink(linkId.orElseThrow());

        Set<Link> actual = linkRepository.findLinksByUserIdAndTag(userId.orElseThrow(), tagId.orElseThrow());

        Assertions.assertTrue(actual.isEmpty());
    }
}
