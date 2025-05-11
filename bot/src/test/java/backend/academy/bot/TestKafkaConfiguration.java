package backend.academy.bot;

import static org.mockito.Mockito.mock;

import backend.academy.common.Update;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@TestConfiguration
public class TestKafkaConfiguration {
    @Bean
    public Map<String, Object> testProducerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                NotificationKafkaListenerTest.staticKafkaContainer.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public KafkaProducer<Long, Update> testProducer(Map<String, Object> testProducerConfig) {
        return new KafkaProducer<>(testProducerConfig);
    }

    @Bean
    public ApplicationEventPublisher eventPublisher() {
        return mock(ApplicationEventPublisher.class);
    }

    @Bean
    public NewTopic testTopic() {
        return TopicBuilder.name("test").partitions(1).build();
    }

    @Bean
    public Map<String, Object> testConsumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                NotificationKafkaListenerTest.staticKafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
        props.put("auto.offset.reset", "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "backend.academy.common");

        return props;
    }

    @Bean
    public KafkaConsumer<Long, Update> testConsumer(Map<String, Object> testConsumerConfig) {
        return new KafkaConsumer<>(testConsumerConfig);
    }
}
