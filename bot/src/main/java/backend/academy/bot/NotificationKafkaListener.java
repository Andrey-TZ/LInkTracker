package backend.academy.bot;

import backend.academy.bot.model.UpdateMessage;
import backend.academy.common.Update;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Conditional(KafkaEnabledCondition.class)
@Slf4j
@Component
public class NotificationKafkaListener {
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public NotificationKafkaListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2.0))
    @KafkaListener(
            topics = "${kafka.topic}",
            groupId = "first",
            concurrency = "1",
            containerFactory = "kafkaListenerContainerFactory"
            //        containerFactory = "factory"
            )
    public void listen(ConsumerRecord<Long, Update> record, Acknowledgment ack) {
        Long chatId = record.key();
        Update update = record.value();
        String message = update.link() + " - " + update.messages().size() + " обновлений";
        eventPublisher.publishEvent(new UpdateMessage(chatId, message));
        ack.acknowledge();
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<Long, Update> record) {
        log.atError()
                .addKeyValue("chatId", record.key())
                .addKeyValue("message", record.value().messages())
                .log("Не удалось обработать сообщение");
    }
}
