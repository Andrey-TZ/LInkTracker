package backend.academy.bot.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class UpdateMessage extends ApplicationEvent {
    private Long chatId;
    private String message;

    public UpdateMessage(Long chatId, String message) {
        super(message);
        this.chatId = chatId;
        this.message = message;
    }
}
