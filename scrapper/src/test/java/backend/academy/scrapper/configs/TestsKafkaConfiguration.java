package backend.academy.scrapper.configs;

import backend.academy.common.Update;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.kafka.KafkaContainer;

@TestConfiguration
public class TestsKafkaConfiguration {
    @Bean
    public Map<String, Object> consumerConfig(KafkaContainer kafkaContainer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
        props.put("auto.offset.reset", "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "backend.academy.common");

        return props;
    }

    @Bean
    public KafkaConsumer<Long, Update> testConsumer(Map<String, Object> consumerConfig) {
        return new KafkaConsumer<>(consumerConfig);
    }

    @Bean
    public Map<String, Object> testProducerConfig(KafkaContainer kafkaContainer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<Long, Update> testProducerFactory(Map<String, Object> testProducerConfig) {
        return new DefaultKafkaProducerFactory<>(testProducerConfig);
    }

    @Bean
    public KafkaTemplate<Long, Update> testKafkaTemplate(ProducerFactory<Long, Update> testProducerFactory) {
        return new KafkaTemplate<>(testProducerFactory);
    }

    @Bean
    public NewTopic testTopic() {
        return TopicBuilder.name("test").partitions(1).build();
    }
}
