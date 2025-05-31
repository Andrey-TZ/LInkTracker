package backend.academy.scrapper.sevices;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.common.Link;
import backend.academy.scrapper.config.RedisConfiguration;
import backend.academy.scrapper.configs.RedisTestConfiguration;
import backend.academy.scrapper.repos.LinkRepository;
import backend.academy.scrapper.services.link.JDBCLinkService;
import backend.academy.scrapper.services.link.LinkService;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
@SpringBootTest(classes = {RedisConfiguration.class, JDBCLinkService.class})
@ExtendWith(SpringExtension.class)
@Import(RedisTestConfiguration.class)
public class LinkServiceCacheTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private LinkService linkService;

    private static final long CHAT_ID = 123L;
    private final Link LINK = new Link("t-bank.ru");

    @Container
    private static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void configureRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
        System.out.println(redis.getHost());
        System.out.println(redis.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        cacheManager.getCache("userLinks").clear(); // имя кэша, указанное в @Cacheable("links")

        reset(linkRepository);
        when(linkRepository.userExists(CHAT_ID)).thenReturn(true);
        when(linkRepository.findUserIdByChatId(CHAT_ID)).thenReturn(Optional.of(CHAT_ID));
        when(linkRepository.findLinksByUserId(CHAT_ID)).thenReturn(Set.of(LINK));
        when(linkRepository.findLinkIdByUserIdAndUrl(CHAT_ID, LINK.url())).thenReturn(Optional.of(123L));
    }

    @Test
    void testCache() {
        // Arrange
        linkService.getLinks(CHAT_ID);

        // Act
        linkService.getLinks(CHAT_ID);

        // Assert
        verify(linkRepository, times(1)).findLinksByUserId(CHAT_ID);
    }

    @Test
    void invalidateCacheAfterDeleting() {
        // Arrange
        when(linkRepository.linkExists(CHAT_ID, LINK.url())).thenReturn(true);

        // Act
        linkService.getLinks(CHAT_ID);
        linkService.deleteLink(CHAT_ID, LINK);
        linkService.getLinks(CHAT_ID);

        // Assert
        verify(linkRepository, times(2)).findLinksByUserId(CHAT_ID);
    }

    @Test
    void invalidateCacheAfterAdding() {
        // Arrange
        when(linkRepository.linkExists(CHAT_ID, LINK.url())).thenReturn(false);
        when(linkRepository.addLink(CHAT_ID, LINK.url(), LINK.updatedAt())).thenReturn(Optional.of(1L));

        // Act
        linkService.getLinks(CHAT_ID);
        linkService.addLink(CHAT_ID, LINK);
        linkService.getLinks(CHAT_ID);

        // Assert
        verify(linkRepository, times(2)).findLinksByUserId(CHAT_ID);
    }
}
