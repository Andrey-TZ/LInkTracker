package backend.academy.bot;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import backend.academy.bot.config.KafkaConfiguration;
import backend.academy.bot.model.UpdateMessage;
import backend.academy.common.Update;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@TestPropertySource(properties = {"kafka.topic=test", "app.message-transport=kafka"})
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {KafkaConfiguration.class, NotificationKafkaListener.class})
@Testcontainers
@Import({TestcontainersConfiguration.class, TestKafkaConfiguration.class})
@DirtiesContext
class NotificationKafkaListenerTest {
    public static final KafkaContainer staticKafkaContainer = new KafkaContainer("apache/kafka-native:3.8.1")
            .withExposedPorts(9092)
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

    @BeforeAll
    static void startKafka() {
        staticKafkaContainer.start();
    }

    @Autowired
    private KafkaProducer<Long, Update> kafkaProducer;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationKafkaListener kafkaListener;

    @Autowired
    private KafkaConsumer kafkaConsumer;

    @Value("${kafka.topic}")
    private String topic;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", staticKafkaContainer::getBootstrapServers);
        //        registry.add("kafka.topic", ()->"test");
    }

    @Test
    void sendThanRead_Success() throws InterruptedException {
        // Arrange
        String messageExpected = "t-bank.com - 1 обновлений";
        Long chatId = 12345L;
        Update update = new Update();
        String testLink = "t-bank.com";
        String messageUpdate = "Hello T!";
        update.link(testLink);
        update.messages(List.of(messageUpdate));

        // Act
        kafkaProducer.send(new ProducerRecord<>(topic, chatId, update));

        // Assert
        verify(eventPublisher, timeout(3000)).publishEvent(argThat(event -> {
            if (!(event instanceof UpdateMessage message)) return false;

            return Objects.equals(message.chatId(), chatId) && message.message().equals(messageExpected);
        }));
    }

    @Test
    void sendThanRead_Error() {
        // Arrange
        Long chatId = 12345L;
        Update update = null;

        // Act
        kafkaProducer.send(new ProducerRecord<>(topic, chatId, update));
        kafkaConsumer.subscribe(List.of("test-dlt"));

        // Assert
        ConsumerRecords<Long, Update> records = kafkaConsumer.poll(Duration.ofSeconds(10));
        Assertions.assertEquals(1, records.count());
    }
}
