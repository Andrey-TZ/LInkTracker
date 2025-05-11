package backend.academy.scrapper.services;

import backend.academy.common.Update;
import backend.academy.scrapper.conditions.KafkaEnabledCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Conditional(KafkaEnabledCondition.class)
@Service
public class KafkaNotificationService implements NotificationService {
    private final KafkaTemplate<Long, Update> kafkaTemplate;
    private final String topicName;

    @Autowired
    public KafkaNotificationService(
            KafkaTemplate<Long, Update> kafkaTemplate, @Value("${spring.kafka.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void sendUpdate(long chatId, Update update) {
        kafkaTemplate.send(topicName, chatId, update);
        kafkaTemplate.flush();
    }
}
