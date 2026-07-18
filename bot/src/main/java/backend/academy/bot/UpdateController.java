package backend.academy.bot;

import backend.academy.bot.config.conditions.HttpTransportEnabledCondition;
import backend.academy.bot.model.UserMessage;
import backend.academy.common.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Conditional(HttpTransportEnabledCondition.class)
@RestController
@RequestMapping("/updates")
public class UpdateController {
    private final ApplicationEventPublisher eventPublisher;

    public UpdateController(@Autowired ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> receiveUpdates(@PathVariable("id") long id, @RequestBody Update update) {
        String message = update.link() + " - " + update.messages().size() + " обновлений";
        eventPublisher.publishEvent(new UserMessage(id, message));
        return ResponseEntity.ok("Updates were received");
    }
}
