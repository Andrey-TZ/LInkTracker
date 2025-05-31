package backend.academy.scrapper.sevices;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.common.Update;
import backend.academy.scrapper.config.KafkaConfiguration;
import backend.academy.scrapper.configs.TestsBeansContainersConfiguration;
import backend.academy.scrapper.configs.TestsKafkaConfiguration;
import backend.academy.scrapper.services.KafkaNotificationService;
import backend.academy.scrapper.services.NotificationService;
import java.time.Duration;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

// без этой аннотации @RestartScope для TestContainer не работает
@SpringBootTest(
        classes =
                KafkaConfiguration
                        .class) // Бины отсюда не используются, так как не получилось внедрить адрес контейнера с
// помощью @ServiceConnection
@Testcontainers
@ContextConfiguration(classes = {TestsKafkaConfiguration.class, TestsBeansContainersConfiguration.class})
class KafkaNotificationServiceTest {
    @Autowired
    private KafkaContainer kafkaContainer;

    @Autowired
    @Qualifier("testKafkaTemplate")
    private KafkaTemplate<Long, Update> kafkaTemplate;

    @Autowired
    private KafkaConsumer<Long, Update> kafkaConsumer;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${test.topic}")
    private String topic;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        kafkaContainer.start();
        notificationService = new KafkaNotificationService(kafkaTemplate, topic);
    }

    @Test
    void sendUpdate() {
        // Arrange
        Long chatId = 12345L;
        Update update = new Update();
        String testLink = "t-bank.com";
        String message = "Hello T!";
        update.link(testLink);
        update.messages(List.of(message));
        kafkaConsumer.subscribe(List.of("test"));

        // Act
        notificationService.sendUpdate(chatId, update);

        // Assert
        ConsumerRecords<Long, Update> records = kafkaConsumer.poll(Duration.ofSeconds(10));
        assertThat(records.count()).isGreaterThanOrEqualTo(1);

        ConsumerRecord<Long, Update> record = records.iterator().next();

        Assertions.assertEquals(chatId, record.key());
        Assertions.assertEquals(testLink, record.value().link());
        Assertions.assertEquals(message, record.value().messages().getFirst());
    }
}
