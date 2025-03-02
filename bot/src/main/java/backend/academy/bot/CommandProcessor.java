package backend.academy.bot;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import backend.academy.common.Link;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CommandProcessor {
    private static final String DOMAIN_REGEX = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    @Getter
    private final Map<String, CommandWithInfo> commands = new HashMap<>();
    private final ScrapperClient scrapperClient;
    private final ApplicationEventPublisher eventPublisher;

    public CommandProcessor(@Autowired ScrapperClient scrapperClient, @Autowired ApplicationEventPublisher eventPublisher) {
        this.scrapperClient = scrapperClient;
        this.eventPublisher = eventPublisher;
    }

    public boolean processCommand(long chatId, String text) {
        try {
            commands.get(text.split(" ")[0]).getCommand().execute(chatId, text);
            return true;
        } catch (ArrayIndexOutOfBoundsException | NullPointerException | ClassCastException e) {
            return false;
        }
    }

    public void registerCommand(String name, Command command, String description) {
        commands.put(name, new CommandWithInfo(command, description));
    }

    public void start(long chatId) {
        scrapperClient.addUser(chatId);
    }

    public void trackLink(long chatId, String text) {
        String[] parts = text.trim().split(" ", 2);
        if (parts.length < 2) {
            eventPublisher.publishEvent(new UpdateMessage(chatId, "Используйте: /track [ссылка]"));
            return;
        }
        String link = parts[1];
        try {
            URI uri = URI.create(link);
            String host = uri.getHost();
            if (uri.isAbsolute() && uri.getScheme() != null && host != null && host.matches(DOMAIN_REGEX)) {
                scrapperClient.sendLink(chatId, new Link(link));
            } else {
                eventPublisher.publishEvent(new UpdateMessage(chatId, "Введите валидную ссылку"));
            }
        } catch (IllegalArgumentException e) {
            eventPublisher.publishEvent(new UpdateMessage(chatId, "Введите валидную ссылку"));
        }
    }

    public void untrackLink(long chatId, String text) {
        String[] parts = text.split(" ", 2);
        if (parts.length < 2) {
            eventPublisher.publishEvent(new UpdateMessage(chatId, "Используйте: /untrack [ссылка]"));
            return;
        }
        scrapperClient.deleteLink(chatId, new Link(parts[1]));
    }

    public void list(long chatId) {
        scrapperClient.getLinks(chatId);
    }

    public void help(long chatId) {
        eventPublisher.publishEvent(new UpdateMessage(chatId, "Доступные команды:\n" +
            commands.values().stream()
                .map(CommandWithInfo::getDescription)
                .collect(Collectors.joining("\n"))));
    }
}
