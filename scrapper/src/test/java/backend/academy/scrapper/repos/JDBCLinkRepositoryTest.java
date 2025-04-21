package backend.academy.scrapper.repos;

import backend.academy.common.Link;
import backend.academy.scrapper.TestsBeansContainersConfiguration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@JdbcTest
@Import(TestsBeansContainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JDBCLinkRepositoryTest {

    //    @Container
    //    @ServiceConnection
    //    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
    //            .withDatabaseName("scrapper_test")
    //            .withUsername("test")
    //            .withPassword("test");
    //
    //    @DynamicPropertySource
    //    static void configureProperties(DynamicPropertyRegistry registry) {
    //        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    //        registry.add("spring.datasource.username", postgres::getUsername);
    //        registry.add("spring.datasource.password", postgres::getPassword);
    //        registry.add("spring.liquibase.enabled", () -> "true");
    //        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.sql");
    //    }

    @Autowired
    private JdbcClient jdbcClient;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    private LinkRepository linkRepository;

    @BeforeEach
    void setUp() {
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
        if (userId.isPresent()) {
            linkRepository.addLink(userId.get(), url, dateTime);
            Link expected =
                    linkRepository.findLinksByUserId(userId.get()).iterator().next();
            Assertions.assertEquals(url, expected.url());
            Assertions.assertEquals(dateTime, expected.updatedAt());
        }
    }

    @Test
    void findLinksByUserIdAndTag() {
        long chatId = 1231213L;
        String url = "github.com";
        String tag = "study";
        LocalDateTime dateTime = LocalDateTime.parse(LocalDateTime.now().format(formatter));
        Optional<Long> userId = linkRepository.addChat(chatId);

        if (userId.isPresent()) {
            Optional<Long> linkId = linkRepository.addLink(userId.get(), url, dateTime);
            Optional<Long> tagId = linkRepository.addTag(tag);
            if (linkId.isEmpty() || tagId.isEmpty()) {
                return;
            }
            linkRepository.addLinkTag(linkId.get(), tagId.get());

            Link actual = linkRepository
                    .findLinksByUserIdAndTag(userId.get(), tagId.get())
                    .iterator()
                    .next();

            Assertions.assertEquals(url, actual.url());
            Assertions.assertEquals(dateTime, actual.updatedAt());
        }
    }

    @Test
    void addThanDeleteLink() {
        long chatId = 1231214L;
        String url = "github.com";
        String tag = "study";
        LocalDateTime dateTime = LocalDateTime.parse(LocalDateTime.now().format(formatter));
        Optional<Long> userId = linkRepository.addChat(chatId);

        if (userId.isPresent()) {
            Optional<Long> linkId = linkRepository.addLink(userId.get(), url, dateTime);
            Optional<Long> tagId = linkRepository.addTag(tag);
            if (linkId.isEmpty() || tagId.isEmpty()) {
                return;
            }
            linkRepository.addLinkTag(linkId.get(), tagId.get());

            linkRepository.deleteLinkTag(linkId.get(), tagId.get());
            linkRepository.deleteLink(linkId.get());

            Set<Link> actual = linkRepository.findLinksByUserIdAndTag(userId.get(), tagId.get());

            Assertions.assertTrue(actual.isEmpty());
        }
    }
}
